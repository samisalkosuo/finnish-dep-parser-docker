package findep.ported;

public interface ParserLog {
		public void debug(String message);
		public void info(String message);
		public void error(String message);
		public void error(String message, Exception e);	
}
