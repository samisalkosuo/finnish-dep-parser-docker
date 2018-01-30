package findep.utils;

public class SystemOutLogger {

	public int LOG_LEVEL = 1;

	private static SystemOutLogger logger = new SystemOutLogger();

	public static SystemOutLogger getInstance() {
		return logger;
	}

	public void sysout(int logLevel, String msg) {
		sysout(logLevel, msg, true);
	}

	public void sysout(int logLevel, String msg, boolean newline) {

		if (LOG_LEVEL == 1) {
			// replace new lines if written to log
			msg = StringUtils.replaceNewLines(msg);
			msg = StringUtils.shorten(msg);

		}

		if (LOG_LEVEL == logLevel || logLevel == -1) {
			// replace new lines if written to log
			msg = StringUtils.replaceNewLines(msg);

			if (newline == true) {
				System.out.print(StringUtils.now());
				System.out.print(": ");
				System.out.println(msg);
			} else {
				System.out.print(msg);
			}
		}
	}
}
