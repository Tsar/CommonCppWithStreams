package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class Program implements CodeProvider {
	private ArrayList<VarDef> varDefs;  // global variables
	private ArrayList<Function> functions;

	public Program(Tree tree, ErrorsCollector ec, SymbolTable st) {
		varDefs = new ArrayList<VarDef>();
		functions = new ArrayList<Function>();

		st.beginBlock();
		if (tree == null) {
			// Nothing to do: empty program
		} else if (tree.getType() == CommonCppWithStreamsLexer.VAR_DEF) {
			varDefs.add(new VarDef(tree, ec, st, true));
		} else if (tree.getType() == CommonCppWithStreamsLexer.FUNCTION) {
			functions.add(new Function(tree, ec, st));
		} else {
			for (int i = 0; i < tree.getChildCount(); ++i) {
	        	Tree child = tree.getChild(i);
	        	if (child.getType() == CommonCppWithStreamsLexer.VAR_DEF) {
	        		varDefs.add(new VarDef(child, ec, st, true));
	        	} else if (child.getType() == CommonCppWithStreamsLexer.FUNCTION) {
	        		functions.add(new Function(child, ec, st));
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
		w.writeBeginning();

	    w.t("Declare (and init) global variables");
	    for (VarDef varDef : varDefs) {
			varDef.writeAsmCode(w);
		}

	    w.ln();
	    w.call("_func_main");

	    w.t("Clear global variables");
	    w.addESP(varDefs.size() * 4);

	    w.writeExitSyscall();

	    for (Function function : functions) {
			function.writeAsmCode(w);
		}
	    w.ln();

		w.writeEndingAndClose();

		assert(w.getSP() == 0);
	}
}
