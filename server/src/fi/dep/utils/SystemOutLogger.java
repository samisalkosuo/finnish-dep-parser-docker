package fi.dep.utils;

public class SystemOutLogger {

	public int LOG_LEVEL = 1;

	private static SystemOutLogger logger = new SystemOutLogger();

	public static SystemOutLogger getInstance() {
		return logger;
	}

	public void sysout(String msg) {

		//print to system out only if log level is 1
		if (LOG_LEVEL == 1) {
			// replace new lines if written to log
			msg = StringUtils.replaceNewLines(msg);
			msg = StringUtils.shorten(msg);

			System.out.print(StringUtils.now());
			System.out.print(": ");
			System.out.println(msg);
		}
	}
}
