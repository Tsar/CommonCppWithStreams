package base;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import components.Block;

public class AsmWriter {
	private PrintWriter pw;

	private int sp;
	private int nextUId;
	private Map<Integer, Integer> uidToSP;
	private List<Block> blockList;

	public AsmWriter(PrintWriter pw) {
		this.pw = pw;
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
		pw.print("    ");
		pw.println(command);
	}

	public void c(String command) {
		assert(!(command.startsWith("push") || command.startsWith("pop")));

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
		cInternal("push " + regName);
		sp += 4;
		return sp;
	}
	
	public int push(String regName, String comment) {
		cInternal("push " + regName + "  ; " + comment);
		sp += 4;
		return sp;
	}

	public void pop(String regName) {
		cInternal("pop " + regName);
		sp -= 4;
	}

	public void pop(String regName, String comment) {
		cInternal("pop " + regName + "  ; " + comment);
		sp -= 4;
	}

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
