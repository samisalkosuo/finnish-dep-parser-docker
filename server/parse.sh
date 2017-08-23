#!/bin/bash
# Parse a tagged conll-09 file

OUTPUT_FILE=$TMPDIR/input_parsed_raw.conll09
#Call local java servlet, replaces call to java process
#accepts input in standard input
curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876/annaparser > $OUTPUT_FILE

cat $OUTPUT_FILE | $PYTHON conllUtil.py --swap HEAD:=PHEAD,DEPREL:=PDEPREL
