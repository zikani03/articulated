NER
===

It has been a long time goal to get some basic Named Entity Recognition functionality in this project.
I managed to do that using three libraries, the first is a Go library so we have an HTTP service in [quickprosener](../src/main/go/quickprosener/README.md) which uses that library.

I also tried using [Clulab's processors](https://clulab.github.io/processors/processors.html) project for the same. The
CLU lab processors provide two implementations for Named Entity Recognition, their own and 
one based on [Stanford's CoreNLP](https://github.com/stanfordnlp/CoreNLP) project.

I have not added the CoreNLP libraries as they are licensed under GPL which would require me to re-license this project
so if you want to add the CoreNLP libraries for Named Entity Recognition implementation, you can follow the instructions below:

### Using CoreNLP for NER

In order to implement NER with CoreNLP you can use the following steps in your own fork:

Add the following dependency:
```xml
<dependency>
    <groupId>org.clulab</groupId>
    <artifactId>processors-corenlp_2.12</artifactId>
    <version>8.4.9</version>
</dependency>
```

Add this implementation to the  `nlp` package
```java
public class CoreNLPNamedEntityRecognitionService extends AbstractCluNamedEntityRecognitionService {
    private CoreNLPProcessor coreNLPProcessor;

    public CoreNLPNamedEntityRecognitionService() {
        this.coreNLPProcessor = new CoreNLPProcessor(true, false, true, 0, 100);
    }

    @Override
    protected Sentence[] extractSentences(Article article) {
        Document document = coreNLPProcessor.annotate(article.getBody(), false);
        return document.sentences();
    }
}
```

Change the implementation of the NamedRecognition passed to the `ArticleNamedEntitiesResource` in the `Application` class:

```java
Spark.get("/articles/entities/:id", new ArticleNamedEntitiesResource(objectMapper, articleDAO, new CoreNLPNamedEntityRecognitionService()));
```
