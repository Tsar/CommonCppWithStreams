package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class Statement implements CodeProvider {
	public enum StatementType {
		EXPR,
		RETURN,
		OTHER
	}

	private StatementType statementType;
	private CodeProvider statement;

	public Statement(Tree tree, ErrorsCollector ec, SymbolTable st) {
		switch (tree.getType()) {
			case CommonCppWithStreamsLexer.VAR_DEF:
				statementType = StatementType.OTHER;
				statement = new VarDef(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.STREAM_READ:
				statementType = StatementType.OTHER;
				statement = new StreamRead(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.STREAM_WRITE:
				statementType = StatementType.OTHER;
				statement = new StreamWrite(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.RETURN:
				statementType = StatementType.RETURN;
				statement = new Return(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.WHILE:
				statementType = StatementType.OTHER;
				statement = new While(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.DOWHILE:
				statementType = StatementType.OTHER;
				statement = new DoWhile(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.FOR:
				statementType = StatementType.OTHER;
				statement = new For(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.IF:
				statementType = StatementType.OTHER;
				statement = new If(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.BREAK:
				statementType = StatementType.OTHER;
				statement = new Break(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.CONTINUE:
				statementType = StatementType.OTHER;
				statement = new Continue(tree, ec, st);
				return;
			case CommonCppWithStreamsLexer.BLOCK:
				statementType = StatementType.OTHER;
				statement = new Block(tree, ec, st, false);
				return;
		}

		statementType = StatementType.EXPR;
		statement = new Expression(tree, ec, st);
	}

	public StatementType getStatementType() {
		return statementType;
	}

	public void writeAsmCode(AsmWriter w) {
		statement.writeAsmCode(w);
		if (statementType == StatementType.EXPR) {
			w.pop("eax");
		}
	}
}
