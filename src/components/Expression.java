package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.Type;
import base.TypeChecker;
import base.TypeConverter;

public class Expression implements CodeProvider {
	private enum ExpressionType {
		NUMBER_VALUE,
		BOOL_VALUE,
		VARIABLE,
		
		INPUT_STREAM_FUNC,
		OUTPUT_STREAM_FUNC,
		
		INPUT_FILE_STREAM_FUNC,
		OUTPUT_FILE_STREAM_FUNC,
		INPUT_BINARY_FILE_STREAM_FUNC,
		OUTPUT_BINARY_FILE_STREAM_FUNC,
		
		FUNCTION_CALL,
		
		OP_POSTFIX_PP,  // i++
		OP_POSTFIX_MM,  // i--
		OP_PREFIX_PP,   // ++i
		OP_PREFIX_MM,   // --i
		
		OP_EQ,          // =
		OP_MULT_EQ,     // *=
		OP_DIV_EQ,      // /=
		OP_MOD_EQ,      // %=
		OP_PLUS_EQ,     // +=
		OP_MINUS_EQ,    // -=
		OP_SHR_EQ,      // >>=
		OP_SHL_EQ,      // <<=
		OP_AND_EQ,      // &=
		OP_XOR_EQ,      // ^=
		OP_OR_EQ,       // |=
		
		OP_OR,          // ||
		OP_AND,         // &&
		
		OP_BIN_OR,      // |
		OP_BIN_XOR,     // ^
		OP_BIN_AND,     // &

		OP_EQ_EQ,       // ==
		OP_NOT_EQ,      // !=

		OP_LE_EQ,       // <=
		OP_GR_EQ,       // >=
		OP_LE,          // <
		OP_GR,          // >

		OP_SHL,         // <<<
		OP_SHR,         // >>>

		OP_PLUS,        // a + b
		OP_MINUS,       // a - b
		
		OP_MULT,        // *
		OP_DIV,         // /
		OP_MOD,         // %

		OP_UNARY_PLUS,  // +i
		OP_UNARY_MINUS, // -i

		OP_NOT,         // !
		OP_BIN_NOT      // ~
	}

	private ErrorsCollector ec;
	private SymbolTable st;
	private Tree tree;

	private ExpressionType exprType;
	private Type type;

	// if has NO children
	private int numberValue;
	private boolean boolValue;
	private String varName;
	private VarDef varDef;
	
	// if has children
	private String fileName;
	private FunctionCall funcCall;
	
	private Expression expr1;
	private Expression expr2;
	
	private void oneSon(ExpressionType exprType) {
		assert(tree.getChildCount() == 1);
		this.exprType = exprType;
		expr1 = new Expression(tree.getChild(0), ec, st);
	}
	
	private void twoSons(ExpressionType exprType) {
		assert(tree.getChildCount() == 2);
		this.exprType = exprType;
		expr1 = new Expression(tree.getChild(0), ec, st);
		expr2 = new Expression(tree.getChild(1), ec, st);
	}

	public Expression(Tree tree, ErrorsCollector ec, SymbolTable st) {
		this.tree = tree;
		this.ec = ec;
		this.st = st;

		expr1 = null;
		expr2 = null;

		if (tree.getChildCount() == 0) {
			if (tree.getType() == CommonCppWithStreamsLexer.NUMBER) {
				exprType = ExpressionType.NUMBER_VALUE;
				type = Type.INT;
				numberValue = Integer.parseInt(tree.getText());
			} else if (tree.getType() == CommonCppWithStreamsLexer.BOOL_VALUE) {
				exprType = ExpressionType.BOOL_VALUE;
				type = Type.BOOL;
				boolValue = tree.getText().equals("true");
			} else if (tree.getType() == CommonCppWithStreamsLexer.TRUE) {
				exprType = ExpressionType.BOOL_VALUE;
				type = Type.BOOL;
				boolValue = true;
			} else if (tree.getType() == CommonCppWithStreamsLexer.NAME) {
				exprType = ExpressionType.VARIABLE;
				varName = tree.getText();
				boolean isBeingAssigned = (tree.getParent().getText().equals("=") && tree.getParent().getChild(0) == tree);
				varDef = st.referenceVariableAndGetVarDef(varName, !isBeingAssigned, tree.getLine());
				type = varDef.getType();
				if (isBeingAssigned) {
					st.setVariableInitialized(varName);
				}
			} else if (tree.getType() == CommonCppWithStreamsLexer.STREAM_FUNC) {
				if (tree.getText().equals("InputStream")) {
					exprType = ExpressionType.INPUT_STREAM_FUNC;
					type = Type.ISTREAM;
				} else if (tree.getText().equals("OutputStream")) {
					exprType = ExpressionType.OUTPUT_STREAM_FUNC;
					type = Type.OSTREAM;
				} else {
					assert(false);
				}
			} else {
				assert(false);
			}
		} else {
			if (tree.getType() == CommonCppWithStreamsLexer.STREAM_F_FUNC) {
				assert(tree.getChildCount() == 1);
				assert(tree.getChild(0).getType() == CommonCppWithStreamsLexer.FILE_NAME_STR);

				fileName = tree.getChild(0).getText();  // file name in quotations
				ec.check(fileName.length() <= 257, tree.getChild(0).getLine(), "File name can not be longer than 255 characters");
				st.incFileNamesCount();
				if (tree.getText().equals("InputFileStream")) {
					exprType = ExpressionType.INPUT_FILE_STREAM_FUNC;
					type = Type.ISTREAM;
				} else if (tree.getText().equals("OutputFileStream")) {
					exprType = ExpressionType.OUTPUT_FILE_STREAM_FUNC;
					type = Type.OSTREAM;
				} else if (tree.getText().equals("InputBinaryFileStream")) {
					exprType = ExpressionType.INPUT_BINARY_FILE_STREAM_FUNC;
					type = Type.ISTREAM;
				} else if (tree.getText().equals("OutputBinaryFileStream")) {
					exprType = ExpressionType.OUTPUT_BINARY_FILE_STREAM_FUNC;
					type = Type.OSTREAM;
				} else {
					assert(false);
				}
			} else if (tree.getType() == CommonCppWithStreamsLexer.CALL) {
				exprType = ExpressionType.FUNCTION_CALL;
				funcCall = new FunctionCall(tree, ec, st);
				type = funcCall.getType();

			} else if (tree.getType() == CommonCppWithStreamsLexer.POSTFIX_PP) {
				oneSon(ExpressionType.OP_POSTFIX_PP);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as increment operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "can not increment '" + TypeConverter.typeToString(expr1) + "'");
				type = expr1.getType();

			} else if (tree.getType() == CommonCppWithStreamsLexer.POSTFIX_MM) {
				oneSon(ExpressionType.OP_POSTFIX_MM);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as decrement operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "can not decrement '" + TypeConverter.typeToString(expr1) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("=")) {
				twoSons(ExpressionType.OP_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '=' operand");
				ec.check(expr1.getType() != Type.VOID, tree.getLine(), "'void' can not be assigned");
				ec.check(TypeChecker.canBeAssigned(expr1, expr2), tree.getLine(), "can not assign '" + TypeConverter.typeToString(expr1) + "' to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("*=")) {
				twoSons(ExpressionType.OP_MULT_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '*=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '*=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '*=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("/=")) {
				twoSons(ExpressionType.OP_DIV_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '/=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '/=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '/=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("%=")) {
				twoSons(ExpressionType.OP_MOD_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '%=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '%=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '%=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("+=")) {
				twoSons(ExpressionType.OP_PLUS_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '+=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '+=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '+=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("-=")) {
				twoSons(ExpressionType.OP_MINUS_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '-=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '-=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '-=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals(">>=")) {
				twoSons(ExpressionType.OP_SHR_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '>>=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '>>=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '>>=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("<<=")) {
				twoSons(ExpressionType.OP_SHL_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '<<=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '<<=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '<<=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("&=")) {
				twoSons(ExpressionType.OP_AND_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '&=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '&=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '&=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("^=")) {
				twoSons(ExpressionType.OP_XOR_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '^=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '^=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '^=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("|=")) {
				twoSons(ExpressionType.OP_OR_EQ);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '|=' operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '|=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '|=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = expr1.getType();

			} else if (tree.getText().equals("||")) {
				twoSons(ExpressionType.OP_OR);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '||' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '||' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.BOOL;

			} else if (tree.getText().equals("&&")) {
				twoSons(ExpressionType.OP_AND);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '&&' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '&&' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.BOOL;

			} else if (tree.getText().equals("|")) {
				twoSons(ExpressionType.OP_BIN_OR);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '|' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '|' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.INT;

			} else if (tree.getText().equals("^")) {
				twoSons(ExpressionType.OP_BIN_XOR);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '^' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '^' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.INT;

			} else if (tree.getText().equals("&")) {
				twoSons(ExpressionType.OP_BIN_AND);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '&' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '&' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.INT;

			} else if (tree.getText().equals("==")) {
				twoSons(ExpressionType.OP_EQ_EQ);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '==' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '==' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.BOOL;
			} else if (tree.getText().equals("!=")) {
				twoSons(ExpressionType.OP_NOT_EQ);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '!=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '!=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.BOOL;

			} else if (tree.getText().equals("<=")) {
				twoSons(ExpressionType.OP_LE_EQ);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '<=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '<=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.BOOL;
			} else if (tree.getText().equals(">=")) {
				twoSons(ExpressionType.OP_GR_EQ);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '>=' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '>=' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.BOOL;
			} else if (tree.getText().equals("<")) {
				twoSons(ExpressionType.OP_LE);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '<' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '<' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.BOOL;
			} else if (tree.getText().equals(">")) {
				twoSons(ExpressionType.OP_GR);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '>' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '>' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.BOOL;

			} else if (tree.getText().equals("<<<")) {
				twoSons(ExpressionType.OP_SHL);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '<<<' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '<<<' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.INT;
			} else if (tree.getText().equals(">>>")) {
				twoSons(ExpressionType.OP_SHR);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '>>>' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '>>>' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.INT;

			} else if (tree.getText().equals("+")) {
				type = Type.INT;
				if (tree.getChildCount() == 1) {
					oneSon(ExpressionType.OP_UNARY_PLUS);
					ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "unary operator '+' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				} else {
					twoSons(ExpressionType.OP_PLUS);
					ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '+' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
					ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '+' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				}
			} else if (tree.getText().equals("-")) {
				type = Type.INT;
				if (tree.getChildCount() == 1) {
					oneSon(ExpressionType.OP_UNARY_MINUS);
					ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "unary operator '-' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				} else {
					twoSons(ExpressionType.OP_MINUS);
					ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '-' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
					ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '-' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				}

			} else if (tree.getText().equals("*")) {
				twoSons(ExpressionType.OP_MULT);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '*' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '*' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.INT;
			} else if (tree.getText().equals("/")) {
				twoSons(ExpressionType.OP_DIV);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '/' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '/' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.INT;
			} else if (tree.getText().equals("%")) {
				twoSons(ExpressionType.OP_MOD);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '%' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '%' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.INT;

			} else if (tree.getText().equals("++")) {
				oneSon(ExpressionType.OP_PREFIX_PP);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as increment operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "can not increment '" + TypeConverter.typeToString(expr1) + "'");
				type = expr1.getType();
			} else if (tree.getText().equals("--")) {
				oneSon(ExpressionType.OP_PREFIX_MM);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as decrement operand");
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "can not decrement '" + TypeConverter.typeToString(expr1) + "'");
				type = expr1.getType();
			} else if (tree.getText().equals("!")) {
				oneSon(ExpressionType.OP_NOT);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "unary operator '!' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				type = Type.BOOL;
			} else if (tree.getText().equals("~")) {
				oneSon(ExpressionType.OP_BIN_NOT);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "unary operator '~' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				type = Type.INT;

			} else {
				assert(false);
			}
		}
	}

	private boolean isLValue() {
		return exprType == ExpressionType.VARIABLE ||

				exprType == ExpressionType.OP_PREFIX_PP ||
				exprType == ExpressionType.OP_PREFIX_MM ||

				exprType == ExpressionType.OP_EQ ||
				exprType == ExpressionType.OP_MULT_EQ ||
				exprType == ExpressionType.OP_DIV_EQ ||
				exprType == ExpressionType.OP_MOD_EQ ||
				exprType == ExpressionType.OP_PLUS_EQ ||
				exprType == ExpressionType.OP_MINUS_EQ ||
				exprType == ExpressionType.OP_SHR_EQ ||
				exprType == ExpressionType.OP_SHL_EQ ||
				exprType == ExpressionType.OP_AND_EQ ||
				exprType == ExpressionType.OP_XOR_EQ ||
				exprType == ExpressionType.OP_OR_EQ;
	}

	private Expression getLValueVariable() {
		assert(isLValue());

		if (exprType == ExpressionType.VARIABLE)
			return this;
		return expr1.getLValueVariable();
	}

	private VarDef getLValueVariableVarDef() {
		return getLValueVariable().varDef;
	}

	public Type getType() {
		return type;
	}

	public void writeAsmCode(AsmWriter w) {
		switch (exprType) {
			case NUMBER_VALUE:
				w.t("Expression: Number");
				w.c("mov eax, " + numberValue);
				w.push("eax");
				break;
			case BOOL_VALUE:
				w.t("Expression: Bool");
				w.c("mov eax, " + (boolValue ? "1" : "0"));
				w.push("eax");
				break;
			case VARIABLE:
				w.t("Expression: Variable " + varDef.getName());
				w.c("mov eax, " + w.varAddr(varDef));
				w.push("eax");
				break;
			case INPUT_STREAM_FUNC:
				w.t("Expression: InputStream()");
				w.c("mov eax, 1");
				w.push("eax");
				break;
			case OUTPUT_STREAM_FUNC:
				w.t("Expression: OutputStream()");
				w.c("mov eax, 2");
				w.push("eax");
				break;
			case INPUT_FILE_STREAM_FUNC:
				{
				w.t("Expression: InputFileStream()");
				int fileNameNum = w.addFileName(fileName);
				assert(fileNameNum <= 0x00FFFFFF);
				w.c("mov eax, " + ((fileNameNum << 8) | 3));
				w.push("eax");
				}
				break;
			case OUTPUT_FILE_STREAM_FUNC:
				{
				w.t("Expression: OutputFileStream()");
				int fileNameNum = w.addFileName(fileName);
				assert(fileNameNum <= 0x00FFFFFF);
				w.c("mov eax, " + ((fileNameNum << 8) | 4));
				w.push("eax");
				}
				break;
			case INPUT_BINARY_FILE_STREAM_FUNC:
				{
				w.t("Expression: InputBinaryFileStream()");
				int fileNameNum = w.addFileName(fileName);
				assert(fileNameNum <= 0x00FFFFFF);
				w.c("mov eax, " + ((fileNameNum << 8) | 5));
				w.push("eax");
				}
				break;
			case OUTPUT_BINARY_FILE_STREAM_FUNC:
				{
				w.t("Expression: OutputBinaryFileStream()");
				int fileNameNum = w.addFileName(fileName);
				assert(fileNameNum <= 0x00FFFFFF);
				w.c("mov eax, " + ((fileNameNum << 8) | 6));
				w.push("eax");
				}
				break;
			case FUNCTION_CALL:
				funcCall.writeAsmCode(w);
				break;
	
			case OP_POSTFIX_PP:
				expr1.writeAsmCode(w);
				w.t("Expression: postfix ++");
				w.pop("eax");
				w.push("eax");
				w.c("inc eax");
				w.c("mov " + w.varAddr(expr1.getLValueVariableVarDef()) + ", eax");
				break;
			case OP_POSTFIX_MM:
				expr1.writeAsmCode(w);
				w.t("Expression: postfix --");
				w.pop("eax");
				w.push("eax");
				w.c("dec eax");
				w.c("mov " + w.varAddr(expr1.getLValueVariableVarDef()) + ", eax");
				break;
			case OP_PREFIX_PP:
				expr1.writeAsmCode(w);
				w.t("Expression: prefix ++");
				w.pop("eax");
				w.c("inc eax");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;
			case OP_PREFIX_MM:
				expr1.writeAsmCode(w);
				w.t("Expression: prefix --");
				w.pop("eax");
				w.c("dec eax");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;

			case OP_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: =");
				w.pop("ebx");
				w.pop("eax");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", ebx");
				w.push("ebx");
				break;
			case OP_MULT_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: *=");
				w.pop("ebx");
				w.pop("eax");
				w.c("imul eax, ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;
			case OP_DIV_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: /=");
				w.pop("ebx");
				w.pop("eax");
				w.c("idiv eax, ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;
			case OP_MOD_EQ:
				break;
			case OP_PLUS_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: +=");
				w.pop("ebx");
				w.pop("eax");
				w.c("add eax, ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;
			case OP_MINUS_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: -=");
				w.pop("ebx");
				w.pop("eax");
				w.c("sub eax, ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;
			case OP_SHR_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: >>=");
				w.pop("ebx");
				w.pop("eax");
				w.c("shr eax, ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;
			case OP_SHL_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: <<=");
				w.pop("ebx");
				w.pop("eax");
				w.c("shl eax, ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;
			case OP_AND_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: &=");
				w.pop("ebx");
				w.pop("eax");
				w.c("and eax, ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;
			case OP_XOR_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: ^=");
				w.pop("ebx");
				w.pop("eax");
				w.c("xor eax, ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;
			case OP_OR_EQ:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: |=");
				w.pop("ebx");
				w.pop("eax");
				w.c("or eax, ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
				w.push("eax");
				break;

			case OP_OR:
				break;
			case OP_AND:
				break;
			case OP_BIN_OR:
				break;
			case OP_BIN_XOR:
				break;
			case OP_BIN_AND:
				break;
			case OP_EQ_EQ:
				{
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: ==");
				w.pop("ebx");
				w.pop("eax");
				w.c("cmp eax, ebx");
				int uid = w.genNewUId();
				w.c("jnz _cmp_" + uid + "_false");
				w.c("mov eax, 1");
				w.c("jmp _cmp_" + uid + "_end");
				w.l("_cmp_" + uid + "_false");
				w.c("mov eax, 0");
				w.l("_cmp_" + uid + "_end");
				w.push("eax");
				}
				break;
			case OP_NOT_EQ:
				{
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: !=");
				w.pop("ebx");
				w.pop("eax");
				w.c("cmp eax, ebx");
				int uid = w.genNewUId();
				w.c("jz _cmp_" + uid + "_false");
				w.c("mov eax, 1");
				w.c("jmp _cmp_" + uid + "_end");
				w.l("_cmp_" + uid + "_false");
				w.c("mov eax, 0");
				w.l("_cmp_" + uid + "_end");
				w.push("eax");
				}
				break;
			case OP_LE_EQ:
				break;
			case OP_GR_EQ:
				break;
			case OP_LE:
				break;
			case OP_GR:
				break;
			case OP_SHL:
				break;
			case OP_SHR:
				break;
			case OP_PLUS:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: +");
				w.pop("ebx");
				w.pop("eax");
				w.c("add eax, ebx");
				w.push("eax");
				break;
			case OP_MINUS:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: -");
				w.pop("ebx");
				w.pop("eax");
				w.c("sub eax, ebx");
				w.push("eax");
				break;
			case OP_MULT:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: *");
				w.pop("ebx");
				w.pop("eax");
				w.c("imul eax, ebx");
				w.push("eax");
				break;
			case OP_DIV:
				expr1.writeAsmCode(w);
				expr2.writeAsmCode(w);
				w.t("Expression: /");
				w.pop("ebx");
				w.pop("eax");
				w.c("idiv eax, ebx");
				w.push("eax");
				break;
			case OP_MOD:
				break;
			case OP_UNARY_PLUS:
				expr1.writeAsmCode(w);
				break;
			case OP_UNARY_MINUS:
				expr1.writeAsmCode(w);
				w.t("Expression: unary -");
				w.pop("eax");
				w.c("neg eax");
				w.push("eax");
				break;
			case OP_NOT:
				break;
			case OP_BIN_NOT:
				break;
		}
		w.ln();
	}
}
