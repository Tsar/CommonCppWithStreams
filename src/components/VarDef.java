package components;

import java.io.PrintWriter;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class VarDef implements CodeProvider {
	private Type type;
	private String name;
	private Expression defaultValue;
	
	public VarDef(Tree tree, ErrorsCollector ec) {
		assert(tree.getChildCount() == 2 || tree.getChildCount() == 3);
		assert(tree.getChild(0).getType() == CommonCppWithStreamsLexer.TYPE);
		assert(tree.getChild(1).getType() == CommonCppWithStreamsLexer.NAME);

		type = TypeConverter.stringToType(tree.getChild(0).getText());
		ec.check(type != Type.VOID, tree.getLine(), "variable can not be void");
		name = tree.getChild(1).getText();
		defaultValue = (tree.getChildCount() == 3) ? (new Expression(tree.getChild(2), ec)) : null;
	}

	public void writeCppCode(PrintWriter w) {
		if (defaultValue != null) {
			w.print(TypeConverter.typeToString(type) + " " + name + " = ");
			defaultValue.writeCppCode(w);
		} else {
			w.print(TypeConverter.typeToString(type) + " " + name);
		}
	}
}
