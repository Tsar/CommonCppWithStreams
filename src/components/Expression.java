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

	private ExpressionType exprType;

	// if has NO children
	private int numberValue;
	private boolean boolValue;
	private String varName;
	
	// if has children
	private String fileName;
	private FunctionCall funcCall;
	
	private Expression expr1;
	private Expression expr2;
	
	private void oneSon(ExpressionType exprType, Tree tree, ErrorsCollector ec) {
		assert(tree.getChildCount() == 1);
		this.exprType = exprType;
		expr1 = new Expression(tree.getChild(0), ec);
	}
	
	private void twoSons(ExpressionType exprType, Tree tree, ErrorsCollector ec) {
		assert(tree.getChildCount() == 2);
		this.exprType = exprType;
		expr1 = new Expression(tree.getChild(0), ec);
		expr2 = new Expression(tree.getChild(1), ec);
	}

	public Expression(Tree tree, ErrorsCollector ec) {
		if (tree.getChildCount() == 0) {
			if (tree.getType() == CommonCppWithStreamsLexer.NUMBER) {
				exprType = ExpressionType.NUMBER_VALUE;
				numberValue = Integer.parseInt(tree.getText());
			} else if (tree.getType() == CommonCppWithStreamsLexer.BOOL_VALUE) {
				exprType = ExpressionType.BOOL_VALUE;
				boolValue = tree.getText().equals("true");
			} else if (tree.getType() == CommonCppWithStreamsLexer.NAME) {
				exprType = ExpressionType.VARIABLE;
				varName = tree.getText();
			} else if (tree.getType() == CommonCppWithStreamsLexer.STREAM_FUNC) {
				if (tree.getText().equals("InputStream")) {
					exprType = ExpressionType.INPUT_STREAM_FUNC;
				} else if (tree.getText().equals("OutputStream")) {
					exprType = ExpressionType.OUTPUT_STREAM_FUNC;
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

				fileName = tree.getChild(0).getText();  // TODO: remove ""
				if (tree.getText().equals("InputFileStream")) {
					exprType = ExpressionType.INPUT_FILE_STREAM_FUNC;
				} else if (tree.getText().equals("OutputFileStream")) {
					exprType = ExpressionType.OUTPUT_FILE_STREAM_FUNC;
				} else if (tree.getText().equals("InputBinaryFileStream")) {
					exprType = ExpressionType.INPUT_BINARY_FILE_STREAM_FUNC;
				} else if (tree.getText().equals("OutputBinaryFileStream")) {
					exprType = ExpressionType.OUTPUT_BINARY_FILE_STREAM_FUNC;
				} else {
					assert(false);
				}
			} else if (tree.getType() == CommonCppWithStreamsLexer.CALL) {
				exprType = ExpressionType.FUNCTION_CALL;
				funcCall = new FunctionCall(tree, ec);  // TODO: check type (here?)

			} else if (tree.getType() == CommonCppWithStreamsLexer.POSTFIX_PP) {
				oneSon(ExpressionType.OP_POSTFIX_PP, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as increment operand");

			} else if (tree.getType() == CommonCppWithStreamsLexer.POSTFIX_MM) {
				oneSon(ExpressionType.OP_POSTFIX_MM, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as decrement operand");

			} else if (tree.getText().equals("=")) {
				twoSons(ExpressionType.OP_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '=' operand");

			} else if (tree.getText().equals("*=")) {
				twoSons(ExpressionType.OP_MULT_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '*=' operand");

			} else if (tree.getText().equals("/=")) {
				twoSons(ExpressionType.OP_DIV_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '/=' operand");

			} else if (tree.getText().equals("%=")) {
				twoSons(ExpressionType.OP_MOD_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '%=' operand");

			} else if (tree.getText().equals("+=")) {
				twoSons(ExpressionType.OP_PLUS_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '+=' operand");

			} else if (tree.getText().equals("-=")) {
				twoSons(ExpressionType.OP_MINUS_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '-=' operand");

			} else if (tree.getText().equals(">>=")) {
				twoSons(ExpressionType.OP_SHR_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '>>=' operand");

			} else if (tree.getText().equals("<<=")) {
				twoSons(ExpressionType.OP_SHL_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '<<=' operand");

			} else if (tree.getText().equals("&=")) {
				twoSons(ExpressionType.OP_AND_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '&=' operand");

			} else if (tree.getText().equals("^=")) {
				twoSons(ExpressionType.OP_XOR_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '^=' operand");

			} else if (tree.getText().equals("|=")) {
				twoSons(ExpressionType.OP_OR_EQ, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as left '|=' operand");

			} else if (tree.getText().equals("||")) {
				twoSons(ExpressionType.OP_OR, tree, ec);

			} else if (tree.getText().equals("&&")) {
				twoSons(ExpressionType.OP_AND, tree, ec);

			} else if (tree.getText().equals("|")) {
				twoSons(ExpressionType.OP_BIN_OR, tree, ec);

			} else if (tree.getText().equals("^")) {
				twoSons(ExpressionType.OP_BIN_XOR, tree, ec);

			} else if (tree.getText().equals("&")) {
				twoSons(ExpressionType.OP_BIN_AND, tree, ec);

			} else if (tree.getText().equals("==")) {
				twoSons(ExpressionType.OP_EQ_EQ, tree, ec);
			} else if (tree.getText().equals("!=")) {
				twoSons(ExpressionType.OP_NOT_EQ, tree, ec);

			} else if (tree.getText().equals("<=")) {
				twoSons(ExpressionType.OP_LE_EQ, tree, ec);
			} else if (tree.getText().equals(">=")) {
				twoSons(ExpressionType.OP_GR_EQ, tree, ec);
			} else if (tree.getText().equals("<")) {
				twoSons(ExpressionType.OP_LE, tree, ec);
			} else if (tree.getText().equals(">")) {
				twoSons(ExpressionType.OP_GR, tree, ec);

			} else if (tree.getText().equals("<<")) {
				twoSons(ExpressionType.OP_SHL, tree, ec);
			} else if (tree.getText().equals(">>")) {
				twoSons(ExpressionType.OP_SHR, tree, ec);

			} else if (tree.getText().equals("+")) {
				if (tree.getChildCount() == 1) {
					oneSon(ExpressionType.OP_UNARY_PLUS, tree, ec);
				} else {
					twoSons(ExpressionType.OP_PLUS, tree, ec);
				}
			} else if (tree.getText().equals("-")) {
				if (tree.getChildCount() == 1) {
					oneSon(ExpressionType.OP_UNARY_MINUS, tree, ec);
				} else {
					twoSons(ExpressionType.OP_MINUS, tree, ec);
				}

			} else if (tree.getText().equals("*")) {
				twoSons(ExpressionType.OP_MULT, tree, ec);
			} else if (tree.getText().equals("/")) {
				twoSons(ExpressionType.OP_DIV, tree, ec);
			} else if (tree.getText().equals("%")) {
				twoSons(ExpressionType.OP_MOD, tree, ec);

			} else if (tree.getText().equals("++")) {
				oneSon(ExpressionType.OP_PREFIX_PP, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as increment operand");
			} else if (tree.getText().equals("--")) {
				oneSon(ExpressionType.OP_PREFIX_MM, tree, ec);
				ec.check(expr1.isLValue(), tree.getLine(), "lvalue required as increment operand");
			} else if (tree.getText().equals("!")) {
				oneSon(ExpressionType.OP_NOT, tree, ec);
			} else if (tree.getText().equals("~")) {
				oneSon(ExpressionType.OP_BIN_NOT, tree, ec);

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
}
