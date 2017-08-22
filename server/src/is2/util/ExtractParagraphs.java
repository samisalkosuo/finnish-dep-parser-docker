package is2.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.StringTokenizer;

public class ExtractParagraphs {

	/**

	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {

		if (args.length<1) {
			System.out.println("Please provide a file name.");
			System.exit(0);
		}
		
		File file = new File(args[0]);
		file.isDirectory();
		String[] dirs = file.list();
	
		BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]),"UTF-8"),32768);
		int cnt=0;
		
for (String fileName : dirs) {		
	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]+fileName),"UTF-8"),32768);
	
		
		
	
		int state =0;
	
		String s;
		while ((s = reader.readLine()) != null) {
			
			if (s.startsWith("<P>")||s.startsWith("<p>")) {
				state=1; // paragraph start
				continue;
			}
			
		
			
			
			if (s.startsWith("</P>")||s.startsWith("</p>")) {
				state=2; // paragraph end
				write.newLine();
			}
			
			boolean lastNL =false;
			if (state==1) {
				String sp[] = s.split("\\. ");
				for(String p : sp) {
					write.write(p);
	//				if (sp.length>1) write.newLine();
				}
				cnt++;
			}
		}
		
		//if (cnt>5000) break;
		
		reader.close();
}
		write.flush();
		write.close();
		
		System.out.println("Extract "+cnt+" lines ");
		
			
	}
	
	
}
