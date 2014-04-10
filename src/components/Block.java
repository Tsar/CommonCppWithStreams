package components;

import java.io.PrintWriter;
import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Block implements CodeProvider {
	private ArrayList<Statement> contents;
	
	public Block(Tree tree, ErrorsCollector ec) {
		assert(tree.getType() == CommonCppWithStreamsLexer.BLOCK);

		contents = new ArrayList<Statement>();
		for (int i = 0; i < tree.getChildCount(); ++i) {
			contents.add(new Statement(tree.getChild(i), ec));
		}
	}

	public void writeCppCode(PrintWriter w) {
		for (Statement st : contents) {
			st.writeCppCode(w);
		}
	}
}
