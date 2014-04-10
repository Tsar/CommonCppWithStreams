package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class VarDef {
	private Type type;
	private String name;
	private Expression defaultValue;
	
	public VarDef(Tree tree, ErrorsCollector ec) {
		assert(tree.getChildCount() == 2 || tree.getChildCount() == 3);
		assert(tree.getChild(0).getType() == CommonCppWithStreamsLexer.TYPE);
		assert(tree.getChild(1).getType() == CommonCppWithStreamsLexer.NAME);

		type = TypeConverter.typeFromString(tree.getChild(0).getText());
		ec.check(type != Type.VOID, tree.getLine(), "variable can not be void");
		name = tree.getChild(1).getText();

		if (tree.getChildCount() == 3) {
			defaultValue = new Expression(tree.getChild(2), ec);
		}
	}
}
