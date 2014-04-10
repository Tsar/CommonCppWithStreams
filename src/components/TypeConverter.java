package components;

public class TypeConverter {
	public static Type typeFromString(String typeName) {
		if (typeName.equals("int")) {
			return Type.INT;
		} else if (typeName.equals("bool")) {
			return Type.BOOL;
		} else if (typeName.equals("void")) {
			return Type.VOID;
		} else if (typeName.equals("Stream")) {
			return Type.STREAM;
		} else {
			assert(false);
		}
		return null;
	}
}
