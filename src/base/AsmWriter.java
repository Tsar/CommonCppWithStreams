package base;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import components.Block;
import components.VarDef;

public class AsmWriter {
	private PrintWriter pw;
	private boolean optimizePushPop;

	private int sp;
	private int nextUId;
	private Map<Integer, Integer> uidToSP;
	private List<Block> blockList;
	private List<String> fileNames;

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
		fileNames = new ArrayList<String>();
	}

	public void writeBeginning() {
	    c("section .text");
	    c("global _start");
	    l("_start");

	    t("Save initial 'esp' value to 'edx'");
	    c("mov edx, esp");
	}

	public void writeExitSyscall() {
		t("Exit with result of 'main' (it is in 'eax')");
	    c("mov ebx, eax");
	    c("mov eax, 1", "number of 'exit' syscall");
	    c("int 80h");
	}

	public void writeEndingAndClose() {
		/* prepare_int_eax_to_write */
		l("_binary_prepare_int_eax_to_write");
		c("mov dword [str_buf], eax");
		c("mov ecx, str_buf");
		c("mov edx, 4");
		c("ret");

		l("_minus_to_buffer_and_continue");
		c("mov byte [str_buf], '-'");
		c("inc esi");
		c("neg eax");
		c("neg edi");
		c("jmp _continue_writing_int");

		l("prepare_int_eax_to_write");
		c("test esi, esi", "what mode (text or binary)");
		c("jnz _binary_prepare_int_eax_to_write");

		push("esi");
		c("mov edi, eax");
		c("mov ebx, 10");
		c("mov esi, 0");
		c("test eax, eax");
		c("js _minus_to_buffer_and_continue");

		l("_continue_writing_int");
		c("xor edx, edx");
		c("div ebx");
		c("inc esi");
		c("test eax, eax");
		c("jnz _continue_writing_int");

		c("mov eax, edi");
		c("lea edi, [esi + str_buf - 1]");

		l("_continue_writing_int_2");
		c("xor edx, edx");
		c("div ebx");
		c("add dl, '0'");
		c("mov byte [edi], dl");
		c("dec edi");
		c("test eax, eax");
		c("jnz _continue_writing_int_2");

		l("_finish_writing_int");
		c("mov ecx, str_buf");
		c("mov edx, esi");
		pop("esi");
		c("ret");
		ln();

		/* prepare_bool_eax_to_write */
		l("_binary_prepare_bool_eax_to_write");
		c("mov byte [str_buf], al");
		c("mov ecx, str_buf");
		c("mov edx, 1");
		c("ret");

		l("prepare_bool_eax_to_write");
		c("test esi, esi", "what mode (text or binary)");
		c("jnz _binary_prepare_bool_eax_to_write");

		c("test eax, eax");
		c("jz _false_to_eax");
		c("mov ecx, str_true");
		c("mov edx, 4");
		c("ret");
		l("_false_to_eax");
		c("mov ecx, str_false");
		c("mov edx, 5");
		c("ret");
		ln();

		/* write_space */
		l("write_space_if_text_mode");
		c("test esi, esi");
		c("jnz _just_ret", "if binary mode");
		c("mov ecx, str_space");
		c("mov edx, 1");
		c("mov eax, 4", "number of 'write' syscall");
	    c("int 80h");
		c("ret");
		ln();

		/* write_endl */
		l("write_endl_if_text_mode");
		c("test esi, esi");
		c("jnz _just_ret", "if binary mode");
		c("mov ecx, str_endl");
		c("mov edx, 1");
		c("mov eax, 4", "number of 'write' syscall");
	    c("int 80h");
		c("ret");
		ln();

		/* open for writing */
		l("get_W_descriptor_into_ebp_and_mode_into_esi");
		c("mov ebp, 0", "descriptor of console");
		c("mov esi, 0", "text mode");
		c("cmp al, 2");
		c("jz _W_descriptor_is_set");

		c("cmp al, 6");
		c("jnz _W_descriptor_try_next");

		c("mov esi, 1", "binary mode");
		c("jmp _W_descriptor_open_file");

		l("_W_descriptor_try_next");
		c("cmp al, 4");
		c("jnz _W_descriptor_is_set", "writing to console, if not 2, 4 or 6");

		l("_W_descriptor_open_file");
		push("edx");
		c("shr eax, 8");
		c("mov ebx, 256");
		c("mul ebx");
		c("lea ebx, [eax + filename_0]");
		c("mov ecx, 101", "O_CREAT | O_WRONLY");
		c("mov edx, 644", "access mode");
		c("mov eax, 5", "number of 'open' syscall");
		c("int 80h");
		c("mov ebp, eax");

		c("mov ebx, eax");
		c("mov ecx, 0");
		c("mov edx, 2", "SEEK_END");
		c("mov eax, 19", "number of 'lseek' syscall");
		c("int 80h");
		pop("edx");

		l("_W_descriptor_is_set");
		c("ret");

		/* close */
		l("close_by_descriptor_in_ebp");
		c("test ebp, ebp");
		c("jz _just_ret");

		c("mov ebx, ebp");
		c("mov eax, 6", "number of 'close' syscall");
		c("int 80h");

		l("_just_ret");
		c("ret");
		ln();

		c("section .rodata");
		ln();
		pw.println("str_true  db \"true\"");
		pw.println("str_false db \"false\"");
		pw.println("str_space db \" \"");
		pw.println("str_endl  db 10");
		ln();
		if (fileNames.size() > 0) {
			for (int i = 0; i < fileNames.size(); ++i) {
				pw.print("filename_" + i + " db " + fileNames.get(i));
				for (int j = 0; j < 258 - fileNames.get(i).length(); ++j) {
					pw.print(",0");
				}
				pw.println();
			}
		} else {
			pw.println("filename_0 db 0");
		}
		ln();

		c("section .data");
		ln();
		pw.println("str_buf db 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
		ln();

		c("end");
		close();
	}

	public void close() {
		pw.close();
	}

	public int genNewUId() {
		return nextUId++;
	}

	public void ln() {
		pw.println();
	}

	public int addFileName(String fileName) {
		fileNames.add(fileName);
		return fileNames.size() - 1;
	}

	public void optimizerOutput() {
		if (optimizePushPop) {
			for (String pp : pendingPushPopList) {
				pw.println("    " + pp);
			}
			pendingPushPopList.clear();
		}
	}

	public void l(String label) {
		optimizerOutput();
		ln();
		pw.println(label + ":");
	}

	private void cInternal(String command) {
		optimizerOutput();
		pw.println("    " + command + "  ; sp = " + sp);
	}

	public void c(String command) {
		assert(!(command.startsWith("push") || command.startsWith("pop") || command.startsWith("add esp")));

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

	// Method for cleaning stack
	public void addESP(int addition) {
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
		sp -= addition;
	}

	public void push4() {
		push("ebx");
		push("ebp");
		push("esi");
		push("edi");
	}

	public void pop4() {
		pop("edi");
		pop("esi");
		pop("ebp");
		pop("ebx");
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

	public String varAddr(VarDef varDef) {
		int varUId = varDef.getUId();
		assert(uidToSP.containsKey(varUId));

		return varDef.isGlobal() ? "[edx - " + uidToSP.get(varUId) + "]" : "[esp + " + (sp - uidToSP.get(varUId)) + "]";
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
