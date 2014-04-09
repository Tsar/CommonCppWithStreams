package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Program {
	private ArrayList<VarDefOrFunction> contents;
	
	public Program(Tree tree) {
		contents = new ArrayList<VarDefOrFunction>();
		
		for (int i = 0; i < tree.getChildCount(); ++i) {
        	Tree child = tree.getChild(i);
        	if (child.getType() == CommonCppWithStreamsLexer.VAR_DEF) {
        		contents.add(new VarDefOrFunction(new VarDef(child)));
        	} else if (child.getType() == CommonCppWithStreamsLexer.FUNCTION) {
        		contents.add(new VarDefOrFunction(new Function(child)));
        	} else {
        		assert(false);
        	}
        }
	}
}
