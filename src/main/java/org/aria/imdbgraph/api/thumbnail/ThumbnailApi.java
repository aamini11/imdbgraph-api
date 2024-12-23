package org.aria.imdbgraph.api.thumbnail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@RestController
public class ThumbnailApi {

    private static final Logger logger = LogManager.getLogger(ThumbnailApi.class);

    private final ThumbnailDb thumbnailDb;

    @Autowired
    public ThumbnailApi(ThumbnailDb thumbnailDb) {
        this.thumbnailDb = thumbnailDb;
    }

    @GetMapping(path = "/thumbnail/{showId}.jpg", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<UrlResource> getRatings(@PathVariable(value = "showId") String showId) throws IOException {
        Optional<String> thumbnailUrl = thumbnailDb.getThumbnailUrl(showId);
        if (thumbnailUrl.isEmpty()) {
            logger.info("Could not find thumbnail for show: {}", showId);
            return ResponseEntity.noContent().build();
        }

        var uri = URI.create(thumbnailUrl.get());
        var resource = new UrlResource(uri);
        return ResponseEntity.ok()
                .body(resource);
    }
}
