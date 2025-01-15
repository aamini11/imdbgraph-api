package org.aria.imdbgraph.api.thumbnail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ThumbnailApi {

    private static final Logger logger = LogManager.getLogger(ThumbnailApi.class);

    private final ThumbnailDb thumbnailDb;

    @Autowired
    public ThumbnailApi(ThumbnailDb thumbnailDb) {
        this.thumbnailDb = thumbnailDb;
    }

    @GetMapping("/thumbnail/{showId}")
    public String getRatings(@PathVariable(value = "showId") String showId) {
        Optional<String> thumbnailUrl = thumbnailDb.getThumbnailUrl(showId);
        if (thumbnailUrl.isEmpty()) {
            logger.info("Could not find thumbnail for show: {}", showId);
            return null;
        }
        return thumbnailUrl.get();
    }
}
