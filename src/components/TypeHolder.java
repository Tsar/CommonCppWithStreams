package components;

public class TypeHolder {
	public enum Type {
		INT,
		BOOL,
		VOID,
		STREAM
	}
	
	private Type type;
	
	public TypeHolder(String typeName) {
		if (typeName == "int") {
			type = Type.INT;
		} else if (typeName == "bool") {
			type = Type.BOOL;
		} else if (typeName == "void") {
			type = Type.VOID;
		} else if (typeName == "stream") {
			type = Type.STREAM;
		} else {
			assert(false);
		}
	}
	
	public Type getType() {
		return type;
	}
}
