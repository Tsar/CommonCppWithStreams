package components;

public class Symbol {
	private String name;
	private Type type;

	private boolean variable;

	// if variable
	private boolean initialized;
	private VarDef varDef;

	public Symbol(String name, Type type, VarDef varDef, boolean initialized) {
		variable = true;

		this.name = name;
		this.type = type;
		this.varDef = varDef;
		this.initialized = initialized;
	}

	public Symbol(String name, Type type/*, something else*/) {
		variable = false;
		varDef = null;

		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public VarDef getVarDef() {
		return varDef;
	}
	
	public boolean isVariable() {
		return variable;
	}

	public boolean isFunction() {
		return !isVariable();
	}

	public void setInitialized() {
		initialized = true;
	}

	public boolean isInitialized() {
		return initialized;
	}
}
