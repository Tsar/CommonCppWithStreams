package components;

import java.io.PrintWriter;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Function implements CodeProvider {
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
		
		type = TypeConverter.stringToType(tree.getChild(0).getText());
		name = tree.getChild(1).getText();
		args = new ArgumentsDef(tree.getChild(2), ec);
		block = new Block(tree.getChild(3), ec);
	}

	public void writeCppCode(PrintWriter w) {
		w.print(TypeConverter.typeToString(type) + " " + name + "(");
		args.writeCppCode(w);
		w.println(") {");
		block.writeCppCode(w);
		w.println("}");
	}
}
