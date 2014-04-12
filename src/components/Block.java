package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Block implements CodeProvider {
	private ArrayList<Statement> contents;

	public Block(Tree tree, ErrorsCollector ec, SymbolTable st, boolean functionBlock) {
		assert(tree.getType() == CommonCppWithStreamsLexer.BLOCK);

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
		for (Statement statement : contents) {
			statement.writeAsmCode(w);
		}
	}
}
