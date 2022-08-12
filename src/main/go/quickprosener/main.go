// MIT License
//
// Copyright (c) 2020 - 2022 Zikani Nyirenda Mwase and Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
package main

import (
	"encoding/json"
	"io/ioutil"
	"net/http"

	"github.com/DavidBelicza/TextRank/v2"
	"github.com/jdkato/prose/v2"
)

type NERResult struct {
	Entities       []NamedEntity `json:"entities"`
	TextRankResult interface{}   `json:"rankedPhrases"`
}

type NamedEntity struct {
	EntityType string `json:"entityType"`
	Name       string `json:"name"`
}

func main() {

	handler := func(w http.ResponseWriter, r *http.Request) {
		data, err := ioutil.ReadAll(r.Body)
		if err != nil {
			w.WriteHeader(500)
			return
		}

		dataString := string(data)

		tr := textrank.NewTextRank()
		rule := textrank.NewDefaultRule()
		language := textrank.NewDefaultLanguage()
		algo := textrank.NewDefaultAlgorithm()

		tr.Populate(dataString, language, rule)
		tr.Ranking(algo)

		// 		rankedPhrases := textrank.FindPhrases(tr)
		sentences := textrank.FindSentencesByRelationWeight(tr, 10)

		doc, _ := prose.NewDocument(dataString)
		result := NERResult{
			Entities:       make([]NamedEntity, 0),
			TextRankResult: sentences,
		}

		for _, ent := range doc.Entities() {
			result.Entities = append(result.Entities, NamedEntity{EntityType: ent.Label, Name: ent.Text})
		}
		data, err = json.Marshal(result)
		if err != nil {
			return
		}
		w.WriteHeader(200)
		_, err = w.Write(data)
		if err != nil {
			w.WriteHeader(500)
			return
		}
	}

	err := http.ListenAndServe(":4000", http.HandlerFunc(handler))
	if err != nil {
		panic(err)
	}
}
