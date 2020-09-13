Articulated
===

Toy project to scrape articles from [nyasatimes.com](https://www.nyasatimes.com) and possibly other sites and play with that data.

Particularly, I want to play with some new features of Java including:
- `HttpClient` async requests, 
- `var` keyword
- `Virtual Threads`
- Switch Expressions,
- ZGC
- etc..

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

## Building

You will need to have Java 16 and the latest Apache Maven, I recommend using IntelliJ's latest IDEA IDE.
