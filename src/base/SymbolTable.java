package base;

import java.util.*;

import components.Function;
import components.VarDef;

public class SymbolTable {
	private List<Map<String, Symbol>> lm;
	private ErrorsCollector ec;

	private boolean mainFuncDefined;

	public SymbolTable(ErrorsCollector ec) {
		this.ec = ec;
		lm = new ArrayList<Map<String, Symbol>>();
		mainFuncDefined = false;
	}

	private int currentBlockNumber() {
		return lm.size() - 1;
	}

	public void beginBlock() {
		lm.add(new LinkedHashMap<String, Symbol>());
	}

	public void endBlock() {
		int cb = currentBlockNumber();
		assert(cb >= 0);

		lm.remove(cb);
	}

	public void declareVariable(String name, Type type, VarDef varDef, int lineNumber) {
		int cb = currentBlockNumber();
		if (lm.get(cb).containsKey(name)) {
			ec.check(false, lineNumber, "redeclaration of '" + name + "' in the same block");
			return;
		}
		lm.get(cb).put(name, new Symbol(name, type, varDef, false));
	}

	public VarDef referenceVariableAndGetVarDef(String name, boolean checkForInitialized, int lineNumber) {
		for (int i = currentBlockNumber(); i >= 0; --i) {
			if (lm.get(i).containsKey(name) && lm.get(i).get(name).isVariable()) {
				if (checkForInitialized) {
					ec.warnIfNot(lm.get(i).get(name).isInitialized(), lineNumber, "using value of uninitialized variable '" + name + "'");
				}
				return lm.get(i).get(name).getVarDef();
			}
		}
		ec.check(false, lineNumber, "undefined variable " + name);
		return null;
	}

	public void setVariableInitialized(String name) {
		for (int i = currentBlockNumber(); i >= 0; --i) {
			if (lm.get(i).containsKey(name) && lm.get(i).get(name).isVariable()) {
				lm.get(i).get(name).setInitialized();
				return;
			}
		}
		assert(false);
	}

	public void declareFunction(String name, Type type, Function funcDef, int lineNumber) {
		assert(currentBlockNumber() == 0);

		if (lm.get(0).containsKey(name)) {
			ec.check(false, lineNumber, "redeclaration of '" + name + "' in the same block");
			return;
		}
		lm.get(0).put(name, new Symbol(name, type, funcDef));

		if (!mainFuncDefined && name.equals("main")) {
			mainFuncDefined = true;
			ec.check(type == Type.INT || type == Type.VOID, lineNumber, "function 'main' can not return '" + TypeConverter.typeToString(type) + "'");
		}
	}

	public Function referenceFunctionAndGetIt(String name, int lineNumber) {
		if (lm.get(0).containsKey(name) && lm.get(0).get(name).isFunction()) {
			return lm.get(0).get(name).getFuncDef();
		}
		ec.check(false, lineNumber, "undefined function " + name);
		return null;
	}

	public boolean isMainFuncDefined() {
		return mainFuncDefined;
	}
}
