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
	private static final String ASM_SHR = "mov ecx, ebx\n    shr eax, cl";
	private static final String ASM_SHL = "mov ecx, ebx\n    shl eax, cl";

	private static final String ASM_DIV = " push edx\n     xor edx, edx\n     cdq\n     idiv ebx\n     pop edx";
	private static final String ASM_MOD = " push edx\n     xor edx, edx\n     cdq\n     idiv ebx\n     mov eax, edx\n     pop edx";

	private enum ExpressionType {
		NOP,

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

	private void initLValueSmthEq(ExpressionType exprType, String op) {
		twoSons(exprType);
		ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '" + op + "' operand");
		ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '" + op + "' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
		ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '" + op + "' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
		type = expr1.getType();
	}

	private void initBinaryOp(ExpressionType exprType, String op, Type type) {
		twoSons(exprType);
		ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "operator '" + op + "' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
		ec.check(TypeChecker.isIntOrBool(expr2), tree.getLine(), "operator '" + op + "' can not be applied to '" + TypeConverter.typeToString(expr2) + "'");
		this.type = type;
	}

	private void initUnaryOp(ExpressionType exprType, String op, Type type) {
		oneSon(exprType);
		ec.check(TypeChecker.isIntOrBool(expr1), tree.getLine(), "unary operator '" + op + "' can not be applied to '" + TypeConverter.typeToString(expr1) + "'");
		this.type = type;
	}

	public Expression(Tree tree, ErrorsCollector ec, SymbolTable st) {
		this.tree = tree;
		this.ec = ec;
		this.st = st;

		expr1 = null;
		expr2 = null;

		if (tree.getChildCount() == 0) {
			if (tree.getType() == CommonCppWithStreamsLexer.NOP) {
				exprType = ExpressionType.NOP;
			} else if (tree.getType() == CommonCppWithStreamsLexer.NUMBER) {
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
				if (varDef != null) {
					type = varDef.getType();
					if (isBeingAssigned) {
						st.setVariableInitialized(varName);
					}
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
				initLValueSmthEq(ExpressionType.OP_MULT_EQ, "*=");
			} else if (tree.getText().equals("/=")) {
				initLValueSmthEq(ExpressionType.OP_DIV_EQ, "/=");
			} else if (tree.getText().equals("%=")) {
				initLValueSmthEq(ExpressionType.OP_MOD_EQ, "%=");
			} else if (tree.getText().equals("+=")) {
				initLValueSmthEq(ExpressionType.OP_PLUS_EQ, "+=");
			} else if (tree.getText().equals("-=")) {
				initLValueSmthEq(ExpressionType.OP_MINUS_EQ, "-=");
			} else if (tree.getText().equals(">>=")) {
				initLValueSmthEq(ExpressionType.OP_SHR_EQ, ">>=");
			} else if (tree.getText().equals("<<=")) {
				initLValueSmthEq(ExpressionType.OP_SHL_EQ, "<<=");
			} else if (tree.getText().equals("&=")) {
				initLValueSmthEq(ExpressionType.OP_AND_EQ, "&=");
			} else if (tree.getText().equals("^=")) {
				initLValueSmthEq(ExpressionType.OP_XOR_EQ, "^=");
			} else if (tree.getText().equals("|=")) {
				initLValueSmthEq(ExpressionType.OP_OR_EQ, "|=");

			} else if (tree.getText().equals("||")) {
				initBinaryOp(ExpressionType.OP_OR, "||", Type.BOOL);
			} else if (tree.getText().equals("&&")) {
				initBinaryOp(ExpressionType.OP_AND, "&&", Type.BOOL);

			} else if (tree.getText().equals("|")) {
				initBinaryOp(ExpressionType.OP_BIN_OR, "|", Type.INT);
			} else if (tree.getText().equals("^")) {
				initBinaryOp(ExpressionType.OP_BIN_XOR, "^", Type.INT);
			} else if (tree.getText().equals("&")) {
				initBinaryOp(ExpressionType.OP_BIN_AND, "&", Type.INT);

			} else if (tree.getText().equals("==")) {
				initBinaryOp(ExpressionType.OP_EQ_EQ, "==", Type.BOOL);
			} else if (tree.getText().equals("!=")) {
				initBinaryOp(ExpressionType.OP_NOT_EQ, "!=", Type.BOOL);
			} else if (tree.getText().equals("<=")) {
				initBinaryOp(ExpressionType.OP_LE_EQ, "<=", Type.BOOL);
			} else if (tree.getText().equals(">=")) {
				initBinaryOp(ExpressionType.OP_GR_EQ, ">=", Type.BOOL);
			} else if (tree.getText().equals("<")) {
				initBinaryOp(ExpressionType.OP_LE, "<", Type.BOOL);
			} else if (tree.getText().equals(">")) {
				initBinaryOp(ExpressionType.OP_GR, ">", Type.BOOL);

			} else if (tree.getText().equals("<<<")) {
				initBinaryOp(ExpressionType.OP_SHL, "<<<", Type.INT);
			} else if (tree.getText().equals(">>>")) {
				initBinaryOp(ExpressionType.OP_SHR, ">>>", Type.INT);

			} else if (tree.getText().equals("+")) {
				if (tree.getChildCount() == 1) {
					initUnaryOp(ExpressionType.OP_UNARY_PLUS, "+", Type.INT);
				} else {
					initBinaryOp(ExpressionType.OP_PLUS, "+", Type.INT);
				}
			} else if (tree.getText().equals("-")) {
				if (tree.getChildCount() == 1) {
					initUnaryOp(ExpressionType.OP_UNARY_MINUS, "-", Type.INT);
				} else {
					initBinaryOp(ExpressionType.OP_MINUS, "-", Type.INT);
				}

			} else if (tree.getText().equals("*")) {
				initBinaryOp(ExpressionType.OP_MULT, "*", Type.INT);
			} else if (tree.getText().equals("/")) {
				initBinaryOp(ExpressionType.OP_DIV, "/", Type.INT);
			} else if (tree.getText().equals("%")) {
				initBinaryOp(ExpressionType.OP_MOD, "%", Type.INT);

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
				initUnaryOp(ExpressionType.OP_NOT, "!", Type.BOOL);
			} else if (tree.getText().equals("~")) {
				initUnaryOp(ExpressionType.OP_BIN_NOT, "~", Type.INT);

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

	private void writeAsmCodeForOpSmthEq(AsmWriter w, String op, String asmLine) {
		// Order is important (!!!)
		expr2.writeAsmCode(w);
		expr1.writeAsmCode(w);
		w.t("Expression: " + op);
		w.pop("eax");
		w.pop("ebx");
		w.c(asmLine);
		w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", eax");
		w.push("eax");
	}

	private void writeAsmCodeForBinaryCountOp(AsmWriter w, String op, String asmLine) {
		expr1.writeAsmCode(w);
		expr2.writeAsmCode(w);
		w.t("Expression: " + op);
		w.pop("ebx");
		w.pop("eax");
		w.c(asmLine);
		w.push("eax");
	}

	private void writeAsmCodeForBinaryCmpOp(AsmWriter w, String op, String condGotoIfTrueCommand) {
		expr1.writeAsmCode(w);
		expr2.writeAsmCode(w);
		w.t("Expression: " + op);
		w.pop("ebx");
		w.pop("eax");
		w.c("cmp eax, ebx");
		int uid = w.genNewUId();
		w.c(condGotoIfTrueCommand + " _cmp_" + uid + "_true");
		w.c("mov eax, 0");
		w.c("jmp _cmp_" + uid + "_end");
		w.l("_cmp_" + uid + "_true");
		w.c("mov eax, 1");
		w.l("_cmp_" + uid + "_end");
		w.push("eax");
	}

	private void writeAsmCodeForFileStream(AsmWriter w, String func, int streamTypeId) {
		w.t("Expression: " + func);
		int fileNameNum = w.addFileName(fileName);
		assert(fileNameNum <= 0x00FFFFFF);
		w.c("mov eax, " + ((fileNameNum << 8) | streamTypeId));
		w.push("eax");
	}

	public void writeAsmCode(AsmWriter w) {
		switch (exprType) {
			case NOP:
				w.push("eax");
				break;
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
				writeAsmCodeForFileStream(w, "InputFileStream()", 3);
				break;
			case OUTPUT_FILE_STREAM_FUNC:
				writeAsmCodeForFileStream(w, "OutputFileStream()", 4);
				break;
			case INPUT_BINARY_FILE_STREAM_FUNC:
				writeAsmCodeForFileStream(w, "InputBinaryFileStream()", 5);
				break;
			case OUTPUT_BINARY_FILE_STREAM_FUNC:
				writeAsmCodeForFileStream(w, "OutputBinaryFileStream()", 6);
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
				expr2.writeAsmCode(w);
				expr1.writeAsmCode(w);
				w.t("Expression: =");
				w.pop("eax");
				w.pop("ebx");
				w.c("mov " + w.varAddr(getLValueVariableVarDef()) + ", ebx");
				w.push("ebx");
				break;

			case OP_MULT_EQ:
				writeAsmCodeForOpSmthEq(w, "*=", "imul eax, ebx");
				break;
			case OP_DIV_EQ:
				writeAsmCodeForOpSmthEq(w, "/=", ASM_DIV);
				break;
			case OP_MOD_EQ:
				writeAsmCodeForOpSmthEq(w, "%=", ASM_MOD);
				break;
			case OP_PLUS_EQ:
				writeAsmCodeForOpSmthEq(w, "+=", "add eax, ebx");
				break;
			case OP_MINUS_EQ:
				writeAsmCodeForOpSmthEq(w, "-=", "sub eax, ebx");
				break;
			case OP_SHR_EQ:
				writeAsmCodeForOpSmthEq(w, ">>=", ASM_SHR);
				break;
			case OP_SHL_EQ:
				writeAsmCodeForOpSmthEq(w, "<<=", ASM_SHL);
				break;
			case OP_AND_EQ:
				writeAsmCodeForOpSmthEq(w, "&=", "and eax, ebx");
				break;
			case OP_XOR_EQ:
				writeAsmCodeForOpSmthEq(w, "^=", "xor eax, ebx");
				break;
			case OP_OR_EQ:
				writeAsmCodeForOpSmthEq(w, "|=", "or eax, ebx");
				break;

			case OP_OR:
				{
				expr1.writeAsmCode(w);
				w.t("Expression: ||");
				int uid = w.genNewUId();
				w.pop("eax");
				w.c("test eax, eax");
				w.c("jnz _logor_" + uid + "_true");
				expr2.writeAsmCode(w);
				w.pop("eax");
				w.c("test eax, eax");
				w.c("jnz _logor_" + uid + "_true");
				w.c("mov eax, 0");
				w.c("jmp _logor_" + uid + "_end");
				w.l("_logor_" + uid + "_true");
				w.c("mov eax, 1");
				w.l("_logor_" + uid + "_end");
				w.push("eax");
				}
				break;
			case OP_AND:
				{
				expr1.writeAsmCode(w);
				w.t("Expression: &&");
				int uid = w.genNewUId();
				w.pop("eax");
				w.c("test eax, eax");
				w.c("jz _logand_" + uid + "_false");
				expr2.writeAsmCode(w);
				w.pop("eax");
				w.c("test eax, eax");
				w.c("jz _logand_" + uid + "_false");
				w.c("mov eax, 1");
				w.c("jmp _logand_" + uid + "_end");
				w.l("_logand_" + uid + "_false");
				w.c("mov eax, 0");
				w.l("_logand_" + uid + "_end");
				w.push("eax");
				}
				break;

			case OP_BIN_OR:
				writeAsmCodeForBinaryCountOp(w, "|", "or eax, ebx");
				break;
			case OP_BIN_XOR:
				writeAsmCodeForBinaryCountOp(w, "^", "xor eax, ebx");
				break;
			case OP_BIN_AND:
				writeAsmCodeForBinaryCountOp(w, "&", "and eax, ebx");
				break;

			case OP_EQ_EQ:
				writeAsmCodeForBinaryCmpOp(w, "==", "jz");
				break;
			case OP_NOT_EQ:
				writeAsmCodeForBinaryCmpOp(w, "!=", "jnz");
				break;
			case OP_LE_EQ:
				writeAsmCodeForBinaryCmpOp(w, "<=", "jle");
				break;
			case OP_GR_EQ:
				writeAsmCodeForBinaryCmpOp(w, ">=", "jge");
				break;
			case OP_LE:
				writeAsmCodeForBinaryCmpOp(w, "<", "jl");
				break;
			case OP_GR:
				writeAsmCodeForBinaryCmpOp(w, ">", "jg");
				break;

			case OP_SHL:
				writeAsmCodeForBinaryCountOp(w, ">>>", ASM_SHL);
				break;
			case OP_SHR:
				writeAsmCodeForBinaryCountOp(w, "<<<", ASM_SHR);
				break;

			case OP_PLUS:
				writeAsmCodeForBinaryCountOp(w, "+", "add eax, ebx");
				break;
			case OP_MINUS:
				writeAsmCodeForBinaryCountOp(w, "-", "sub eax, ebx");
				break;
			case OP_MULT:
				writeAsmCodeForBinaryCountOp(w, "*", "imul eax, ebx");
				break;
			case OP_DIV:
				writeAsmCodeForBinaryCountOp(w, "/", ASM_DIV);
				break;
			case OP_MOD:
				writeAsmCodeForBinaryCountOp(w, "%", ASM_MOD);
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
				{
				expr1.writeAsmCode(w);
				int uid = w.genNewUId();
				w.t("Expression: !");
				w.pop("eax");
				w.c("test eax, eax");
				w.c("jz _lognot_" + uid + "_false");
				w.c("mov eax, 1");
				w.c("jmp _lognot_" + uid + "_end");
				w.l("_lognot_" + uid + "_false");
				w.c("mov eax, 0");
				w.l("_lognot_" + uid + "_end");
				w.push("eax");
				}
				break;
			case OP_BIN_NOT:
				expr1.writeAsmCode(w);
				w.t("Expression: ~");
				w.pop("eax");
				w.c("not eax");
				w.push("eax");
				break;
		}
		w.ln();
	}
}
