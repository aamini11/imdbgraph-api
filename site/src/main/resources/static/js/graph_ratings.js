function parse_ratings(ratings_json) {
    // ratings_json.allSeasons = data.allSeasons.map(seasonInfo => {
    //     return {
    //         name: "Season " + seasonInfo.season,
    //         type: "spline",
    //         data: seasonInfo.episodes,
    //         seasonInfo: seasonInfo
    //     }
    // });

    Object.keys(ratings_json).map(season => {
        return {
            name: "Season " + season.season,
            type: "spline",
            data: season.episodes,
            seasonInfo: season
        };
    });

    let totalEpisodesInSeries = data.allRatings.reduce((acc, e) => {
        return acc + e.data.length
    }, 0);

    for (let season = 0, i = 1; i <= totalEpisodesInSeries; season++) {
        let seasonRatings = data.allRatings[season];
        let seasonSize = seasonRatings.data.length;

        for (let j = 0; j < seasonSize; j++) {
            let episodeInfo = seasonRatings.data[j];
            seasonRatings.data[j] = {
                x: i,
                y: parseFloat(episodeInfo.imdbRating),
                episode: episodeInfo.episode,
                season: season + 1,
                title: episodeInfo.title
            };
            i++;
        }
    }
}

function load_graph(html_id, ratings_json) {
    let highcharts_data = parse_ratings(ratings_json);

    Highcharts.chart(html_id, {
        title: {
            text: data.showInfo.title
        },

        plotOptions: {
            spline: {
                dataLabels: {
                    enabled: true
                }
            }
        },

        xAxis: {
            //tickInterval: 1
            visible: false
        },

        yAxis: {
            title: {
                text: 'IMDB Rating'
            },
            tickInterval: 1
        },

        tooltip: {
            shared: true,
            useHTML: true,
            headerFormat: '',
            pointFormat:
                '<tr><td style="color: {series.color}">s{point.season}e{point.episode} ({point.title}): </td>' +
                '<td style="text-align: right"><b>{point.y}</b></td></tr>',
            footerFormat: '',
            valueDecimals: 2
        },

        series: highcharts_data
    });
}

