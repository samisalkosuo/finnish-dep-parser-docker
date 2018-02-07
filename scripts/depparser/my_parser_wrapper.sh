#this script is custom parser wrapper to be called from servlet
#replaces parse_conll.sh, parse.sh scripts

#accepts input in standard input
#preprocesses, parses and postprocesses text & conllu
#cat $TMPDIR/$INPUT_TEXT_FILE | $PYTHON conv_u_09.py --output=09 | $PYTHON limit_sentence_len.py -N $MAX_SEN_LEN -C $SEN_CHUNK | ./tag.sh  | curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876/annaparser | $PYTHON limit_sentence_len.py --reverse | $PYTHON conv_u_09.py --output=u > $TMPDIR/$OUTPUT_CONLLU_FILE 2> $TMPDIR/$ERROR_FILE

#these are used when developing
#__tmpfile=$TMPDIR/after_tag.sh.txt
#cat $TMPDIR/$INPUT_TEXT_FILE |  $PYTHON conv_u_09.py --output=09 | $PYTHON limit_sentence_len.py -N $MAX_SEN_LEN -C $SEN_CHUNK | ./tag.sh  > $__tmpfile 2> error.txt
#cat $__tmpfile | curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876/annaparser | $PYTHON conllUtil.py --swap HEAD:=PHEAD,DEPREL:=PDEPREL | $PYTHON limit_sentence_len.py --reverse | $PYTHON conv_u_09.py --output=u > $TMPDIR/$OUTPUT_CONLLU_FILE 2> $TMPDIR/$ERROR_FILE

#combined my_parser_wrapper.sh and tag.sh is below

#cat $TMPDIR/$INPUT_TEXT_FILE | $PYTHON conv_u_09.py --output=09 | $PYTHON limit_sentence_len.py -N $MAX_SEN_LEN -C $SEN_CHUNK > $TMPDIR/tagger_input.conll09
#removed conv_u_09.py --output=09 because input is already 09.
cat $TMPDIR/$INPUT_TEXT_FILE | $PYTHON limit_sentence_len.py -N $MAX_SEN_LEN -C $SEN_CHUNK > $TMPDIR/tagger_input.conll09

#| ./tag.sh  | curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876/annaparser | $PYTHON limit_sentence_len.py --reverse | $PYTHON conv_u_09.py --output=u > $TMPDIR/$OUTPUT_CONLLU_FILE 2> $TMPDIR/$ERROR_FILE

#start lines from tag.sh
# Morpho analyzis and POS tagging

# Run the input through hunpos and populate the LEMMA,POS,FEAT,PLEMMA,PPOS,PFEAT columns

source init.sh

#cat > $TMPDIR/tagger_input.conll09
#replaced: sort | uniq ==> sort -u
#cat $TMPDIR/tagger_input.conll09 | cut -f 2 | sort -u | $PYTHON omorfi_pos.py > $TMPDIR/all_readings.sd
#replaced: cut -f 2 | sort -u ==> python cut_and_sort.py
#cat $TMPDIR/tagger_input.conll09 | python cut_and_sort.py | $PYTHON omorfi_pos.py > $TMPDIR/all_readings.sd
cat $TMPDIR/tagger_input.conll09 | $PYTHON omorfi_pos_modified.py > $TMPDIR/all_readings.sd


cd morpho-sd2ud
./run.sh $TMPDIR/morpho_conv_tmp $TMPDIR/all_readings.sd $TMPDIR/all_readings.ud
cd ..

#uses IS parser servlet
#cat $TMPDIR/tagger_input.conll09 | $PYTHON marmot-tag.py --marmot $THIS/LIBS/marmot.jar --tempdir $TMPDIR --ud --hardpos --mreadings $TMPDIR/all_readings.ud --word-counts model/vocab-fi.pickle.gz -m model/fin_model.marmot |  curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876/annaparser | $PYTHON limit_sentence_len.py --reverse | $PYTHON conv_u_09.py --output=u > $TMPDIR/$OUTPUT_CONLLU_FILE 2> $TMPDIR/$ERROR_FILE
cat $TMPDIR/tagger_input.conll09 | $PYTHON marmot-tag.py --marmot $THIS/LIBS/marmot.jar --tempdir $TMPDIR --ud --hardpos --mreadings $TMPDIR/all_readings.ud --word-counts model/vocab-fi.pickle.gz -m model/fin_model.marmot |  curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876/annaparser | $PYTHON limit_sentence_len.py --reverse  > $TMPDIR/$OUTPUT_CONLLU_FILE 2> $TMPDIR/$ERROR_FILE

#cat $TMPDIR/tagger_input.conll09 | $PYTHON marmot-tag.py --marmot $THIS/LIBS/marmot.jar --tempdir $TMPDIR --ud --hardpos --mreadings $TMPDIR/all_readings.ud --word-counts model/vocab-fi.pickle.gz -m model/fin_model.marmot > $TMPDIR/tag_sh_output.conll09
#end lines from tag.sh
#cat $TMPDIR/tag_sh_output.conll09 | curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876/annaparser | $PYTHON limit_sentence_len.py --reverse | $PYTHON conv_u_09.py --output=u > $TMPDIR/$OUTPUT_CONLLU_FILE 2> $TMPDIR/$ERROR_FILE
