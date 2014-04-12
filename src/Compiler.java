import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

import components.AsmWriter;
import components.ErrorsCollector;
import components.Program;
import components.SymbolTable;

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
            	System.out.println("======= Compiling '" + fileName + "' =======");

                ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(new File(fileName)));
                CommonCppWithStreamsLexer lexer = new CommonCppWithStreamsLexer(input);
                CommonCppWithStreamsParser parser = new CommonCppWithStreamsParser(new CommonTokenStream(lexer));
                CommonTree tree = (CommonTree)parser.s().getTree();

                int syntaxErrorsCount = parser.getNumberOfSyntaxErrors();
                if (syntaxErrorsCount != 0) {
                	System.out.println("======= " + syntaxErrorsCount + " syntax errors =======");
                	System.exit(2);
                }
                
                String fileNameWE = fileName.replaceFirst("[.][^.]+$", "");  // without extension

                // Save AST graph to file
                saveASTToFile(tree, fileNameWE + ".AST.dot", fileNameWE + ".AST.ps");

                ErrorsCollector ec = new ErrorsCollector();
                SymbolTable st = new SymbolTable(ec);

                Program p = new Program(tree, ec, st);

                if (ec.getErrorsCount() == 0) {
                	System.out.println("Writing C++ code...");
                	PrintWriter cpp = new PrintWriter(fileNameWE + ".gen.cpp");
                    p.writeCppCode(cpp);
                    cpp.close();
                    System.out.println("Done");

                    System.out.println("Writing ASM code...");
                    PrintWriter asm = new PrintWriter(fileNameWE + ".asm");
                    p.writeAsmCode(new AsmWriter(asm));
                    asm.close();
                    System.out.println("Done");
                }

                System.out.println("======= " + ec.getErrorsCount() + " errors; " + ec.getWarningsCount() + " warnings =======");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
