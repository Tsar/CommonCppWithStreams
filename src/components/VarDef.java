package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class VarDef {
	public VarDef(Tree tree) {
		assert(tree.getChildCount() == 2 || tree.getChildCount() == 3);
		assert(tree.getChild(0).getType() == CommonCppWithStreamsLexer.TYPE);
		assert(tree.getChild(1).getType() == CommonCppWithStreamsLexer.NAME);
		if (tree.getChildCount() == 3) {
		}
	}
}
