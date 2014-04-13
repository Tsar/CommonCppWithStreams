package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

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

	public void writeAsmCode(AsmWriter w) {
	    w.c("section .text");
	    w.c("global _start");
	    w.l("_start");
	    w.c("call _func_main");
	    w.t("Exit with result of 'main' (it is in eax)");
	    w.c("mov ebx, eax");
	    w.c("mov eax, 1", "number of exit syscall");
	    w.c("int 80h");
		for (VarDefOrFunction vf : contents) {
			vf.writeAsmCode(w);
		}
		w.ln();
		w.c("section .data");
		w.c("end");
		w.close();

		assert(w.getSP() == 0);
	}
}
