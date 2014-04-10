package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Function {
	private Type type;
	private String name;
	private ArgumentsDef args;
	private Block block;
	
	public Function(Tree tree, ErrorsCollector ec) {
		assert(tree.getChildCount() == 4);
		assert(tree.getChild(0).getType() == CommonCppWithStreamsLexer.TYPE);
		assert(tree.getChild(1).getType() == CommonCppWithStreamsLexer.NAME);
		assert(tree.getChild(2).getType() == CommonCppWithStreamsLexer.ARGS);
		assert(tree.getChild(3).getType() == CommonCppWithStreamsLexer.BLOCK);
		
		type = TypeConverter.typeFromString(tree.getChild(0).getText());
		name = tree.getChild(1).getText();
		args = new ArgumentsDef(tree.getChild(2), ec);
		block = new Block(tree.getChild(3), ec);
	}
}
