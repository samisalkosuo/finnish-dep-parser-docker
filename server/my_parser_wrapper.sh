#this script is custom parser wrapper to be called from servlet
#replaces also parse_conll.sh script

#tag the input, and fill in lemmas
# MAX_SEN_LEN and SEN_CHUNK are defined in servlet
cat $TMPDIR/$INPUT_TEXT_FILE | $PYTHON conv_u_09.py --output=09 | $PYTHON limit_sentence_len.py -N $MAX_SEN_LEN -C $SEN_CHUNK | ./tag.sh > $TMPDIR/input_tagged.conll09

if [[ $? -ne 0 ]]
then
    echo "Tagging of the input failed. Please check the error messages above for any help" 1>&2
    exit 1
fi

#parse
cat $TMPDIR/input_tagged.conll09 | ./parse.sh  > $TMPDIR/input_parsed.conll09

#reverse: tag the input, and fill in lemmas
cat $TMPDIR/input_parsed.conll09 | $PYTHON limit_sentence_len.py --reverse | python conv_u_09.py --output=u > $TMPDIR/$OUTPUT_CONLLU_FILE 2> $TMPDIR/$ERROR_FILE
