import java.util.*;

public class SymbolTable {
    private CommonCppWithStreamsParser parser;
    private Map<String, Symbol> symbolTable;
    private List<String> _debug_symbolList;

    public SymbolTable(CommonCppWithStreamsParser parser) {
        this.parser = parser;
        symbolTable = new HashMap<String, Symbol>();
        _debug_symbolList = new ArrayList<String>();
    }
    
    private String _debug_typeToText(DataType type) {
    	if (type == null) {
    		return "<null>";
    	}
    	switch (type) {
    	case INT_FUNCTION:
    		return "intF";
    	case BOOL_FUNCTION:
    		return "boolF";
    	case VOID_FUNCTION:
    		return "voidF";
    	case INT_VARIABLE:
    		return "intV";
    	case BOOL_VARIABLE:
    		return "boolV";
    	}
    	return "<???>";
    }

    public void _debug_output() {
    	System.out.println("=== SYMBOL TABLE ===");
    	for (String name : _debug_symbolList) {
    		System.out.format("%s\t| %s\t| %s\n", name, _debug_typeToText(symbolTable.get(name).type), symbolTable.get(name).initialized ? "true" : "false");
    	}
    }

    public void declareVariable(String name, String typeStr, boolean initialized) {
        DataType type = parser.getVariableType(typeStr);
        if (symbolTable.containsKey(name)) {
            parser.addCompilationError("Identifier '" + name + "' is already used");
        }
        symbolTable.put(name, new Symbol(name, type, initialized));
        _debug_symbolList.add(name);
    }

    public void declareFunction(String name, String typeStr, FuncArgs funcArgs) {
        DataType type = parser.getFunctionType(typeStr);
        if (symbolTable.containsKey(name)) {
            parser.addCompilationError("Identifier '" + name + "' is already used");
        }
        symbolTable.put(name, new Symbol(name, type, funcArgs));
        _debug_symbolList.add(name);
    }
}
