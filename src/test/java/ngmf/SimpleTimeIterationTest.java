/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ngmf;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import oms3.Compound;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.control.Time;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Olaf David
 */
public class SimpleTimeIterationTest {

	static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
	static Calendar last;

	/**
	 * A command that consumes time.
	 * 
	 */
	public static class Cmd1 {

		@In
		public Calendar current;

		@Execute
		public void execute() {
			last = current;
		}
	}

	public static class W extends Time {

		Cmd1 cmd1 = new Cmd1();

		public W() {
			connectTime(cmd1, "current");
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		Compound.reload();
	}

	@Test()
	public void simpleTimeIteration() throws Exception {
		final W c = new W();

		GregorianCalendar s = new GregorianCalendar();
		s.setTime(sdf.parse("01-04-1999"));

		GregorianCalendar e = new GregorianCalendar();
		e.setTime(sdf.parse("01-10-1999"));

		c.start = s;
		c.end = e;
		c.amount = 1;
		c.field = Calendar.DATE;

		c.execute();

		Calendar curr = last;
		// System.out.println(sdf.format(curr.getTime()));
		Assert.assertEquals("01-10-1999", sdf.format(curr.getTime()));
	}
}
