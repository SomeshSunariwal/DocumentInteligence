package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.ChatModel.SearchRequestDTO;
import com.example.doc_intel.DTO.ChatModel.SearchResponseDTO;
import com.example.doc_intel.DTO.ChatModel.AISearchResponseDTO;
import com.example.doc_intel.Service.SearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;


import javax.annotation.Nullable;


/**
 * Controller class for handling search-related API endpoints.
 */
@Tag(
    name = "Search Controller",
    description = "These APIs used to perform search in documents"
)
@AllArgsConstructor
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private SearchService searchService;

    /**
     * API endpoint for performing search on the document.
     *
     * @param documentId       ID of the document to perform search on.
     * @param searchRequestDTO Request object containing search query
     * @return SearchResponseDTO   Response object containing search results.
     */
    @PostMapping("/users/search")
    public ResponseEntity<SearchResponseDTO> postSearch(@RequestParam(name = "documentId") @Nullable String documentId,
                                                        @Valid @RequestBody SearchRequestDTO searchRequestDTO) {
        SearchResponseDTO searchResponseDTO = searchService.processSearch(documentId, searchRequestDTO);
        return ResponseEntity.ok().body(searchResponseDTO);
    }

    /**
     * API endpoint for performing AI search on the document.
     *
     * @param documentId       ID of the document to perform search on.
     * @param searchRequestDTO Request object containing search query
     * @return
     */
    @PostMapping("/users/generate")
    public ResponseEntity<AISearchResponseDTO> postAISearch(
        @RequestParam(name = "documentId") @Nullable String documentId,
        @Valid @RequestBody SearchRequestDTO searchRequestDTO) {
        AISearchResponseDTO searchResponseDTO = searchService.processAISearch(documentId, searchRequestDTO);
        return ResponseEntity.ok().body(searchResponseDTO);
    }
}
