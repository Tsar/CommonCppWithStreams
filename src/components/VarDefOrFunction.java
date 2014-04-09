package components;

public class VarDefOrFunction {
	private VarDef varDef;
	private Function function;

	public VarDefOrFunction(VarDef varDef) {
		this.varDef = varDef;
		this.function = null;
	}

	public VarDefOrFunction(Function function) {
		this.varDef = null;
		this.function = function;
	}

	public boolean isVarDef() {
		return varDef != null;
	}

	public boolean isFunction() {
		return !isVarDef();
	}
	
	public VarDef getVarDef() {
		return varDef;
	}
	
	public Function getFunction() {
		return function;
	}
	
	public Object get() {
		return isVarDef() ? varDef : function;
	}
}
