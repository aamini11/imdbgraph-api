# IMDB Graph
The backend API code for www.imdbgraph.org/. This repo scrapes ratings data from 
IMDB daily and provides that data as an easy to consume REST API. The frontend 
code that consumes this API is available 
[here](https://gitlab.com/aamini11/imdbgraph-client).

Note: See [Scrapper.java](src/main/java/org/aria/imdbgraph/api/ratings/scrapper) 
for the class responsible for implementing this scrapping behavior.

## Endpoints

This app is really simple and only supports 2 endpoints:

https://api.imdbgraph.org/search?q=[anyquery]

https://api.imdbgraph.org/ratings/[showId]

## Examples 

https://api.imdbgraph.org/search?q=breaking would return:

```json
[
  {
    "imdbId": "tt0903747",
    "title": "Breaking Bad",
    "startYear": "2008",
    "endYear": "2013",
    "showRating": 9.5,
    "numVotes": 1949748
  },
  {
    "imdbId": "tt3865236",
    "title": "Into the Badlands",
    "startYear": "2015",
    "endYear": "2019",
    "showRating": 7.9,
    "numVotes": 47690
  },
  {
    "imdbId": "tt12708542",
    "title": "Star Wars: The Bad Batch",
    "startYear": "2021",
    "endYear": null,
    "showRating": 7.8,
    "numVotes": 41215
  },
  {
    "imdbId": "tt15469618",
    "title": "Bad Sisters",
    "startYear": "2022",
    "endYear": null,
    "showRating": 8.3,
    "numVotes": 23215
  }
  // ...
]
```

https://api.imdbgraph.org/ratings/tt26687196 would return:

```json
{
  "show": {
    "imdbId": "tt26687196",
    "title": "Waco: American Apocalypse",
    "startYear": "2023",
    "endYear": "2023",
    "showRating": 7.1,
    "numVotes": 2497
  },
  "allEpisodeRatings": {
    "1": {
      "1": {
        "episodeTitle": "In the Beginning...",
        "season": 1,
        "episodeNumber": 1,
        "imdbRating": 7.5,
        "numVotes": 164
      },
      "2": {
        "episodeTitle": "Children of God",
        "season": 1,
        "episodeNumber": 2,
        "imdbRating": 7.3,
        "numVotes": 148
      },
      "3": {
        "episodeTitle": "Fire",
        "season": 1,
        "episodeNumber": 3,
        "imdbRating": 7.4,
        "numVotes": 142
      }
    }
  }
}
```

### Copyright
Copyright Notice: Information courtesy of IMDb (http://www.imdb.com). Used with permission.
