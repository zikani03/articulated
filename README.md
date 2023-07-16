Articulated
===

Toy project to scrape articles from some Malawian news sites to play with that data.
This project uses an SQLite database to store the articles and runs a webserver to
allow you to interact with the downloaded articles.

## Ideas and Objectives

What interesting things can we do:

- ~~Estimate reading times for the articles~~
- ~~Use sqlite for caching articles~~
- ~~Word frequency count~~
- ~~Named Entity Extraction (places, people, businesses, etc..)~~ See [docs/ner.md](docs/ner.md)
- ~~Use Full Text Search functionalities of SQLite~~
- ~~Use new built-in `HttpClient` for HTTP (async) requests~~
- ~~Use new Java language features: `var` keyword, `record` and  Switch Expressions~~
- ~~Publish Articles to a Kafka broker~~
- ~~Use Greypot to generate PDF from an article~~
- Use [Tribuo](https://tribuo.org/learn/4.3/tutorials/irises-tribuo-v4.html) for basic article classification
- Try to use [Formula Engine](https://github.com/salesforce/formula-engine) for search queries??
- Implement Markov Title Generator with Java
- Use Greypot to generate a basic newsletter like PDF of the articles (e.g. articles for month of September)
- Use jib to build the container for the project
- Text to speech (Amazon Polly??)
- Download images from the article and Base64 encode them for storage
- Play with String compression algorithms
- Use `Virtual Threads`for running the webscrapers (waiting for Project Loom to reach GA :))
- Integrate Cloudy for rendering semantic word clouds from articles
- **NLP-esque** tasks
  - Find out how much money has been mentioned on NyasaTimes since 2016 categorized by keywords (donates, funds, wins, receives, aid etc..)
  - Group articles by Keywords
  - Group articles by named entities
  - Extract quotes from articles **"..." He said**, **"..." said Ndani ndani**
  - Find/detect (Malawian) locations mentioned in the articles
  - Given an article, find related articles by named entity, location or amount range
  - Use Lucene Tokenizer to get words which can be used with CRAFTML for labeling
  - Add "pub/sub" for keywords - enable a person to subscribe to a keyword and send a webhook when we have an article that matches that keyword.

## Supported sites so far

- [Nyasa Times](https://www.nyasatimes.com)
- [Malawi24](https://malawi24.com)
- ADD SUPPORT FOR mbc.mw ref https://mbc.mw/gess-to-be-awarded-in-london/
- ADD SUPPORT FOR times.mw ref https://times.mw/malawi-egypt-in-fertiliser-talks/
- ADD SUPPORT FOR https://mwnation.com/ .. ref: https://mwnation.com/tough-going-for-poor-households/
- 

## Building / running

You will need to have Java JDK 17 and the latest Apache Maven, I recommend using IntelliJ's latest IDEA IDE.

```sh
$ git clone https://github.com/zikani03/articulated.git

$ cd articulated

$ mvn clean compile package

$ java --enable-preview -Dspark.port="4567" -Dneria.url="https://neria-fly.fly.dev" -jar target\articulated.jar 
```

This runs a webserver on port `localhost:4567` you can use the following curl request to download sports articles...

```sh
$ curl -X POST http://localhost:4567/articles/download/nyasatimes/sports?from=1&to=10
```


> DISCLAIMER: The content scraped from the sites is property of those publishers and this is intended for personal use. If you want to use this for some other purposes e.g. commercial purposes, speak to a lawyer. I am not a lawyer. YMMV  

---

Copyright (c) Zikani Nyirenda Mwase