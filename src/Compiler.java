import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

import components.Program;

import java.io.*;

import gen.*;

public class Compiler {
	private static void saveASTToFile(CommonTree tree, String dotFileName, String psFileName) throws FileNotFoundException {
		System.out.println("Saving AST to file '" + dotFileName + "'...");
		DOTTreeGenerator gen = new DOTTreeGenerator();
        StringTemplate st = gen.toDOT(tree);
        PrintWriter out = new PrintWriter(dotFileName);
        out.print(st);
        out.close();
        System.out.println("Done");
        System.out.println("Converting AST to '" + psFileName + "'...");
        try {
        	Runtime.getRuntime().exec("dot -Tps " + dotFileName + " -o " + psFileName);
        	System.out.println("Done");
        } catch (Exception e) {
        	e.printStackTrace(System.err);
        }
	}

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: ccswc <cpp-program-source-1> [<cpp-program-source-2> [<cpp-program-source-3> ...]]");
            System.exit(1);
        }

        for (String fileName : args) {
            try {
                ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(new File(fileName)));
                CommonCppWithStreamsLexer lexer = new CommonCppWithStreamsLexer(input);
                CommonCppWithStreamsParser parser = new CommonCppWithStreamsParser(new CommonTokenStream(lexer));
                CommonTree tree = (CommonTree)parser.s().getTree();

                int syntaxErrorsCount = parser.getNumberOfSyntaxErrors();
                if (syntaxErrorsCount != 0) {
                	System.out.println("======= " + syntaxErrorsCount + " syntax errors =======");
                	System.exit(2);
                }

                // Save AST graph to file
                saveASTToFile(tree, fileName + "_AST.dot", fileName + "_AST.ps");
                
                Program p = new Program(tree);
                
                // TODO:
                // p.outputAsmCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}