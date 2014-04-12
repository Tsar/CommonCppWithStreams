package components;

public class TypeConverter {
	public static Type stringToType(String typeName) {
		if (typeName.equals("int")) {
			return Type.INT;
		} else if (typeName.equals("bool")) {
			return Type.BOOL;
		} else if (typeName.equals("void")) {
			return Type.VOID;
		} else if (typeName.equals("IStream")) {
			return Type.ISTREAM;
		} else if (typeName.equals("OStream")) {
			return Type.OSTREAM;
		} else {
			assert(false);
		}
		return null;
	}

	public static String typeToString(Type type) {
		if (type == null) {
			return "<undefined-type>";
		}
		switch (type) {
			case INT:
				return "int";
			case BOOL:
				return "bool";
			case VOID:
				return "void";
			case ISTREAM:
				return "IStream";
			case OSTREAM:
				return "OStream";
		}
		return "<undefined-type>";
	}

	public static String typeToString(Expression expr) {
		return typeToString(expr.getType());
	}
}
