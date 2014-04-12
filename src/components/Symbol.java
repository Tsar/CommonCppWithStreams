package components;

public class Symbol {
	private String name;
	private Type type;
	private boolean initialized;

	private boolean variable;

	public Symbol(String name, Type type, boolean initialized) {
		variable = true;

		this.name = name;
		this.type = type;
		this.initialized = initialized;
	}

	public Symbol(String name, Type type/*, something else*/) {
		variable = false;

		this.name = name;
		this.type = type;
	}

	public Type getType() {
		return type;
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
