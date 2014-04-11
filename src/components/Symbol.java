package components;

public class Symbol {
	private String name;
	private Type type;
	private boolean initialized;

	public Symbol(String name, Type type, boolean initialized) {
		this.name = name;
		this.type = type;
		this.initialized = initialized;
	}

	public Type getType() {
		return type;
	}

	public void setInitialized() {
		initialized = true;
	}
}
