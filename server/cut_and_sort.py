# -*- coding: utf-8 -*-
import sys

#Replaces "cut -f 2 | sort -u" in tag.sh
#by replacing one pipe, there one process less to execute when parsing text
#--> parsing speed increases

allTokens=[]

for line in sys.stdin:
    line=unicode(line,"utf-8").strip()
    cols=line.split('\t')
    if len(cols)>=2:
        #use only field 2
        token=cols[1]
    else:
        token=line
    if not token in allTokens:
        allTokens.append(token)

allTokens.sort()

size=len(allTokens)

for token in allTokens:
    print token.encode(u"utf-8")

