// Implements a Kafka Consumer that receives article text from a topic, extracts Named Entities
// and tries to find which articles the named entities appear in
package main

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/twmb/franz-go/pkg/kgo"
	"io/ioutil"
	"log"
	"net/http"
	//"strconv"
	//"os"
)

type ArticlesResponse struct {
	Articles []Article `json:"articles"`
}

type Article struct {
	ID     string `json:"id"`
	URL    string `json:"url"`
	Author string `json:"author"`
	Body   string `json:"body"`
}

func main() {
	resp, err := http.Get("http://localhost:4567/articles")
	if err != nil {
		panic(err)
	}
	if resp.StatusCode != 200 {
		panic(fmt.Errorf("got invalid response.StatusCode"))
	}
	defer resp.Body.Close()
	data, err := ioutil.ReadAll(resp.Body)

	var articles ArticlesResponse
	err = json.Unmarshal(data, &articles)

	if err != nil {
		panic(err)
	}

    //producerId := strconv.FormatInt(int64(os.Getpid()), 10)

	seeds := []string{"localhost:9092"}
	cl, err := kgo.NewClient(
		kgo.SeedBrokers(seeds...),
		//kgo.TransactionalID(producerId),
		kgo.AllowAutoTopicCreation(),
		kgo.ConsumeTopics("article_ner_finder"),
		kgo.DefaultProduceTopic("article_ner_finder"),
	)
	if err != nil {
		log.Fatal(err)
	}
	defer cl.Close()

	ctx := context.Background()

	for _, article := range articles.Articles {

		articleRecord := kgo.KeyStringRecord(article.URL, article.Body)
        articleRecord.Topic = "article_ner_finder"

		if err := cl.ProduceSync(ctx, articleRecord).FirstErr(); err != nil {
		    fmt.Printf("failed to publish got error %v", err)
		    break;
		}

		// Flush all of the buffered messages.
        //
        // Flush only returns an error if the context was canceled, and
        // it is highly not recommended to cancel the context.
        if err := cl.Flush(ctx); err != nil {
            fmt.Printf("flush was killed due to context cancelation\n")
            break // nothing to do here, since error means context was canceled
        }

        fmt.Println("Published article to Kafka broker", article.URL)
	}
}
