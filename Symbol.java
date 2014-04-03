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
}
