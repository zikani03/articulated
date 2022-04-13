Articulated
===

Toy project to scrape articles from some Malawian news sites to play with that data.
This project uses an SQLite database to store the articles and runs a webserver to
allow you to interact with the downloaded articles.

Some cool stuff we use new/preview features of Java including:
- `HttpClient` async requests, 
- `var` keyword
- `Virtual Threads`
- `record`s
- Switch Expressions,

## Ideas and TODOs

What interesting things can we do with articles extracted from nyasatimes? Well we could:

- ~~Estimate reading times for the articles~~
- ~~Use sqlite for caching articles~~
- ~~Word frequency count~~
- Create a newsletter like PDF of the articles (e.g. articles for month of September)
- Text to speech (Amazon Polly??)
- Download images from the article and Base64 encode them for storage
- Named Entity Extraction (places, people, businesses, etc..)
- Play with String compression algorithms
- Extract quotes from articles **"..." He said**, **"..." said Ndani ndani**
- Find out how much money has been mentioned on NyasaTimes since 2016 categorized by keywords (donates, funds, wins, receives, aid etc..)
- ~Use virtualthreads for running the webscrapers~
- Integrate Cloudy for semantic word clouds

## Supported sites so far

- [Nyasa Times](https://www.nyasatimes.com)
- [Malawi24](https://malawi24.com)

## Building / running

You will need to have Java JDK 17 and the latest Apache Maven, I recommend using IntelliJ's latest IDEA IDE.

```sh
$ git clone https://github.com/zikani03/articulated.git

$ cd articulated

$ mvn clean compile package

$ java --enable-preview -jar target\articulated.jar 
```

This runs a webserver on port `localhost:4567` you can use the following curl request to download sports articles...

```sh
$ curl -X POST http://localhost:4567/articles/download/nyasatimes/sports?from=1&to=10
```


> DISCLAIMER: The content scraped from the sites is property of those publishers and this is intended for personal use. If you want to use this for some other purposes e.g. commercial purposes, speak to a lawyer. I am not a lawyer. YMMV  

---

Copyright (c) Zikani Nyirenda Mwase