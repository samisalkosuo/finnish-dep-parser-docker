package fi.dep.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {

	private static ApplicationProperties instance = new ApplicationProperties();
	private Properties props = new Properties();

	private ApplicationProperties() {
		InputStream inputStream = null;
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			inputStream = loader.getResourceAsStream("application.properties");
			props.load(inputStream);
		} catch (Exception e) {
			System.err.println(e.toString());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public static String version() {
		return instance.props.getProperty("version");
	}

	public static String buildtimestamp() {
		return instance.props.getProperty("buildtimestamp");
	}

	public static String getProperty(String name) {
		return instance.props.getProperty(name);

	}

}
