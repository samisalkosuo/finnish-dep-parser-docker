#this script is custom parser wrapper to be called from servlet
#replaces parse_conll.sh, parse.sh scripts

#accepts input in standard input
#preprocesses, parses and postprocesses text & conllu

cat $TMPDIR/$INPUT_TEXT_FILE | $PYTHON conv_u_09.py --output=09 | $PYTHON limit_sentence_len.py -N $MAX_SEN_LEN -C $SEN_CHUNK | ./tag.sh  | curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876/annaparser | $PYTHON conllUtil.py --swap HEAD:=PHEAD,DEPREL:=PDEPREL | $PYTHON limit_sentence_len.py --reverse | $PYTHON conv_u_09.py --output=u > $TMPDIR/$OUTPUT_CONLLU_FILE 2> $TMPDIR/$ERROR_FILE

