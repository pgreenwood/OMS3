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
public class ThreeCompChainTest {

	public static class Cmd1 {

		@In
		public String in;
		@Out
		public String out;

		@Execute
		public void execute() {
			out = "CMD1(" + in + ")";
		}
	}

	public static class Cmd2 {

		@In
		public String in;
		@Out
		public String out;

		@Execute
		public void execute() {
			out = "CMD2(" + in + ")";
		}
	}

	public static class Cmd3 {

		@In
		public String in1 = "1.2";
		@In
		public String in;
		@Out
		public String out;

		@Execute
		public void execute() {
			out = "CMD3(" + in + in1 + ")";
		}
	}

	public static class C extends Compound {

		@In
		public String in;
		@Out
		public String out;

		// creating the operations
		private Cmd1 op1 = new Cmd1();
		private Cmd2 op2 = new Cmd2();
		private Cmd3 op3 = new Cmd3();

		public C() throws Exception {
			out2in(op1, "out", op2, "in");
			out2in(op2, "out", op3, "in");

			// maps the compound fields
			in2in("in", op1);
			out2out("out", op3, "out");
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		Compound.reload();
	}

	@Test
	public void threeCompChain() throws Exception {
		C c = new C();
		c.in = "1";
		c.execute();
		assertEquals("CMD3(CMD2(CMD1(1))1.2)", c.out);
	}
}
