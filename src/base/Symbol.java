package base;

import components.Function;
import components.VarDef;

public class Symbol {
	private String name;
	private Type type;

	private boolean variable;

	// if variable
	private boolean initialized;
	private VarDef varDef;

	// if function
	private Function funcDef;

	public Symbol(String name, Type type, VarDef varDef, boolean initialized) {
		variable = true;
		funcDef = null;

		this.name = name;
		this.type = type;
		this.varDef = varDef;
		this.initialized = initialized;
	}

	public Symbol(String name, Type type, Function funcDef) {
		variable = false;
		varDef = null;

		this.name = name;
		this.type = type;
		this.funcDef = funcDef;
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
	
	public Function getFuncDef() {
		return funcDef;
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
