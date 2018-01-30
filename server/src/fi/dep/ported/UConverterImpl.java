package fi.dep.ported;

import java.util.StringTokenizer;

public class UConverterImpl implements UConverter {

	public ParserLog log;
	
	public UConverterImpl(ParserLog logIn) {
		this.log = logIn;
	}
	
	@Override
	public String convert09toU(String input) throws Exception {

		StringBuffer sb = new StringBuffer();
		
		int lineNumber = 0;
		
		StringTokenizer st = new StringTokenizer(input,"\n",false);

		while(st.hasMoreTokens()) {
			lineNumber++;
			String lause = st.nextToken();
			log.debug(lineNumber+":"+lause);

			// lets just add comments and empty lines
			if(lause != null && ("".equals(lause.trim()) || "#".equals(lause.substring(0, 1)))) {
				sb.append(lause+"\n");
			} else {
				// then we want to split this lause into words
				StringTokenizer wordt = new StringTokenizer(lause,"\t",false);
				
				String ID = wordt.nextToken();
				String FORM = wordt.nextToken();
				String LEMMA = wordt.nextToken();
				String UCPOS = wordt.nextToken();
				String UPOS = wordt.nextToken();
				String UFEAT = wordt.nextToken();
				String UHEAD = wordt.nextToken();
				String UDEPREL = wordt.nextToken();
				String UDEPS = wordt.nextToken();
				String UMISC = wordt.nextToken();
				
				//  print '\t'.join((cols[ID],cols[FORM],cols[LEMMA],cols[LEMMA],cols[UCPOS],cols[UCPOS],cols[UFEAT],cols[UFEAT],cols[UHEAD],cols[UHEAD],cols[UDEPREL],cols[UDEPREL],'_','_'))
				sb.append(ID+"\t"+FORM+"\t"+LEMMA+ "\t"+LEMMA+ "\t"+UCPOS + "\t"+UCPOS + "\t"+UFEAT+ "\t"+UFEAT+  "\t"+UHEAD+ "\t"+UHEAD+ "\t"+UDEPREL+   "\t"+UDEPREL+ "\t"+"_"+ "\t"+"_" +"\n");
							
			}		
		}
		return sb.toString();

		
		// This method is for transoforming to convu 09 format
		/*
		 ID,FORM,LEMMA,UCPOS,UPOS,UFEAT,UHEAD,UDEPREL,UDEPS,UMISC=range(10)
ID,FORM,LEMMA,PLEMMA,POS,PPOS,FEAT,PFEAT,HEAD,PHEAD,DEPREL,PDEPREL=range(12)

if __name__=="__main__":
    parser = argparse.ArgumentParser(description='Convert conllu to conll09 and back. Infers the direction on its own if no arguments given.')
    parser.add_argument('--output-format', default=None, help='Output format can be "u" or "09". If the input is in this format already, the conversion is a no-op and simply passes data through.')
    parser.add_argument('--drop-comments', default=False, action="store_true", help='Remove comments from the data')
    args = parser.parse_args()
    
    for line in sys.stdin:
        line=line.strip()
        if not line:
            print
        elif line.startswith('#'):
            if not args.drop_comments:
                print line
        else:
            cols=line.split('\t')
            if len(cols)==10:
                #UD in
                if args.output_format=="u":
                    #UD out, no-op
                    print '\t'.join(cols)
                else:
                    #UD -> 09
                    print '\t'.join((cols[ID],cols[FORM],cols[LEMMA],cols[LEMMA],cols[UCPOS],cols[UCPOS],cols[UFEAT],cols[UFEAT],cols[UHEAD],cols[UHEAD],cols[UDEPREL],cols[UDEPREL],'_','_'))
            else:
                #09 in
                assert len(cols) in (12,13,14), cols
                if args.output_format=="09":
                    #09 out, no-op
                    print '\t'.join(cols)
                else:
                    #09 -> UD
                    print '\t'.join((cols[ID],cols[FORM],cols[PLEMMA],cols[PPOS],'_',cols[PFEAT],cols[PHEAD],cols[PDEPREL],'_','_'))
		 */
		
	}

	@Override
	public String convertUto09(String input)  throws Exception {
		StringBuffer sb = new StringBuffer();
		
		int lineNumber = 0;
		
		StringTokenizer st = new StringTokenizer(input,"\n",false);

		while(st.hasMoreTokens()) {
			lineNumber++;
			String lause = st.nextToken();
			log.debug(lineNumber+":"+lause);

			// lets just add comments and empty lines
			if(lause != null && ("".equals(lause.trim()) || "#".equals(lause.substring(0, 1)))) {
				sb.append(lause+"\n");
			} else {
				// then we want to split this lause into words
				StringTokenizer wordt = new StringTokenizer(lause,"\t",false);
				// We could use 'ConluLine' object too 
				String ID = wordt.nextToken();
				String FORM = wordt.nextToken();
				String LEMMA = wordt.nextToken();
				String PLEMMA = wordt.nextToken();
				String POS = wordt.nextToken();
				String PPOS = wordt.nextToken();
				String FEAT = wordt.nextToken();
				String PFEAT = wordt.nextToken();
				String HEAD = wordt.nextToken();
				String PHEAD = wordt.nextToken();
				String DEPREL = wordt.nextToken();
				String PDEPREL = wordt.nextToken();
				//print '\t'.join((cols[ID],cols[FORM],cols[PLEMMA],cols[PPOS],'_',cols[PFEAT],cols[PHEAD],cols[PDEPREL],'_','_'))
				sb.append(ID+"\t"+FORM+"\t"+PLEMMA+"\t"+PPOS+"\t"+"_"+"\t"+PFEAT+"\t"+PHEAD+"\t"+PDEPREL+"\t" +"_"+"\t"+"_"+"\n");	
			}		
		}
		return sb.toString();
	}

}
