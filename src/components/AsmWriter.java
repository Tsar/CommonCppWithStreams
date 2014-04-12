package components;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class AsmWriter {
	private PrintWriter pw;

	private int sp;
	private int nextUId;
	private Map<Integer, Integer> uidToSP;

	public AsmWriter(PrintWriter pw) {
		this.pw = pw;
		sp = 0;
		nextUId = 0;
		uidToSP = new HashMap<Integer, Integer>();
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

	public void t(String comment) {
		ln();
		pw.print("    ; ");
		pw.println(comment);
	}

	public int push(String regName) {
		cInternal("push " + regName);
		sp += 4;
		return sp;
	}

	public void pop(String regName) {
		cInternal("pop " + regName);
		sp -= 4;
	}

	public void pushad() {
		cInternal("pushad");
		sp += 32;
	}

	public void popad() {
		cInternal("popad");
		sp -= 32;
	}

	public int getSP() {
		return sp;
	}

	public void setVariableSP(int varUId, int varSP) {
		uidToSP.put(varUId, varSP);
	}

	public String varAddr(int varUId) {
		assert(uidToSP.containsKey(varUId));

		return "[esp + " + (sp - uidToSP.get(varUId)) + "]";
	}
}
