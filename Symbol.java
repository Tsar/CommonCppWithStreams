public class Symbol {
    public String name;
    public DataType type;
    public boolean initialized;
    public FuncArgs funcArgs;
    public int blockId;

    public Symbol(String name, DataType type, boolean initialized, int blockId) {
        this.name = name;
        this.type = type;
        this.initialized = initialized;
        this.funcArgs = null;
        this.blockId = blockId;
    }

    public Symbol(String name, DataType type, FuncArgs funcArgs) {
        this.name = name;
        this.type = type;
        this.initialized = false;
        this.funcArgs = funcArgs;
    }
    
    public boolean isVariable() {
    	return type == DataType.INT_VARIABLE || type == DataType.BOOL_VARIABLE;
    }
    
    public boolean isFunction() {
    	return type == DataType.INT_FUNCTION || type == DataType.BOOL_FUNCTION || type == DataType.VOID_FUNCTION;
    }
    
    public boolean isStream() {
    	return type == DataType.INPUT_STREAM || type == DataType.OUTPUT_STREAM;
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
        case INPUT_STREAM:
            return "inputStream";
        case OUTPUT_STREAM:
            return "outputStream";
        }
        return "<???>";
    }

    public void _debug_printMe() {
        if (type == null) {
            System.out.format("%s\t| <null>\n", name);
        } else if (type == DataType.INT_VARIABLE || type == DataType.BOOL_VARIABLE) {
            System.out.format("%s\t| %s\t| %s\n", name, _debug_typeToText(type), initialized ? "initialized" : "not initialized");
        } else {
            System.out.format("%s\t| %s\t|", name, _debug_typeToText(type));
            for (FuncArg fa : funcArgs.arguments) {
                System.out.format(" (%s, %s, %s)", fa.name, _debug_typeToText(fa.type), fa.hasDefaultValue ? "has def val" : "no def val");
            }
            System.out.println();
        }
    }
}
