package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.Type;

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
			type = funcDef.getType();
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
		w.t("Function '" + name + "' Call");
		if (args.size() > 0) {
			w.t("Pushing arguments on stack");
		}
		for (Expression arg : args) {
			arg.writeAsmCode(w);
		}
		w.call("_func_" + name);
		// return value should be in eax by now
		w.t("Removing arguments from stack");
		w.addESP(args.size() * 4);
		w.push("eax");
	}
}
