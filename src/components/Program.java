package components;

import java.io.PrintWriter;
import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Program implements CodeProvider {
	private ArrayList<VarDefOrFunction> contents;

	public Program(Tree tree, ErrorsCollector ec, SymbolTable st) {
		contents = new ArrayList<VarDefOrFunction>();

		st.beginBlock();
		if (tree == null) {
			// Nothing to do: empty program
		} else if (tree.getType() == CommonCppWithStreamsLexer.VAR_DEF) {
			contents.add(new VarDefOrFunction(new VarDef(tree, ec, st)));
		} else if (tree.getType() == CommonCppWithStreamsLexer.FUNCTION) {
			contents.add(new VarDefOrFunction(new Function(tree, ec, st)));
		} else {
			for (int i = 0; i < tree.getChildCount(); ++i) {
	        	Tree child = tree.getChild(i);
	        	if (child.getType() == CommonCppWithStreamsLexer.VAR_DEF) {
	        		contents.add(new VarDefOrFunction(new VarDef(child, ec, st)));
	        	} else if (child.getType() == CommonCppWithStreamsLexer.FUNCTION) {
	        		contents.add(new VarDefOrFunction(new Function(child, ec, st)));
	        	} else {
	        		assert(false);
	        	}
	        }
		}
		st.endBlock();

		if (!st.isMainFuncDefined()) {
			ec.fatalError("no function 'main' defined");
		}
	}

	public void writeCppCode(PrintWriter w) {
		for (VarDefOrFunction vf : contents) {
			vf.writeCppCode(w);
		}
	}
	
	public void writeAsmCode(AsmWriter w) {
	    w.c("section .text");
	    w.c("global _start");
	    w.l("_start");
	    w.c("call _func_main");
	    w.t("Exit with code 0");
	    w.c("mov eax, 1");
	    w.c("xor ebx, ebx"); // return code is 0 (TODO: support return code of main)
	    w.c("int 80h");
	    w.ln();
		for (VarDefOrFunction vf : contents) {
			vf.writeAsmCode(w);
		}
		w.ln();
		w.c("section .data");
		w.c("end");
	}
}
