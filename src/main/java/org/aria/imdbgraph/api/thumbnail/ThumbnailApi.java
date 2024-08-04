package org.aria.imdbgraph.api.thumbnail;

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

    private final ThumbnailDb thumbnailDb;

    @Autowired
    ThumbnailApi(ThumbnailDb thumbnailDb) {
        this.thumbnailDb = thumbnailDb;
    }

    @GetMapping(path = "/thumbnail/{showId}.jpg", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<UrlResource> getRatings(@PathVariable(value = "showId") String showId) throws IOException {
        Optional<String> thumbnailUrl = thumbnailDb.getThumbnailUrl(showId);
        if (thumbnailUrl.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var url = URI.create(thumbnailUrl.get()).toURL();
        var resource = new UrlResource(url);
        return ResponseEntity.ok()
                .body(resource);
    }
}
