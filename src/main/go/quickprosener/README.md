quickprosener
=============

This is a quick and dirty Named Entiry Recognition service intended for use with articulated.
It is written in Go and uses the prose library for identifying entities in a plain text document.

There is only one endpoint the main `http://localhost:4000/`

## Request
```http
POST /
Content-Type: text/plain;charset=utf-8

<some-text>
```

## Response

```json
{
  "entities": [
    {
      "name": "<some name>",
      "entityType": "GPE|PERSON"
    }
  ]
}
```