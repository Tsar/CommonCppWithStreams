package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.Type;
import base.TypeChecker;
import base.TypeConverter;

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

	public boolean hasDefaultValue() {
		return defaultValue != null;
	}

	public Expression getDefaultValue() {
		return defaultValue;
	}

	public int getUId() {
		return uid;
	}

	public void setUId(int uid) {
		this.uid = uid;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void writeAsmCode(AsmWriter w) {
		uid = w.genNewUId();
		if (defaultValue != null) {
			defaultValue.writeAsmCode(w);
			w.pop("eax");
		}
		w.t("Variable Declaration: " + name);
		w.setVariableSP(uid, w.push("eax"));
	}
}
