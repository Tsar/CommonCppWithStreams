import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

import base.AsmWriter;
import base.ErrorsCollector;
import base.SymbolTable;

import components.Program;

import java.io.*;

import gen.*;

public class Compiler {
	private static final boolean OPTIMIZE_PUSH_POP = false;

	private static int exec(String command) throws IOException, InterruptedException {
		Process proc = Runtime.getRuntime().exec(command);
    	proc.waitFor();
    	BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        while ((s = stdError.readLine()) != null) {
            System.err.println(s);
        }
    	return proc.exitValue();
	}

	private static void saveASTToFile(CommonTree tree, String dotFileName, String psFileName) throws FileNotFoundException {
		System.out.println("Saving AST to file '" + dotFileName + "'...");
		DOTTreeGenerator gen = new DOTTreeGenerator();
        StringTemplate st = gen.toDOT(tree);
        PrintWriter out = new PrintWriter(dotFileName);
        out.print(st);
        out.close();
        System.out.println("Done\n");
        System.out.println("Converting AST to '" + psFileName + "'...");
        try {
        	System.out.println((exec("dot -Tps " + dotFileName + " -o " + psFileName) == 0) ? "Done\n" : "Failed\n");
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
                    String asmFileName = fileNameWE + ".asm";
                    System.out.println("Writing ASM code to '" + asmFileName + "'...");
                    p.writeAsmCode(new AsmWriter(new PrintWriter(asmFileName), OPTIMIZE_PUSH_POP));
                    System.out.println("Done\n");

                    System.out.println("Creating object-file from asm...");
                    try {
                    	if (exec("yasm -f elf32 -o " + fileNameWE + ".o " + asmFileName) == 0) {
                    		System.out.println("Done\n");

                    		System.out.println("Creating exe-file from object-file...");
                    		if (exec("ld -m elf_i386 " + fileNameWE + ".o -o " + fileNameWE) == 0) {
                    			System.out.println("Done\n");
                    		} else {
                    			ec.fatalError("linking failed");
                    		}
                    	} else {
                    		ec.fatalError("compilation failed");
                    	}
                    } catch (Exception e) {
                    	e.printStackTrace(System.err);
                    }
                }

                System.out.println("======= " + ec.getErrorsCount() + " errors; " + ec.getWarningsCount() + " warnings =======");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
