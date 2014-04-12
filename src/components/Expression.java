package components;

import java.io.PrintWriter;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

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

		OP_SHL,         // <<
		OP_SHR,         // >>

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

		if (tree.getChildCount() == 0) {
			if (tree.getType() == CommonCppWithStreamsLexer.NUMBER) {
				exprType = ExpressionType.NUMBER_VALUE;
				type = Type.INT;
				numberValue = Integer.parseInt(tree.getText());
			} else if (tree.getType() == CommonCppWithStreamsLexer.BOOL_VALUE) {
				exprType = ExpressionType.BOOL_VALUE;
				type = Type.BOOL;
				boolValue = tree.getText().equals("true");
			} else if (tree.getType() == CommonCppWithStreamsLexer.NAME) {
				exprType = ExpressionType.VARIABLE;
				varName = tree.getText();
				boolean isBeingAssigned = (tree.getParent().getText().equals("=") && tree.getParent().getChild(0) == tree);
				type = st.referenceVariableAndGetType(varName, !isBeingAssigned, tree.getLine());
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

				String fileNameInQuotations = tree.getChild(0).getText();
				fileName = fileNameInQuotations.substring(1, fileNameInQuotations.length() - 1);
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
				funcCall = new FunctionCall(tree, ec);  // TODO: check args (here?)
				type = st.referenceFunctionAndGetType(funcCall.getFunctionName(), tree.getLine());

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

			} else if (tree.getText().equals("<<")) {
				twoSons(ExpressionType.OP_SHL);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '<<' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '<<' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
				type = Type.INT;
			} else if (tree.getText().equals(">>")) {
				twoSons(ExpressionType.OP_SHR);
				ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '>>' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
				ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '>>' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
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

	public Type getType() {
		return type;
	}

	public void writeCppCode(PrintWriter w) {
		switch (exprType) {
			case NUMBER_VALUE:
				w.print(numberValue);
				break;
			case BOOL_VALUE:
				w.print(boolValue ? "true" : "false");
				break;
			case VARIABLE:
				w.print(varName);
				break;
			case INPUT_STREAM_FUNC:
				w.print("InputStream()");
				break;
			case OUTPUT_STREAM_FUNC:
				w.print("OutputStream()");
				break;
			case INPUT_FILE_STREAM_FUNC:
				w.print("InputFileStream(" + fileName + ")");
				break;
			case OUTPUT_FILE_STREAM_FUNC:
				w.print("OutputFileStream(" + fileName + ")");
				break;
			case INPUT_BINARY_FILE_STREAM_FUNC:
				w.print("InputBinaryFileStream(" + fileName + ")");
				break;
			case OUTPUT_BINARY_FILE_STREAM_FUNC:
				w.print("OutputBinaryFileStream(" + fileName + ")");
				break;
			case FUNCTION_CALL:
				funcCall.writeCppCode(w);
				break;

			case OP_POSTFIX_PP:
				w.print("("); expr1.writeCppCode(w); w.print("++)");
				break;
			case OP_POSTFIX_MM:
				w.print("("); expr1.writeCppCode(w); w.print("--)");
				break;
			case OP_PREFIX_PP:
				w.print("(++"); expr1.writeCppCode(w); w.print(")");
				break;
			case OP_PREFIX_MM:
				w.print("(--"); expr1.writeCppCode(w); w.print(")");
				break;
			case OP_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" = "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_MULT_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" *= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_DIV_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" /= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_MOD_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" %= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_PLUS_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" += "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_MINUS_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" -= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_SHR_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" >>= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_SHL_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" <<= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_AND_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" &= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_XOR_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" ^= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_OR_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" |= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_OR:
				w.print("("); expr1.writeCppCode(w); w.print(" || "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_AND:
				w.print("("); expr1.writeCppCode(w); w.print(" && "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_BIN_OR:
				w.print("("); expr1.writeCppCode(w); w.print(" | "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_BIN_XOR:
				w.print("("); expr1.writeCppCode(w); w.print(" ^ "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_BIN_AND:
				w.print("("); expr1.writeCppCode(w); w.print(" & "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_EQ_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" == "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_NOT_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" != "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_LE_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" <= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_GR_EQ:
				w.print("("); expr1.writeCppCode(w); w.print(" >= "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_LE:
				w.print("("); expr1.writeCppCode(w); w.print(" < "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_GR:
				w.print("("); expr1.writeCppCode(w); w.print(" > "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_SHL:
				w.print("("); expr1.writeCppCode(w); w.print(" << "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_SHR:
				w.print("("); expr1.writeCppCode(w); w.print(" >> "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_PLUS:
				w.print("("); expr1.writeCppCode(w); w.print(" + "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_MINUS:
				w.print("("); expr1.writeCppCode(w); w.print(" - "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_MULT:
				w.print("("); expr1.writeCppCode(w); w.print(" * "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_DIV:
				w.print("("); expr1.writeCppCode(w); w.print(" / "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_MOD:
				w.print("("); expr1.writeCppCode(w); w.print(" % "); expr2.writeCppCode(w); w.print(")");
				break;
			case OP_UNARY_PLUS:
				expr1.writeCppCode(w);
				break;
			case OP_UNARY_MINUS:
				w.print("(-"); expr1.writeCppCode(w); w.print(")");
				break;
			case OP_NOT:
				w.print("(!"); expr1.writeCppCode(w); w.print(")");
				break;
			case OP_BIN_NOT:
				w.print("(~"); expr1.writeCppCode(w); w.print(")");
				break;
		}
	}
	
	public void writeAsmCode(PrintWriter w) {
		switch (exprType) {
			case NUMBER_VALUE:
				w.println("    mov eax, " + numberValue);
				w.println("    push eax");
				break;
			case BOOL_VALUE:
				w.println("    mov eax, " + (boolValue ? "1" : "0"));
				w.println("    push eax");
				break;
			case VARIABLE:
				// TODO:
				break;
			case INPUT_STREAM_FUNC:
				break;
			case OUTPUT_STREAM_FUNC:
				break;
			case INPUT_FILE_STREAM_FUNC:
				break;
			case OUTPUT_FILE_STREAM_FUNC:
				break;
			case INPUT_BINARY_FILE_STREAM_FUNC:
				break;
			case OUTPUT_BINARY_FILE_STREAM_FUNC:
				break;
			case FUNCTION_CALL:
				//TODO: push arguments
				w.println("    call _func_" + funcCall.getFunctionName());
				break;
	
			case OP_POSTFIX_PP:
				// TODO:
				break;
			case OP_POSTFIX_MM:
				break;
			case OP_PREFIX_PP:
				break;
			case OP_PREFIX_MM:
				break;
			case OP_EQ:
				break;
			case OP_MULT_EQ:
				break;
			case OP_DIV_EQ:
				break;
			case OP_MOD_EQ:
				break;
			case OP_PLUS_EQ:
				break;
			case OP_MINUS_EQ:
				break;
			case OP_SHR_EQ:
				break;
			case OP_SHL_EQ:
				break;
			case OP_AND_EQ:
				break;
			case OP_XOR_EQ:
				break;
			case OP_OR_EQ:
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
				break;
			case OP_NOT_EQ:
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
				w.println("    pop ebx");
				w.println("    pop eax");
				w.println("    add eax, ebx");
				w.println("    push eax");
				break;
			case OP_MINUS:
				w.println("    pop ebx");
				w.println("    pop eax");
				w.println("    sub eax, ebx");
				w.println("    push eax");
				break;
			case OP_MULT:
				w.println("    pop ebx");
				w.println("    pop eax");
				w.println("    imul eax, ebx");
				w.println("    push eax");
				break;
			case OP_DIV:
				w.println("    pop ebx");
				w.println("    pop eax");
				w.println("    idiv eax, ebx");
				w.println("    push eax");
				break;
			case OP_MOD:
				break;
			case OP_UNARY_PLUS:
				break;
			case OP_UNARY_MINUS:
				break;
			case OP_NOT:
				break;
			case OP_BIN_NOT:
				break;
		}
	}
}
