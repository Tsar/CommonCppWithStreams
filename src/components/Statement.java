package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Statement implements CodeProvider {
	private enum StatementType {
		VAR_DEF,
		STREAM_READ,
		STREAM_WRITE,
		EXPR,
		RETURN,
		FOR,
		WHILE,
		DOWHILE,
		IF,
		BREAK,
		CONTINUE,
		BLOCK
	}

	private StatementType statementType;
	
	private VarDef varDef;
	private Expression expr;
	private Block block;
	
	public Statement(Tree tree, ErrorsCollector ec, SymbolTable st) {
		varDef = null;
		expr = null;
		block = null;
		
		switch (tree.getType()) {
			case CommonCppWithStreamsLexer.VAR_DEF:
				statementType = StatementType.VAR_DEF;
				varDef = new VarDef(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.STREAM_READ:
				statementType = StatementType.STREAM_READ;
				// TODO
				return;
			case CommonCppWithStreamsLexer.STREAM_WRITE:
				statementType = StatementType.STREAM_WRITE;
				// TODO
				return;
			case CommonCppWithStreamsLexer.RETURN:
				statementType = StatementType.RETURN;
				// TODO
				return;
			case CommonCppWithStreamsLexer.FOR:
				statementType = StatementType.FOR;
				// TODO
				return;
			case CommonCppWithStreamsLexer.WHILE:
				statementType = StatementType.WHILE;
				// TODO
				return;
			case CommonCppWithStreamsLexer.DOWHILE:
				statementType = StatementType.DOWHILE;
				// TODO
				return;
			case CommonCppWithStreamsLexer.IF:
				statementType = StatementType.IF;
				// TODO
				return;
			case CommonCppWithStreamsLexer.BREAK:
				statementType = StatementType.BREAK;
				// TODO
				return;
			case CommonCppWithStreamsLexer.CONTINUE:
				statementType = StatementType.CONTINUE;
				// TODO
				return;
			case CommonCppWithStreamsLexer.BLOCK:
				statementType = StatementType.BLOCK;
				block = new Block(tree, ec, st, false);
				return;
		}

		statementType = StatementType.EXPR;
		expr = new Expression(tree, ec, st);
	}

	public void writeAsmCode(AsmWriter w) {
		switch (statementType) {
			case VAR_DEF:
				w.t("Statement: Variable Declaration");
				varDef.writeAsmCode(w);
				break;
			case EXPR:
				w.t("Statement: Expression");
				expr.writeAsmCode(w);
				w.pop("eax");
				break;
			case BLOCK:
				w.t("Statement: Block");
				block.writeAsmCode(w);
				break;
		}
	}
}
