import cPickle as pickle
import gzip
import sqlite3
conn = sqlite3.connect('model/vocab-fi.sqlite')
c = conn.cursor()
c.execute('CREATE TABLE word_counts (word text, count int)')

with gzip.open("model/vocab-fi.pickle.gz","rb") as f:
  word_counts=pickle.load(f)

for key in word_counts.keys():
  c.execute("INSERT INTO word_counts VALUES ('%s',%d)" % (key.encode('utf-8').strip(),word_counts.get(key)))
  print key.encode('utf-8').strip()+","+str(word_counts.get(key))
conn.commit()
conn.close()