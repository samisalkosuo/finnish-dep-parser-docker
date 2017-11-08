import codecs
import sys
import os
import re
import unicodedata as udata
import subprocess
import logging
logging.basicConfig(level=logging.WARNING)

import traceback
import omorfi_pos as omor
import cPickle as pickle
import gzip

#remember to import these to call marmot servlet
import urllib
import urllib2


def load_readings(m_readings):
    words={} #{wordform -> set of (lemma,pos,feat)}
    with codecs.open(m_readings,"r","utf-8") as f:
        for line in f:
            line=line.rstrip(u'\n')
            if not line:
                continue
            form, lemma, pos, feat=line.split(u'\t')
            words.setdefault(form,set()).add((lemma,pos,feat))
    return words

def score(ppos,pfeat,pos,feat):
    s=0
    if ppos==pos:
        s+=5
    pfeat_set=set(pfeat.split(u"|"))
    feat_set=set(feat.split(u"|"))
    s+=len(pfeat_set & feat_set)
    return s

wordsLooked=[]
wordCounts=dict()

def getWordCount(word):
    
    if word in wordsLooked:
        return wordCounts[word]
    else:
        data = {}
        data['wordCount']=word.encode('utf-8')
        url_values = urllib.urlencode(data)
        url = 'http://127.0.0.1:9876/marmot'
        full_url = url + '?' + url_values
        #print >> sys.stderr, "marmot-tag, url: %s" % full_url
        response = urllib2.urlopen(full_url)
        data = response.read()
        print >> sys.stderr, "marmot-tag get word count, result: %s" % data
        wordsLooked.append(word)
        count=int(data)
        wordCounts[word]=count
        return count

def best_reading(plemma,ppos,pfeat,readings,word_counts={}):
    if not readings:
        return plemma,ppos,pfeat
    alternatives=list(((lemma,pos,feat,(score(ppos,pfeat,pos,feat),-lemma.count(u"#"),getWordCount(lemma))) for lemma,pos,feat in readings))
    #alternatives=list(((lemma,pos,feat,(score(ppos,pfeat,pos,feat),-lemma.count(u"#"),word_counts.get(lemma,0))) for lemma,pos,feat in readings))
#    print >> sys.stderr, "ALTERNATIVES", alternatives
    best=max(alternatives,key=lambda k:k[3])
    if options.hard: 
        return best[0],best[1],best[2]
    elif options.hardpos and ppos in (x[1] for x in readings): ###Uncomment to improve your LAS by 2pp :)
        return best[0],best[1],best[2]
    else:
        return best[0],ppos,pfeat

if __name__=="__main__":
    log = logging.getLogger("omorfi")
    from optparse import OptionParser
    parser = OptionParser()
    parser.add_option("-t", "--train", dest="train",action="store_true",default=False, help="Prepare training data")
    parser.add_option("--tempdir", dest="tempdir",action="store",default=".", help="Where temporary files should be kept. Default to current dir.")
    parser.add_option("-m", "--model", dest="model",action="store", default=None,help="Fill PLEMMA/PPOS/PFEAT using this marmot model",metavar="MODELFILE")
    parser.add_option("--marmot", dest="marmotbin",action="store", default=None,help="marmot .jar file")
    parser.add_option("--mreadings",action="store", default=None,help="File with the morphological readings")
    parser.add_option("--ud",action="store_true", default=False,help="UD")
    parser.add_option("--hard",action="store_true", default=False,help="Use OMorFi hard constraint.")
    parser.add_option("--hardpos",action="store_true", default=False,help="Use OMorFi hard constraint if pos matches.")
    parser.add_option("--word-counts",dest="wordcounts",action="store",default=None, help="Pickled dictionary of words and counts, used as an optional hint when resolving ambiguities")
    (options, args) = parser.parse_args()

    if options.mreadings:
        readings=load_readings(options.mreadings)
    else:
        readings=None
#no load, always used from servlet
#    if options.wordcounts:
#        with gzip.open(options.wordcounts,"rb") as f:
#            word_counts=pickle.load(f)
#    else:
#        
    word_counts={}

    if options.train:
        for line in sys.stdin:
            line=unicode(line,"utf-8").strip()
            if line.startswith(u"#"):
                continue
            if not line:
                print
                continue
            cols=line.split(u"\t")
            if len(cols)==10: #UD
                idx,token,pos,feat=int(cols[0]),cols[1],cols[3],cols[5]
            else:
                assert len(cols) in (13,14,15)
                idx,token,pos,feat=int(cols[0]),cols[1],cols[4],cols[6]
            tagList=[None for x in range(17)]
            #tagList[0]=pos
            if options.ud:
                pos_set=set(x[1] for x in readings.get(token,[]))
                s=feat
            else:
                if feat!=u"_":
                    for cat_tag in feat.split(u"|"):
                        if u"=" in cat_tag:
                            cat,tag=cat_tag.split(u"=",1)
                        else:
                            cat,tag=cat_tag.split(u"_",1)
                        if cat not in omor.cat2idx:
                            print >> sys.stderr, "Unknown cat:", cat
                        else:
                            tagList[omor.cat2idx[cat]]=tag
                s=omor.hun_taglist2tagstring(tagList)
                pos_set=set(omor.hun_possiblepos(token))
            #pos_set.add(pos)
            marmot_feats=u"#".join(u"POS_"+x for x in sorted(pos_set))
            if not marmot_feats:
                marmot_feats=u"_"
            if not s:
                s=u"_"
            print (unicode(idx-1)+u"\t"+token+u"\t"+pos+u"\t"+s+u"\t"+marmot_feats).encode("utf-8")
    elif options.model!=None:
        f=codecs.open(os.path.join(options.tempdir,"marmot_in"),"wt","utf-8")
        lines=[]
        for line in sys.stdin:
            line=unicode(line,"utf-8").strip()
            if line.startswith(u"#"):
                continue
            lines.append(line)
            cols=line.split(u"\t")
            if len(cols)==1:
                print >> f
                continue
            else:                
                assert len(cols) in (10,13,14,15)
                if options.ud:
                    pos_set=set(x[1] for x in readings.get(cols[1],[]))
                else:
                    pos_set=set(omor.hun_possiblepos(cols[1]))
                marmot_feats=u"#".join(u"POS_"+x for x in sorted(pos_set))
                if not marmot_feats:
                    marmot_feats=u"_"
                print >> f, cols[1]+u"\t"+marmot_feats #wordform tab feats
        f.close()
        #Now invoke marmot
        try:
            name_in=os.path.join(options.tempdir,"marmot_in")
            name_out=os.path.join(options.tempdir,"marmot_out")
            
            
            #calls marmotservlet instead of starting subprocess
            data = {}
            data['predfile']=name_out
            data['testfile']="form-index=0,token-feature-index=1,"+name_in
            url_values = urllib.urlencode(data)
            url = 'http://127.0.0.1:9876/marmot'
            full_url = url + '?' + url_values
            #print >> sys.stderr, "marmot-tag, url: %s" % full_url
            response = urllib2.urlopen(full_url)
            data = response.read()
            print >> sys.stderr, "marmot-tag, result: %s" % data




            f=codecs.open(name_out,"r","utf-8")
            predictions=[]
            for line in f: #reads in MarMot predictions
                line=line.strip()
                predictions.append(line.split(u"\t"))
            f.close()

            if len(predictions)==0:
                raise ValueError("Empty predictions from Marmot in %s"%(os.path.join(options.tempdir,"marmot_out")))
        except:
            traceback.print_exc()
            log.error("""Did not succeed in launching 'LIBS/%s'. The most common reason for this is that you forgot to run './install.sh'. \n\nGiving up, because the parser cannot run without a tagger."""%(" ".join(args)))
            sys.exit(1)

        while predictions[-1]==[u''] or not predictions[-1]:
            predictions.pop(-1)
        while lines[-1]==u'':
            lines.pop(-1)
        
        newSent=True
        assert len(lines)==len(predictions), (len(lines),len(predictions))
        if options.ud:

            for inLine,pred in zip(lines,predictions):
                inCols=inLine.split(u"\t")
                if len(inCols)==1:
                    assert inCols[0]==u""
                    assert pred==[u""]
                    print
                    newSent=True
                    continue #New sentence starts

                #print  >> sys.stderr, "inLine, inCols[1] and pred[1]"                
                #print >> sys.stderr, inLine.encode("utf-8") 
                #print >> sys.stderr, inCols[1].encode("utf-8") 
                #print >> sys.stderr, pred[1].encode("utf-8") 

                assert inCols[1]==pred[1] #Tokens must match
                txt=inCols[1]
                ppos=pred[5]
                pfeat=pred[7]
                plemma,ppos,pfeat=best_reading(txt,ppos,pfeat,readings.get(txt,[]),word_counts)
                if len(inCols)==10:
                    inCols[2],inCols[3],inCols[5]=plemma,ppos,pfeat
                else:
                    inCols[3],inCols[5],inCols[7]=plemma,ppos,pfeat
                print (u"\t".join(inCols)).encode("utf-8")
                newSent=False

        else:

            for inLine,pred in zip(lines,predictions):
                inCols=inLine.split(u"\t")
                if len(inCols)==1:
                    assert inCols[0]==u""
                    assert pred==[u""]
                    print
                    newSent=True
                    continue #New sentence starts
                assert inCols[1]==pred[1] #Tokens must match
                txt=inCols[1]
#                if omor.is_punct(txt):
#                    plemma,ppos,pfeat=txt,u"Punct",u"_"
#                elif omor.is_num(txt):
#                    plemma,ppos,pfeat=txt,u"Num",u"_"
                if True: #
                    tl=u"POS_"+pred[5]
                    if pred[7]!=u"_":
                        tl+=u"|"+pred[7]
                    plemma,ptaglist=omor.hun_tag2omorfi(pred[1],tl) #Find the ost plausible reading
                    omor.fill_ortho(txt,ptaglist)
                    if txt==u"*null*":
                        ptaglist[omor.cat2idx[u"OTHER"]]=None
                    if options.hard:
                        ppos=ptaglist[0]
                        #Guess proper nouns
        #                if ppos==u"N" and not newSent and ptaglist[omor.cat2idx[u"CASECHANGE"]]==u"Up" and ptaglist[omor.cat2idx[u"OTHER"]]==u"UNK":
        #                    ptaglist[omor.cat2idx[u"SUBCAT"]]=u"Prop"
                        pfeat=[]
                        for cat,tag in zip(omor.cat_list[1:],ptaglist[1:]):
                            if tag!=None:
                                pfeat.append(cat+u"="+tag)
                        if not pfeat:
                            pfeat=u"_"
                        else:
                            pfeat=u"|".join(pfeat)
                    else:
                        #soft 
                        ppos=pred[5]
                        pfeat=re.sub(ur"((^|\|)[A-Z]+)_([a-zA-Z0-9])",ur"\1=\3",pred[7],re.U)
                if len(inCols)==10:
                    inCols[2],inCols[3],inCols[5]=plemma.replace(u"|",u"#"),ppos,pfeat
                else:
                    inCols[3],inCols[5],inCols[7]=plemma.replace(u"|",u"#"),ppos,pfeat
                print (u"\t".join(inCols)).encode("utf-8")
                newSent=False
