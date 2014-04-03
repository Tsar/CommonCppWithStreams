import java.util.*;

public class FuncArgs {
    private CommonCppWithStreamsParser parser;
    public List<FuncArg> arguments; 
    
    public FuncArgs(CommonCppWithStreamsParser parser) {
        this.parser = parser;
        arguments = new ArrayList<FuncArg>();
    }

    public void add(String name, String typeStr, boolean hasDefaultValue) {
        arguments.add(new FuncArg(name, parser.getVariableType(typeStr), hasDefaultValue));
    }
}
