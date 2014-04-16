package base;

import components.Expression;
import components.VarDef;

public class TypeChecker {
	public static boolean canBeAssigned(Type lvalueType, Type exprType) {
		if (lvalueType == Type.VOID || exprType == Type.VOID)
			return false;
		return lvalueType == exprType ||
				(lvalueType == Type.BOOL && exprType == Type.INT) ||
				(lvalueType == Type.INT && exprType == Type.BOOL);
	}

	public static boolean canBeAssigned(Expression lvalue, Expression expr) {
		return canBeAssigned(lvalue.getType(), expr.getType());
	}

	public static boolean canBeAssigned(Type lvalueType, Expression expr) {
		return canBeAssigned(lvalueType, expr.getType());
	}
	
	public static boolean canBeAssigned(Expression lvalue, Type exprType) {
		return canBeAssigned(lvalue.getType(), exprType);
	}

	public static boolean canBeAssigned(VarDef lvalue, Expression expr) {
		return canBeAssigned(lvalue.getType(), expr.getType());
	}

	public static boolean isIntOrBool(Type type) {
		return type == Type.INT || type == Type.BOOL;
	}

	public static boolean isIntOrBool(Expression expr) {
		return isIntOrBool(expr.getType());
	}

	public static boolean isIntOrBool(VarDef varDef) {
		return isIntOrBool(varDef.getType());
	}
}
