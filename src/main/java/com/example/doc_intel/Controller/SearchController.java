package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.ChatModel.SearchResponseDTO;
import com.example.doc_intel.DTO.ChatModel.AISearchResponseDTO;
import com.example.doc_intel.Service.SearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;


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
     * @return SearchResponseDTO   Response object containing search results.
     */
    @GetMapping("/users/search")
    public ResponseEntity<SearchResponseDTO> getSearch(@RequestParam(name = "query") @NotBlank String query) {
        SearchResponseDTO searchResponseDTO = searchService.processSearch(query);
        return ResponseEntity.ok().body(searchResponseDTO);
    }

    /**
     * API endpoint for performing AI search on the document.
     *
     * @param documentId ID of the document to perform search on.
     * @return
     */
    @GetMapping("/users/chat")
    public ResponseEntity<AISearchResponseDTO> postAISearch(
        @RequestParam(name = "documentId") @Nullable String documentId,
        @RequestParam(name = "query") @NotBlank String query) {
        AISearchResponseDTO searchResponseDTO = searchService.processAISearch(documentId, query);
        return ResponseEntity.ok().body(searchResponseDTO);
    }
}
