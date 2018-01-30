package fi.dep.ported;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ParserLogImpl implements ParserLog {

public static final boolean DEBUG = true;
	
	public void debug(String message) {
		if(DEBUG)
			printWithAakkoset("Debug: "+message);

	}

	public void info(String message) {
		printWithAakkoset("Info: "+message);

	}

	public void error(String message) {
		printWithAakkoset("Error: "+message);
	}

	public void error(String message, Exception e) {
		this.error(message + "\n" + exceptionToString(e) );
		
	}

	// TODO consider removing will create extra objects just...
	public void printWithAakkoset(String s) {
		System.out.println(""+s);
	}
	
	public String exceptionToString(Exception e) {
		new StringBuffer();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		pw.close();
		return e+"\n"+sw.toString();		
	}
}
