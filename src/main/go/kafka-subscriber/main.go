// Implements a Kafka Consumer that receives article text from a topic, extracts Named Entities
// and tries to find which articles the named entities appear in
package main

import (
	"log"
	"bytes"
	"net/http"
	"fmt"
	"io/ioutil"
	"context"
	"encoding/json"
	"github.com/twmb/franz-go/pkg/kgo"
)


func main() {
	seeds := []string{ "localhost:9092" }
	cl, err := kgo.NewClient(
		kgo.SeedBrokers(seeds...),
		kgo.ConsumeTopics("article_ner_finder"),
	)
	if err != nil {
		log.Fatal(err)
	}
	defer cl.Close()

	ctx := context.Background()
	
	for {
		fetches := cl.PollFetches(ctx)

		if errs := fetches.Errors(); len(errs) > 0 {
			panic(fmt.Sprintf("%v", errs))
		}

		iter := fetches.RecordIter()
		for !iter.Done() {
			record := iter.Next()
			err = extractEntities(string(record.Key), string(record.Value))
			if err != nil {
				log.Fatalf("failed to extract enties from %s got %v", string(record.Key), err)
			}
		}
	}
}

type NeriaEvent struct {
	URL      string `json:"Url"`
	Selector string `json:"Selector"`
	Text     string `json:"Text"`
}


func extractEntities(url, body string) error {
	
	event := NeriaEvent{
		URL: url,
		Text: body,
	}

	data, err := json.Marshal(event)
	if err != nil {
		return err
	}

	res, err := http.Post("https://neria-fly.fly.dev", "application/json", bytes.NewReader(data))

	if err != nil {
		return err 
	}
	
	if res.StatusCode != 200 {
		return fmt.Errorf("got invalid response.StatusCode")
	}
	
	entitiesJSON, err := ioutil.ReadAll(res.Body)
	if err != nil {
		return err 
	}
	fmt.Println("-----------------------------------------------")
	fmt.Printf("Article: %s \n\t%s\n", url, string(entitiesJSON))
	fmt.Println("-----------------------------------------------")
	return nil
}
