import org.antlr.runtime.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

public class CommonCppWithStreamsCompiler {
    public static void main(String[] args) {
        for (String fileName : args) {
            try {
                ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(new File(fileName)));
                CommonCppWithStreamsLexer lexer = new CommonCppWithStreamsLexer(input);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                CommonCppWithStreamsParser parser = new CommonCppWithStreamsParser(tokens);
                parser.s();
                PrintWriter out = new PrintWriter(fileName + ".res");
                out.print(parser.getCode());
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
