package fi.dep;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;

import fi.dep.utils.SystemOutLogger;

public class SuperServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected SystemOutLogger SYSOUTLOGGER = SystemOutLogger.getInstance();
	
	
	protected String readInputStreamToString(ServletInputStream inputStream) throws IOException
	{
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	//	int inputSize = 0;
		byte[] inputBytes = new byte[1024];
		int bytesRead = 0;
		while ((bytesRead = bis.read(inputBytes)) != -1) {
		//	inputSize = inputSize + bytesRead;
			baos.write(inputBytes, 0, bytesRead);
		}
		baos.close();

		return new String(baos.toByteArray(), Charset.forName(StandardCharsets.UTF_8.name()));

	}
	
}
