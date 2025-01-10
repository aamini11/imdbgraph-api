package org.aria.imdbgraph.api.thumbnail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ThumbnailApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetThumbnailWithInvalidId() throws Exception {
        mockMvc.perform(get("/thumbnail/123"))
                .andExpect(status().is4xxClientError());
    }
}
