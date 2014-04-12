package components;

import java.io.PrintWriter;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class VarDef implements CodeProvider {
	private Type type;
	private String name;
	private Expression defaultValue;

	private int uid;  // variable unique id

	public VarDef(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getChildCount() == 2 || tree.getChildCount() == 3);
		assert(tree.getChild(0).getType() == CommonCppWithStreamsLexer.TYPE);
		assert(tree.getChild(1).getType() == CommonCppWithStreamsLexer.NAME);

		type = TypeConverter.stringToType(tree.getChild(0).getText());
		ec.check(type != Type.VOID, tree.getLine(), "variable can not be void");
		name = tree.getChild(1).getText();
		st.declareVariable(name, type, this, tree.getLine());
		defaultValue = (tree.getChildCount() == 3) ? (new Expression(tree.getChild(2), ec, st)) : null;
		
		if (defaultValue != null) {
			ec.check(TypeChecker.canBeAssigned(type, defaultValue), tree.getLine(), "conversion of '" + TypeConverter.typeToString(type) + "' to '" + TypeConverter.typeToString(defaultValue.getType()) + "' is not possible");
			st.setVariableInitialized(name);
		}
	}

	public int getUId() {
		return uid;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void writeCppCode(PrintWriter w) {
		if (defaultValue != null) {
			w.print(TypeConverter.typeToString(type) + " " + name + " = ");
			defaultValue.writeCppCode(w);
		} else {
			w.print(TypeConverter.typeToString(type) + " " + name);
		}
	}

	public void writeAsmCode(AsmWriter w) {
		uid = w.genNewUId();
		if (defaultValue != null) {
			defaultValue.writeAsmCode(w);
			w.pop("eax");
		}
		w.setVariableSP(uid, w.push("eax"));
	}
}
