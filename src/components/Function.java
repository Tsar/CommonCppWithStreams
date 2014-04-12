package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

public class Function implements CodeProvider {
	private ErrorsCollector ec;

	private Type type;
	private String name;
	private ArgumentsDef argsDef;
	private Block block;

	public Function(Tree tree, ErrorsCollector ec, SymbolTable st) {
		this.ec = ec;

		assert(tree.getChildCount() == 4);
		assert(tree.getChild(0).getType() == CommonCppWithStreamsLexer.TYPE);
		assert(tree.getChild(1).getType() == CommonCppWithStreamsLexer.NAME);
		assert(tree.getChild(2).getType() == CommonCppWithStreamsLexer.ARGS);
		assert(tree.getChild(3).getType() == CommonCppWithStreamsLexer.BLOCK);
		
		type = TypeConverter.stringToType(tree.getChild(0).getText());
		name = tree.getChild(1).getText();
		st.declareFunction(name, type, this, tree.getLine());
		st.beginBlock();
		argsDef = new ArgumentsDef(tree.getChild(2), ec, st);
		block = new Block(tree.getChild(3), ec, st, true);
		st.endBlock();
	}

	public Type getType() {
		return type;
	}

	public void checkArgumentsAndFillDefaults(ArrayList<Expression> args, int lineNumber) {
		if (argsDef.count() < args.size()) {
			ec.check(false, lineNumber, "more arguments passed to function '" + name + "' than it has");
			return;
		}
		argsDef.checkArgumentsAndFillDefaults(args, lineNumber);
	}

	public void writeAsmCode(AsmWriter w) {
		w.l("_func_" + name);
		w.pushad();

		block.writeAsmCode(w);

		w.t("Function '" + name + "' ending");
		w.popad();
		w.c("ret");
	}
}
