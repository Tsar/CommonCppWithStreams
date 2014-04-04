import java.util.*;

public class SymbolTable {
    private CommonCppWithStreamsParser parser;
    private Map<String, Symbol> symbolTable;
    private Stack<Integer> blockStack;
    private int curBlockId;
    private int nextBlockId;

    private List<String> _debug_symbolList;

    public SymbolTable(CommonCppWithStreamsParser parser) {
        this.parser = parser;
        symbolTable = new HashMap<String, Symbol>();
        blockStack = new Stack<Integer>();
        curBlockId = 0;
        nextBlockId = 1;

        _debug_symbolList = new ArrayList<String>();
    }

    public void _debug_output() {
    	System.out.println("=== SYMBOL TABLE ===");
        for (String name : _debug_symbolList) {
            symbolTable.get(name)._debug_printMe();
        }
    }
    
    public void blockStarted() {
    	curBlockId = nextBlockId++;
    	blockStack.push(curBlockId);
    }

    public void blockFinished() {
    	if (blockStack.empty()) {
    		// Impossible situation if parser works correctly
    		assert(false);
    		return;
    	}
    	blockStack.pop();
    	curBlockId = blockStack.empty() ? 0 : blockStack.peek();
    }

    public void declareVariable(String name, String typeStr, boolean initialized, int lineNumber) {
        DataType type = parser.getVariableType(typeStr, lineNumber);
        if (symbolTable.containsKey(name)) {
            parser.addCompilationError(lineNumber, "Identifier '" + name + "' is already used");
        }
        symbolTable.put(name, new Symbol(name, type, initialized, curBlockId));
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

    public void checkVariableDeclared(String name, int lineNumber) {
    	if (!symbolTable.containsKey(name)) {
            parser.addCompilationError(lineNumber, "Identifier '" + name + "' not declared");
            return;
        }
    	if (!symbolTable.get(name).isVariable()) {
    		parser.addCompilationError(lineNumber, "Identifier '" + name + "' is not a variable");
            return;
    	}
    }
    
    public void checkFunctionCall(String name, int lineNumber) {
    }
}
