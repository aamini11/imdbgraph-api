package org.aria.imdbgraph.api.thumbnail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@RestController
public class ThumbnailController {

    private final ThumbnailService thumbnailService;

    @Autowired
    ThumbnailController(ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

    @GetMapping(value = "/thumbnail/{showId}")
    public ResponseEntity<UrlResource> getRatings(@PathVariable(value = "showId") String showId) throws IOException {
        Optional<String> thumbnailUrl = thumbnailService.getThumbnailUrl(showId);
        if (thumbnailUrl.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var url = URI.create(thumbnailUrl.get()).toURL();
        var resource = new UrlResource(url);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(showId + ".jpg")
                                .build().toString())
                .body(resource);
    }
}
