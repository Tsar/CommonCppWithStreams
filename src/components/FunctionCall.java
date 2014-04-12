package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class FunctionCall implements CodeProvider {
	private String name;
	private Type type;
	private ArrayList<Expression> args;

	public FunctionCall(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.CALL);
		assert(tree.getChildCount() == 2);
		assert(tree.getChild(1).getType() == CommonCppWithStreamsLexer.ARGS);

		name = tree.getChild(0).getText();
		args = new ArrayList<Expression>();
		for (int i = 0; i < tree.getChild(1).getChildCount(); ++i) {
			Tree argExpr = tree.getChild(1).getChild(i);
			args.add(new Expression(argExpr, ec, st));
		}

		Function funcDef = st.referenceFunctionAndGetIt(name, tree.getLine());
		if (funcDef != null) {
			funcDef.checkArgumentsAndFillDefaults(args, tree.getLine());
		}
	}

	public String getFunctionName() {
		return name;
	}
	
	public Type getType() {
		return type;
	}

	public void writeAsmCode(AsmWriter w) {
		for (Expression arg : args) {
			arg.writeAsmCode(w);
		}
		w.c("call _func_" + name);
	}
}
