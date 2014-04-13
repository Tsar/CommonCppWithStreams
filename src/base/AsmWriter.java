package base;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import components.Block;

public class AsmWriter {
	private PrintWriter pw;
	private boolean optimizePushPop;

	private int sp;
	private int nextUId;
	private Map<Integer, Integer> uidToSP;
	private List<Block> blockList;

	private List<String> pendingPushPopList;  // for optimizing

	public AsmWriter(PrintWriter pw, boolean optimizePushPop) {
		this.pw = pw;
		this.optimizePushPop = optimizePushPop;
		if (optimizePushPop) {
			pendingPushPopList = new ArrayList<String>();
		}
		sp = 0;
		nextUId = 0;
		uidToSP = new HashMap<Integer, Integer>();
		blockList = new ArrayList<Block>();
	}

	public int genNewUId() {
		return nextUId++;
	}

	public void ln() {
		pw.println();
	}

	public void l(String label) {
		ln();
		pw.println(label + ":");
	}

	private void cInternal(String command) {
		if (optimizePushPop) {
			for (String pp : pendingPushPopList) {
				pw.println("    " + pp);
			}
			pendingPushPopList.clear();
		}

		pw.println("    " + command);
	}

	public void c(String command) {
		assert(!(command.startsWith("push") || command.startsWith("pop")/* || command.startsWith("add esp")*/));

		cInternal(command);
	}

	public void c(String command, String comment) {
		c(command + "  ; " + comment);
	}

	public void t(String comment) {
		ln();
		tNoLn(comment);
	}

	public void tNoLn(String comment) {
		pw.print("    ; ");
		pw.println(comment);
	}

	public int push(String regName) {
		String asmCmd = "push " + regName;
		if (!optimizePushPop) {
			cInternal(asmCmd);
		} else {
			if (pendingPushPopList.size() > 0 && pendingPushPopList.get(pendingPushPopList.size() - 1).startsWith("pop " + regName)) {
				pendingPushPopList.remove(pendingPushPopList.size() - 1);
			} else {
				pendingPushPopList.add(asmCmd);
			}
		}
		sp += 4;
		return sp;
	}

	public void pop(String regName) {
		String asmCmd = "pop " + regName;
		if (!optimizePushPop) {
			cInternal(asmCmd);
		} else {
			if (pendingPushPopList.size() > 0 && pendingPushPopList.get(pendingPushPopList.size() - 1).startsWith("push " + regName)) {
				pendingPushPopList.remove(pendingPushPopList.size() - 1);
			} else {
				pendingPushPopList.add(asmCmd);
			}
		}
		sp -= 4;
	}

	public int push(String regName, String comment) {
		return push(optimizePushPop ? regName : (regName + "  ; " + comment));
	}

	public void pop(String regName, String comment) {
		pop(optimizePushPop ? regName : (regName + "  ; " + comment));
	}

	/*
    // FOLLOWING CODE IS INCORRECT: IMAGINE, IF USED INSIDE IF WITH RETURN [still requires to think]
	public void addESP(int addition) {
		addESP(addition, true);
	}

	public void addESP(int addition, boolean changeSP) {
		if (!optimizePushPop) {
			cInternal("add esp, " + addition);
		} else {
			int i = pendingPushPopList.size() - 1;
			int addition_ = addition;
			while (addition_ > 0 && i >= 0 && pendingPushPopList.get(i).startsWith("push")) {
				pendingPushPopList.remove(i);
				addition_ -= 4;
				--i;
			}
			if (addition_ != 0) {
				cInternal("add esp, " + addition_);
			}
		}
		if (changeSP) {
			sp -= addition;
		}
	}
	*/

	public void push4() {
		push("ebx");
		push("ebp");
		push("esi");
		push("edi");
	}

	public void pop4() {
		pop("ebx");
		pop("ebp");
		pop("esi");
		pop("edi");
	}

	public int getSP() {
		return sp;
	}

	public void setSP(int sp) {
		this.sp = sp;
	}

	public void setVariableSP(int varUId, int varSP) {
		uidToSP.put(varUId, varSP);
	}

	public String varAddr(int varUId) {
		assert(uidToSP.containsKey(varUId));

		return "[esp + " + (sp - uidToSP.get(varUId)) + "]";
	}

	public void blockStart(Block b) {
		blockList.add(b);
	}

	public void blockEnd() {
		blockList.remove(blockList.size() - 1);
	}

	public List<Block> getBlockList() {
		return blockList;
	}
}
