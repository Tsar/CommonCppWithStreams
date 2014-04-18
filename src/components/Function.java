package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.Type;
import base.TypeConverter;

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

		ec.check(!name.equals("main") || argsDef.count() == 0, tree.getLine(), "function 'main' should not have any arguments");
	}

	public String getName() {
		return name;
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
		w.push4();
		int initialSP = w.getSP();

		argsDef.writeAsmCode(w);
		block.writeAsmCode(w);

		w.l("_end_of_func_" + name);
		if (w.getSP() != initialSP) {
			w.tNoLn("cleaning stack of local function variables");
			w.addESP(w.getSP() - initialSP);
			w.ln();
		}
		w.pop4();
		w.c("ret");
	}
}
