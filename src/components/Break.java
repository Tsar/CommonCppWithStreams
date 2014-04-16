package components;

import java.util.List;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class Break implements CodeProvider {
	private int blocksUnderWhile;

	public Break(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.BREAK);
		assert(tree.getChildCount() == 0);

		blocksUnderWhile = 0;
		Tree p = tree.getParent();
		while (p != null && p.getType() != CommonCppWithStreamsLexer.WHILE) {
			if (p.getType() == CommonCppWithStreamsLexer.BLOCK)
				++blocksUnderWhile;
			p = p.getParent();
		}
		if (p == null) {
			ec.check(false, tree.getLine(), "break from no cycle");
			return;
		}
	}

	public void writeAsmCode(AsmWriter w) {
		assert(w.getWhileList().size() > 0);
		assert(w.getBlockList().size() >= blocksUnderWhile);

		if (blocksUnderWhile > 0) {
			int copyOfSP = w.getSP();
			List<Block> blockList = w.getBlockList();
			for (int i = blockList.size() - 1; i >= blockList.size() - blocksUnderWhile; --i) {
				blockList.get(i).writeAsmCodeToCleanLocalVariables(w);
			}
			w.setSP(copyOfSP);
		}

		int whileUId = w.getWhileList().get(w.getWhileList().size() - 1).getUId();
		w.c("jmp _while_" + whileUId + "_end");
	}
}
