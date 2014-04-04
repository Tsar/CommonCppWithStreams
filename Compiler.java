import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

public class Compiler {
    public static void main(String[] args) {
        for (String fileName : args) {
            try {
                ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(new File(fileName)));
                CommonCppWithStreamsLexer lexer = new CommonCppWithStreamsLexer(input);
                CommonCppWithStreamsParser parser = new CommonCppWithStreamsParser(new CommonTokenStream(lexer));
                CommonTree tree = (CommonTree)parser.s().getTree();
                
                // Save AST graph to file
                DOTTreeGenerator gen = new DOTTreeGenerator();
                StringTemplate st = gen.toDOT(tree);
                String fileNameASTDOT = fileName + "_AST.dot";
                String fileNameASTPS = fileName + "_AST.ps";
                PrintWriter out = new PrintWriter(fileNameASTDOT);
                out.print(st);
                out.close();
                try {
                	Runtime.getRuntime().exec("dot -Tps " + fileNameASTDOT + " -o " + fileNameASTPS);
                } catch (Exception e) {
                	e.printStackTrace(System.err);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
