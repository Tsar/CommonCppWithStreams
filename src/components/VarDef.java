package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class VarDef {
	private TypeHolder typeHolder;
	private String name;
	private Expression defaultValue;
	
	public VarDef(Tree tree) {
		assert(tree.getChildCount() == 2 || tree.getChildCount() == 3);
		assert(tree.getChild(0).getType() == CommonCppWithStreamsLexer.TYPE);
		assert(tree.getChild(1).getType() == CommonCppWithStreamsLexer.NAME);

		typeHolder = new TypeHolder(tree.getChild(0).getText());
		name = tree.getChild(1).getText();

		if (tree.getChildCount() == 3) {
			defaultValue = new Expression(tree.getChild(2));
		}
	}
}
