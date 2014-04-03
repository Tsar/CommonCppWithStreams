public class Symbol {
    public String name;
    public DataType type;
    public boolean initialized;

    public Symbol(String name, DataType type, boolean initialized) {
        this.name = name;
        this.type = type;
        this.initialized = initialized;
    }
}
