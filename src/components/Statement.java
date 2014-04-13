package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class Statement implements CodeProvider {
	public enum StatementType {
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
	private Return ret;
	private Expression expr;
	private If if_;
	private Block block;
	
	public Statement(Tree tree, ErrorsCollector ec, SymbolTable st) {
		varDef = null;
		ret = null;
		expr = null;
		if_ = null;
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
				ret = new Return(tree, ec, st);
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
				if_ = new If(tree, ec, st);
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

	public StatementType getStatementType() {
		return statementType;
	}

	public void writeAsmCode(AsmWriter w) {
		switch (statementType) {
			case VAR_DEF:
				w.t("Statement: Variable Declaration");
				varDef.writeAsmCode(w);
				break;
			case RETURN:
				w.t("Statement: Return");
				ret.writeAsmCode(w);
				break;
			case EXPR:
				w.t("Statement: Expression");
				expr.writeAsmCode(w);
				w.pop("eax");
				break;
			case FOR:
				// TODO
				break;
			case WHILE:
				// TODO
				break;
			case DOWHILE:
				// TODO
				break;
			case IF:
				w.t("Statement: If");
				if_.writeAsmCode(w);
				break;
			case BREAK:
				// TODO
				break;
			case CONTINUE:
				// TODO
				break;
			case BLOCK:
				w.t("Statement: Block");
				block.writeAsmCode(w);
				break;
		}
	}
}
