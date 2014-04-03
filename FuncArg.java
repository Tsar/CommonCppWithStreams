public class FuncArg {
    public String name;
    public DataType type;
    public boolean hasDefaultValue;

    public FuncArg(String name, DataType type, boolean hasDefaultValue) {
        this.name = name;
        this.type = type;
        this.hasDefaultValue = hasDefaultValue;
    }
}
