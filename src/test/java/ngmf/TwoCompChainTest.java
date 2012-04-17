/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ngmf;

import static org.junit.Assert.assertEquals;
import oms3.Compound;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Olaf David
 */
public class TwoCompChainTest {

	public static class Cmd1 {

		@In
		public String in1;
		@Out
		public String out1;

		@Execute
		public void execute() {
			out1 = "CMD1(" + in1 + ")";
		}
	}

	public static class Cmd2 {

		@In
		public String in2;
		@Out
		public String out2;

		@Execute
		public void whatever() {
			out2 = "CMD2(" + in2 + ")";
		}
	}

	public static class C extends Compound {

		@In
		public String in;
		@Out
		public String out;

		// creating the operations
		Cmd1 op1 = new Cmd1();
		Cmd2 op2 = new Cmd2();

		public C() {
			// connects the two internals
			out2in(op1, "out1", op2, "in2");

			// maps the compound fields
			in2in("in", op1, "in1");
			out2out("out", op2, "out2");
			initializeComponents();
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		Compound.reload();
	}

	@Test
	public void twoCompChain() throws Exception {

		C c = new C();
		c.in = "1";
		c.execute();
		assertEquals("CMD2(CMD1(1))", c.out);
	}
}
