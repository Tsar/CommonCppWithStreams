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

    public void _debug_output() {
    	System.out.println("=== SYMBOL TABLE ===");
        for (String name : _debug_symbolList) {
            symbolTable.get(name)._debug_printMe();
        }
    }

    public void declareVariable(String name, String typeStr, boolean initialized, int lineNumber) {
        DataType type = parser.getVariableType(typeStr, lineNumber);
        if (symbolTable.containsKey(name)) {
            parser.addCompilationError(lineNumber, "Identifier '" + name + "' is already used");
        }
        symbolTable.put(name, new Symbol(name, type, initialized));
        _debug_symbolList.add(name);
    }

    public void declareFunction(String name, String typeStr, FuncArgs funcArgs, int lineNumber) {
        DataType type = parser.getFunctionType(typeStr, lineNumber);
        if (symbolTable.containsKey(name)) {
            parser.addCompilationError(lineNumber, "Identifier '" + name + "' is already used");
        }
        symbolTable.put(name, new Symbol(name, type, funcArgs));
        _debug_symbolList.add(name);
    }
}
