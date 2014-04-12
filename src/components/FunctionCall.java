package components;

import gen.CommonCppWithStreamsLexer;

import java.io.PrintWriter;

import org.antlr.runtime.tree.Tree;

public class FunctionCall implements CodeProvider {
	private String funcName;

	public FunctionCall(Tree tree, ErrorsCollector ec) {
		assert(tree.getType() == CommonCppWithStreamsLexer.CALL);
		assert(tree.getChildCount() == 2);
		assert(tree.getChild(1).getType() == CommonCppWithStreamsLexer.ARGS);

		funcName = tree.getChild(0).getText();
	}

	public String getFunctionName() {
		return funcName;
	}

	public void writeCppCode(PrintWriter w) {
	}

	public void writeAsmCode(AsmWriter w) {
	}
}
