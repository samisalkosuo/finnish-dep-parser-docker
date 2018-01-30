package fi.dep.utils;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.util.LFUCache;
import org.apache.commons.io.output.StringBuilderWriter;

import fi.dep.utils.cache.MyCache;

public class SimpleStats {

	private static SimpleStats instance = new SimpleStats();

	private SystemOutLogger SYSOUTLOGGER = SystemOutLogger.getInstance();

	// start time of program
	private long startTimeOfThis = System.currentTimeMillis();

	private long numberOfRequestsHandled = 0;
	private long totalProcessingTimeNano = 0;
	private long totalBytesProcessed = 0;
	private long errors = 0;

	private List<Long> elapsedTimes = new ArrayList<Long>();
	private List<Long> startTimesMsec = new ArrayList<Long>();
	private List<Long> endTimesMsec = new ArrayList<Long>();
	private List<Long> processedBytes = new ArrayList<Long>();

	private double K = 1000.0;
	private double MILLION = 1000000.0;
	private double BILLION = 1000000000.0;
	private double KB = 1024.0;
	private double MB = KB * KB;

	public int maxCacheSize = 0;
	private long cacheHits = 0;

	// fixed length FIFO queue:
	// http://fabian-kostadinov.github.io/2014/11/25/implementing-a-fixed-length-fifo-queue-in-java/
	// latest parsed texts
	private String[] initString = { "", "", "", "", "", "", "", "", "", "" };
	private StringFixedLengthFifoQueue strQueue = new StringFixedLengthFifoQueue(initString);

	private SimpleStats() {

	}

	public static SimpleStats getInstance() {
		return instance;
	}

	public void increaseCacheHits() {
		cacheHits = cacheHits + 1;
	}

	public void addRequest(long startNano, long endNano, long startMsec, long endMsec, long bytesProcessed,
			boolean errorHappened) {
		addRequest(startNano, endNano, startMsec, endMsec, bytesProcessed, errorHappened, null);

	}

	public void addRequest(long startNano, long endNano, long startMsec, long endMsec, long bytesProcessed,
			boolean errorHappened, String logText) {
		numberOfRequestsHandled = numberOfRequestsHandled + 1;
		if (errorHappened == true) {
			errors = errors + 1;
		} else {
			long elapsed = endNano - startNano;
			totalProcessingTimeNano = totalProcessingTimeNano + elapsed;
			totalBytesProcessed = totalBytesProcessed + bytesProcessed;
			elapsedTimes.add(elapsed);
			startTimesMsec.add(startMsec);
			endTimesMsec.add(endMsec);
			processedBytes.add(bytesProcessed);

			if (logText != null) {
				strQueue.add(String.format("%s: %s", StringUtils.now(),
						StringUtils.shorten(StringUtils.replaceNewLines(logText))));
			}

		}
	}

	public String getStatistics() {
		return getStatistics(null);

	}

	public String getStatistics(LFUCache<String, String> cacheInstance) {

		StringBuilderWriter sbw = new StringBuilderWriter();
		PrintWriter pw = new PrintWriter(sbw);
		pw.println("Simple statistics:");

		Date dt = new Date(startTimeOfThis);
		pw.println("  Start time             : " + dt);

		// get ip address/hostname, because this server can run on many Docker
		// containers/hosts
		String ipAddress = "n/a";
		String hostName = "n/a";
		try {
			InetAddress ia = InetAddress.getLocalHost();
			ipAddress = ia.getHostAddress();
			hostName = ia.getHostName();

		} catch (Exception e) {
			// ignore all exceptions
		}
		pw.println(String.format("  Host                   : %s (%s)", ipAddress, hostName));
		pw.println("  Uptime                 : " + elapsedTime(System.currentTimeMillis() - startTimeOfThis));
		pw.println("  Requests               : " + numberOfRequestsHandled + ", failed: " + errors);

		// vm stats
		// see also https://stackoverflow.com/a/12807848
		pw.println("  Processors (cores)     : " + Runtime.getRuntime().availableProcessors());
		long maxmemory = Math.min(Runtime.getRuntime().totalMemory(), Runtime.getRuntime().maxMemory());
		pw.println("  Free memory (approx.)  : "
				+ String.format("%.03f / %.03f MB", Runtime.getRuntime().freeMemory() / MB, maxmemory / MB));

		// cache stats
		if (MyCache.getInstance().isEnabled()) {
			pw.println("  Cache is used.");
			pw.println("  Cache hits             : " + cacheHits);

		} else {
			pw.println("  Cache not used.");

		}
		/*
		 * if (cacheInstance != null) { int cacheSize = cacheInstance.size();
		 * Set<String> keys = cacheInstance.keySet(); long totalCachedBytes = 0;
		 * for (String key : keys) { totalCachedBytes = totalCachedBytes +
		 * cacheInstance.get(key).length(); }
		 * 
		 * pw.println("  Cache hits             : " + cacheHits);
		 * pw.println("  Cached documents       : " + cacheSize + "/" +
		 * maxCacheSize); pw.println("  Cached documents size  : " +
		 * String.format("%.02f KB", (totalCachedBytes) / KB)); } else {
		 * pw.println("  Cache not used.");
		 * 
		 * }
		 */
		// memory
		// check this
		// https://stackoverflow.com/questions/17374743/how-can-i-get-the-memory-that-my-java-program-uses-via-javas-runtime-api

		if (numberOfRequestsHandled > 0) {
			try {
				long totalProcessingTime = totalProcessingTimeNano;
				pw.println("  Total bytes processed  : " + totalBytesProcessed + ", "
						+ String.format("%.02f KB", totalBytesProcessed / KB));
				pw.println("  Total processing time  : " + String.format("%.02f seconds", totalProcessingTime / BILLION)
						+ ", " + totalProcessingTime + " nanoseconds");
				pw.println("  Average bytes processed: " + String.format("%.02f KB/request",
						(totalBytesProcessed / (numberOfRequestsHandled - errors)) / KB));

				double average = this.averageProcessingTimeNano();
				average = average / MILLION;
				double averagePerKB = totalProcessingTime / (totalBytesProcessed / KB);
				averagePerKB = averagePerKB / MILLION;
				pw.println("  Average processing time: " + average + " msecs/request, " + averagePerKB + " msec/KB");

				int index = this.maxProcessingTimeIndex();
				pw.println("  Maximum processing time: " + getProcessingTime(index));
				index = this.minProcessingTimeIndex();
				pw.println("  Minimum processing time: " + getProcessingTime(index));
			} catch (Throwable t) {
				// catch all exceptions and print exception
				pw.println();
				pw.println("  " + t.toString());
			}
		}

		if (SYSOUTLOGGER.LOG_LEVEL == 0) {
			// show only if log level is 0
			pw.println();
			pw.println("Latest parsed texts:");
			pw.println();
			pw.println(strQueue.toString());

		}

		pw.close();

		return sbw.toString();
	}

	private String getProcessingTime(int index) {
		long time = elapsedTimes.get(index);
		// max bytes
		long bytes = processedBytes.get(index);
		long requestStartTime = startTimesMsec.get(index);
		long requestElapsedTime = startTimesMsec.get(index) - startTimeOfThis;

		Date dt = new Date(requestStartTime);
		return (time / MILLION + " msecs, " + bytes + " bytes @ " + dt + ", "
				+ String.format("~%.02f seconds", requestElapsedTime / K) + " secs since start");

	}

	private double averageProcessingTimeNano() {
		return (1.0 * totalProcessingTimeNano) / numberOfRequestsHandled;
	}

	private int minProcessingTimeIndex() {

		int index = 0;

		for (int i = 0; i < elapsedTimes.size(); i++) {
			index = elapsedTimes.get(i) < elapsedTimes.get(index) ? i : index;
		}

		return index;

	}

	private int maxProcessingTimeIndex() {

		int index = 0;

		for (int i = 0; i < elapsedTimes.size(); i++) {
			index = elapsedTimes.get(i) > elapsedTimes.get(index) ? i : index;
		}

		return index;

	}

	private String elapsedTime(long elapsedTime) {
		long hr = TimeUnit.MILLISECONDS.toHours(elapsedTime);
		long min = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60;
		long sec = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
		long ms = TimeUnit.MILLISECONDS.toMillis(elapsedTime) % 1000;
		return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
	}
}
