package components;

import java.util.*;

public class SymbolTable {
	private List<Map<String, Symbol>> lm;
	private ErrorsCollector ec;

	public SymbolTable(ErrorsCollector ec) {
		this.ec = ec;
		lm = new ArrayList<Map<String, Symbol>>();
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
		
		Iterator<Symbol> it = lm.get(cb).values().iterator();
		while (it.hasNext()) {
			Symbol s = it.next();
			// TODO: VarDef.deinit(s.getName());
		}
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

	public void declareFunction(String name, Type type, int lineNumber) {
		assert(currentBlockNumber() == 0);

		if (lm.get(0).containsKey(name)) {
			ec.check(false, lineNumber, "redeclaration of '" + name + "' in the same block");
			return;
		}
		lm.get(0).put(name, new Symbol(name, type));
	}

	public Type referenceFunctionAndGetType(String name, int lineNumber) {
		if (lm.get(0).containsKey(name) && lm.get(0).get(name).isFunction()) {
			return lm.get(0).get(name).getType();
		}
		ec.check(false, lineNumber, "undefined function " + name);
		return null;
	}
}
