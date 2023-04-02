# IMDB Graph
Website to visualize the episode ratings of TV shows using data from IMDB. Website available at: https://www.imdbgraph.org/

Copyright Notice:
Information courtesy of
IMDb
(http://www.imdb.com).
Used with permission.

## Architecture of App

### Overview of App
This repo contains the backend code responsible for scraping data from IMDB and
providing it as an API used by www.imdbgraph.org. The frontend code that
consumes this API is found at https://gitlab.com/aamini11/imdbgraph. 

### How data is scraped
The reason data has to be scrapped is that IMDB doesn't provide an API for
their data. Instead, they provide all their data in large text files that are
updated once a day. This app downloads those files once a day and updates the 
database with that new data. 

### The endpoints
This app is really simple and only supports 2 endpoints:

https://api.imdbgraph.org/search?q=[anyquery]

https://api.imdbgraph.org/ratings/[showId]

The first endpoint just provides a list of show suggestions given
any query. For example https://api.imdbgraph.org/search?q=breaking
would return the following list:

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

The second endpoint returns all the episode ratings for any IMDB TV show.
You just need to provide the IMDB ID for that show. Example:
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
