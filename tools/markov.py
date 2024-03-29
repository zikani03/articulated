import re
import sys
from collections import defaultdict
from random import random
## Adapted from https://gist.github.com/grantslatton/7694811

db = open("./titles.txt")
titles = db.read().split('\n')


markov_map = defaultdict(lambda:defaultdict(int))

lookback = 2

#Generate map in the form word1 -> word2 -> occurences of word2 after word1
for title in titles[:-1]:
    title = title.split()
    if len(title) > lookback:
        for i in range(len(title)+1):
            markov_map[' '.join(title[max(0,i-lookback):i])][' '.join(title[i:i+1])] += 1

#Convert map to the word1 -> word2 -> probability of word2 after word1
for word, following in markov_map.items():
    total = float(sum(following.values()))
    for key in following:
        following[key] /= total

#Typical sampling from a categorical distribution
def sample(items):
    next_word = None
    t = 0.0
    for k, v in items:
        t += v
        if t and random() < v/t:
            next_word = k
    return next_word

sentences = []
while len(sentences) < 100:
    sentence = []
    next_word = sample(markov_map[''].items())
    while next_word != '':
        sentence.append(next_word)
        next_word = sample(markov_map[' '.join(sentence[-lookback:])].items())
    sentence = ' '.join(sentence)
    flag = True
    for title in titles: #Prune titles that are substrings of actual titles
        if sentence in title:
            flag = False
            break
    if flag:
        sentences.append(sentence)

for sentence in sentences:
    print(sentence)
