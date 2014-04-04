public class Symbol {
    public String name;
    public DataType type;
    public boolean initialized;
    public FuncArgs funcArgs;

    public Symbol(String name, DataType type, boolean initialized) {
        this.name = name;
        this.type = type;
        this.initialized = initialized;
        this.funcArgs = null;
    }

    public Symbol(String name, DataType type, FuncArgs funcArgs) {
        this.name = name;
        this.type = type;
        this.initialized = false;
        this.funcArgs = funcArgs;
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
