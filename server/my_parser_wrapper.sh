#this script iscustom parser wrapper to be called from servlet

cat $TMPDIR/$INPUT_TEXT_FILE |  ./parse_conll.sh > $TMPDIR/$OUTPUT_CONLLU_FILE 2> $TMPDIR/$ERROR_FILE
