package components;

import java.util.List;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.Type;
import base.TypeConverter;

public class Return implements CodeProvider {
	private Expression expr;
	private String funcName;

	public Return(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.RETURN);
		assert(tree.getChildCount() <= 1);

		expr = null;

		Tree p = tree.getParent();
		while (p != null && p.getType() != CommonCppWithStreamsLexer.FUNCTION) {
			p = p.getParent();
		}
		if (p == null) {
			ec.check(false, tree.getLine(), "return from no function");
			return;
		}
		assert(p.getChild(1).getType() == CommonCppWithStreamsLexer.NAME);
		Function funcDef = st.referenceFunctionAndGetIt(p.getChild(1).getText(), tree.getLine());
		assert(funcDef != null);
		funcName = funcDef.getName();
		if (funcDef.getType() == Type.VOID) {
			ec.check(tree.getChildCount() == 0, tree.getLine(), "can not return value out of 'void' function '" + funcName + "'");
		} else {
			if (tree.getChildCount() != 1) {
				ec.check(false, tree.getLine(), "can not return from '" + TypeConverter.typeToString(funcDef) + "' function '" + funcName + "' without returning a value");
				return;
			}
			expr = new Expression(tree.getChild(0), ec, st);
		}
	}

	public void writeAsmCode(AsmWriter w) {
		int copyOfSP = w.getSP();
		List<Block> blockList = w.getBlockList();
		int i = blockList.size() - 1;
		while (i >= 0 && !blockList.get(i).isFunctionBlock()) {
			blockList.get(i).writeAsmCodeToCleanLocalVariables(w);
			--i;
		}
		w.setSP(copyOfSP);

		if (expr != null) {
			expr.writeAsmCode(w);
			w.pop("eax", "return function '" + funcName + "' value to eax");
		}
		w.c("jmp _end_of_func_" + funcName);
	}
}
