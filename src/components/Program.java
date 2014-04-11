package components;

import java.io.PrintWriter;
import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Program implements CodeProvider {
	private ArrayList<VarDefOrFunction> contents;
	private SymbolTable st;
	
	public Program(Tree tree, ErrorsCollector ec) {
		contents = new ArrayList<VarDefOrFunction>();
		st = new SymbolTable(ec);

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
	}

	public void writeCppCode(PrintWriter w) {
		for (VarDefOrFunction vf : contents) {
			vf.writeCppCode(w);
		}
	}
	
	public void writeAsmCode(PrintWriter w) {
	    w.println("    section .text");
	    w.println("    global _func_main");
		for (VarDefOrFunction vf : contents) {
			vf.writeAsmCode(w);
		}
		w.println("    section .data");
		w.println("    end");
	}
}
