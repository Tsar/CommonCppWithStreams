package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import components.Statement.StatementType;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class Block implements CodeProvider {
	private ArrayList<Statement> contents;
	private boolean functionBlock;

	private int initialSP;

	public Block(Tree tree, ErrorsCollector ec, SymbolTable st, boolean functionBlock) {
		assert(tree.getType() == CommonCppWithStreamsLexer.BLOCK);

		this.functionBlock = functionBlock;

		if (!functionBlock)
			st.beginBlock();
		contents = new ArrayList<Statement>();
		for (int i = 0; i < tree.getChildCount(); ++i) {
			Statement statement = new Statement(tree.getChild(i), ec, st);
			contents.add(statement);
			if (statement.getStatementType() == StatementType.RETURN)
				break;
		}
		if (!functionBlock)
			st.endBlock();
	}

	public boolean isFunctionBlock() {
		return functionBlock;
	}

	public void writeAsmCode(AsmWriter w) {
		w.blockStart(this);

		initialSP = w.getSP();
		if (!functionBlock) {
			w.t("Block");
		}
		for (Statement statement : contents) {
			statement.writeAsmCode(w);
		}
		if (!functionBlock) {
			w.t("cleaning stack of local block variables");
			while (w.getSP() != initialSP) {
				w.pop("esi");
			}
		}

		w.blockEnd();
	}

	// Following method is used by Return, Break and Continue
	public void writeAsmCodeToCleanLocalVariables(AsmWriter w) {
		int copyOfSP = w.getSP();
		w.t("cleaning stack of local block variables [for return, break or continue]");
		while (w.getSP() != initialSP) {
			w.pop("esi");
		}
		w.setSP(copyOfSP);
	}
}
