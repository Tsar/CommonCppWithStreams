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
		lm.remove(cb);
	}

	public void declareVariable(String name, Type type, int lineNumber) {
		int cb = currentBlockNumber();
		if (lm.get(cb).containsKey(name)) {
			ec.check(false, lineNumber, "redeclaration of '" + name + "' in the same block");
			return;
		}
		lm.get(cb).put(name, new Symbol(name, type, false));
	}

	public void setVariableInitialized(String name) {
		for (int i = currentBlockNumber(); i >= 0; --i) {
			if (lm.get(i).containsKey(name)) {
				lm.get(i).get(name).setInitialized();
				return;
			}
		}
		assert(false);
	}

	public Type getVariableType(String name) {
		for (int i = currentBlockNumber(); i >= 0; --i) {
			if (lm.get(i).containsKey(name)) {
				return lm.get(i).get(name).getType();
			}
		}
		return null;
	}

	public void declareFunction(String name, Type type, int lineNumber) {
	}
}
