#!/bin/sh

sqlite3 articulated.db 'select title from articles;' > titles.txt
python3 markov.py

