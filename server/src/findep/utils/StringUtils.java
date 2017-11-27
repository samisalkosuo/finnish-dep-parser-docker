package findep.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public static String replaceNewLines(String msg)
	{
		return 	msg.replace("\r", "").replace("\n", "|");
	}
	
	public static String shorten(String msg)
	{
		int l = msg.length();
		String end = "";
		if (l > 64) {
			l = 64;
			end = "...";
		}
		msg = msg.substring(0, l) + end;

		return msg;
	}

	public static String now()
	{
		return sdf.format(new Date());
	}
	
}
