package components;

public class TypeConverter {
	public static Type stringToType(String typeName) {
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

	public static String typeToString(Type type) {
		switch (type) {
			case INT:
				return "int";
			case BOOL:
				return "bool";
			case VOID:
				return "void";
			case STREAM:
				return "Stream";
		}
		return "<undefined-type>";
	}
}
