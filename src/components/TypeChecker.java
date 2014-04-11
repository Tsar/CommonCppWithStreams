package components;

public class TypeChecker {
	public static boolean canBeAssigned(Type lvalueType, Type exprType) {
		return lvalueType == exprType ||
				(lvalueType == Type.BOOL && exprType == Type.INT) ||
				(lvalueType == Type.INT && exprType == Type.BOOL);
	}
}
