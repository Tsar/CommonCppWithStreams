package components;

import gen.CommonCppWithStreamsLexer;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.antlr.runtime.tree.Tree;

public class ArgumentsDef implements CodeProvider {
	private ArrayList<VarDef> arguments;
	
	public ArgumentsDef(Tree tree, ErrorsCollector ec) {
		assert(tree.getType() == CommonCppWithStreamsLexer.ARGS);

		arguments = new ArrayList<VarDef>();
		for (int i = 0; i < tree.getChildCount(); ++i) {
			Tree child = tree.getChild(i);
			assert(child.getType() == CommonCppWithStreamsLexer.ARG);
			assert(child.getChildCount() == 2 || child.getChildCount() == 3);
			
			arguments.add(new VarDef(child, ec));
		}
	}

	public void writeCppCode(PrintWriter w) {
		for (int i = 0; i < arguments.size(); ++i) {
			arguments.get(i).writeCppCode(w);
			if (i != arguments.size() - 1)
				w.print(", ");
		}
	}
}
