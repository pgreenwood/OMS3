/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ngmf;

import junit.framework.Assert;
import oms3.Compound;
import oms3.annotations.Execute;
import oms3.annotations.Out;
import oms3.control.Iteration;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Olaf David
 */
public class SimpleIterationTest {

	static int i = 0;

	public static class Cmd1 {

		public @Out
		boolean done;

		@Execute
		public void execute() {
			i++;
			// System.out.println("Loop " + i);
			done = i < 10;
		}
	}

	public static class W extends Iteration {

		Cmd1 cmd1 = new Cmd1();

		public W() {
			conditional(cmd1, "done");
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		Compound.reload();
	}

	@Test()
	public void simpleIteration() throws Exception {
		final W c = new W();
		c.execute();
		Assert.assertEquals(10, i);
	}
}
