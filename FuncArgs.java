import java.util.*;

public class FuncArgs {
    private CommonCppWithStreamsParser parser;
    private List<FuncArg> arguments; 
    
    public FuncArgs(CommonCppWithStreamsParser parser) {
        this.parser = parser;
        arguments = new ArrayList<FuncArg>();
    }

    // TODO: default value
    public void add(String name, String typeStr) {
        arguments.add(new FuncArg(name, parser.getVariableType(typeStr)));
    }
}
