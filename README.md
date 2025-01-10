# IMDb Graph API

www.imdbgraph.org is a website for visualizing the episode ratings of TV shows
using user data from imdb.com. Because IMDb doesn't provide an API for accessing 
their ratings data directly, this project scrapes data from IMDb and provides a copy of
their data as a REST API. The frontend code for imdbgraph that uses this API is available [here](https://gitlab.com/aamini11/imdbgraph-client).

## Scraping 
Instead of an API, IMDb publishes all their data as text files, and they update
those files once a day. The code responsible for scraping that data from IMDB is
available in this [folder](src/main/java/org/aria/imdbgraph/api/ratings/scraper).

- [ImdbDataScraper.java](src/main/java/org/aria/imdbgraph/api/ratings/ImdbDataScraper.java)
is the code that's run once a day to download the latest files and updates the
database with the data
- [ImdbFileDownloader.java](src/main/java/org/aria/imdbgraph/api/ratings/ImdbFileDownloader.java)
is the helper class used to download the files.

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
    "numVotes": 2249931
  },
  {
    "imdbId": "tt1630574",
    "title": "Breaking In",
    "startYear": "2011",
    "endYear": "2012",
    "showRating": 7.1,
    "numVotes": 7648
  },
  {
    "imdbId": "tt14408016",
    "title": "Now, We Are Breaking Up",
    "startYear": "2021",
    "endYear": "2022",
    "showRating": 6.4,
    "numVotes": 1723
  },
  {
    "imdbId": "tt2387761",
    "title": "Breaking Bad: Original Minisodes",
    "startYear": "2009",
    "endYear": "2011",
    "showRating": 7.6,
    "numVotes": 1700
  },
  {
    "imdbId": "tt11151792",
    "title": "The Road to El Camino: Behind the Scenes of El Camino: A Breaking Bad Movie",
    "startYear": "2019",
    "endYear": null,
    "showRating": 7.1,
    "numVotes": 1634
  }
]
```

https://api.imdbgraph.org/ratings/tt0944947 would return:

```json5
{
  "show": {
    "imdbId": "tt0944947",
    "title": "Game of Thrones",
    "startYear": "2011",
    "endYear": "2019",
    "showRating": 9.2,
    "numVotes": 2377099
  },
  "allEpisodeRatings": {
    "1": {
      "1": {
        "episodeTitle": "Winter Is Coming",
        "season": 1,
        "episodeNumber": 1,
        "imdbRating": 8.9,
        "numVotes": 59293
      },
      "2": {
        "episodeTitle": "The Kingsroad",
        "season": 1,
        "episodeNumber": 2,
        "imdbRating": 8.6,
        "numVotes": 44512
      },
      "3": {
        "episodeTitle": "Lord Snow",
        "season": 1,
        "episodeNumber": 3,
        "imdbRating": 8.5,
        "numVotes": 42106
      }
      // ...More episodes
    },
    "2": {
      "1": {
        "episodeTitle": "The North Remembers",
        "season": 2,
        "episodeNumber": 1,
        "imdbRating": 8.6,
        "numVotes": 37018
      },
      "2": {
        "episodeTitle": "The Night Lands",
        "season": 2,
        "episodeNumber": 2,
        "imdbRating": 8.3,
        "numVotes": 35097
      },
      "3": {
        "episodeTitle": "What Is Dead May Never Die",
        "season": 2,
        "episodeNumber": 3,
        "imdbRating": 8.7,
        "numVotes": 34680
      }
      // ...More episodes
    }
    // ...More seasons
  }
}
```

### Copyright
Copyright Notice: Information courtesy of IMDb (http://www.imdb.com). Used with permission.
