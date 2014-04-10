package components;

import java.io.PrintWriter;
import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Program implements CodeProvider {
	private ArrayList<VarDefOrFunction> contents;
	
	public Program(Tree tree, ErrorsCollector ec) {
		contents = new ArrayList<VarDefOrFunction>();

		if (tree == null) {
			// Nothing to do: empty program
		} else if (tree.getType() == CommonCppWithStreamsLexer.VAR_DEF) {
			contents.add(new VarDefOrFunction(new VarDef(tree, ec)));
		} else if (tree.getType() == CommonCppWithStreamsLexer.FUNCTION) {
			contents.add(new VarDefOrFunction(new Function(tree, ec)));
		} else {
			for (int i = 0; i < tree.getChildCount(); ++i) {
	        	Tree child = tree.getChild(i);
	        	if (child.getType() == CommonCppWithStreamsLexer.VAR_DEF) {
	        		contents.add(new VarDefOrFunction(new VarDef(child, ec)));
	        	} else if (child.getType() == CommonCppWithStreamsLexer.FUNCTION) {
	        		contents.add(new VarDefOrFunction(new Function(child, ec)));
	        	} else {
	        		assert(false);
	        	}
	        }
		}
	}

	public void writeCppCode(PrintWriter w) {
		for (VarDefOrFunction vf : contents) {
			vf.writeCppCode(w);
		}
	}
}
