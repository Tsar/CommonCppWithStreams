package components;

public class TypeHolder {
	private Type type;
	
	public TypeHolder(String typeName) {
		if (typeName.equals("int")) {
			type = Type.INT;
		} else if (typeName.equals("bool")) {
			type = Type.BOOL;
		} else if (typeName.equals("void")) {
			type = Type.VOID;
		} else if (typeName.equals("Stream")) {
			type = Type.STREAM;
		} else {
			assert(false);
		}
	}
	
	public Type getType() {
		return type;
	}
}
