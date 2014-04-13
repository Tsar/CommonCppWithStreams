package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class Block implements CodeProvider {
	private ArrayList<Statement> contents;
	private boolean functionBlock;

	public Block(Tree tree, ErrorsCollector ec, SymbolTable st, boolean functionBlock) {
		assert(tree.getType() == CommonCppWithStreamsLexer.BLOCK);

		this.functionBlock = functionBlock;

		if (!functionBlock)
			st.beginBlock();
		contents = new ArrayList<Statement>();
		for (int i = 0; i < tree.getChildCount(); ++i) {
			contents.add(new Statement(tree.getChild(i), ec, st));
		}
		if (!functionBlock)
			st.endBlock();
	}

	public void writeAsmCode(AsmWriter w) {
		int initialSP = w.getSP();
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
	}
}
