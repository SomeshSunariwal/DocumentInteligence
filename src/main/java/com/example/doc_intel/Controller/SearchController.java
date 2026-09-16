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
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/users/search")
    public ResponseEntity<SearchResponseDTO> postSearch(@RequestParam(name = "documentId") @Nullable String documentId,
                                                        @Valid @RequestBody SearchRequestDTO searchRequestDTO) {
        SearchResponseDTO searchResponseDTO = searchService.processSearch(documentId, searchRequestDTO);
        return ResponseEntity.ok().body(searchResponseDTO);
    }

    @PostMapping("/users/generate")
    public ResponseEntity<AISearchResponseDTO> postAISearch(
        @RequestParam(name = "documentId") @Nullable String documentId,
        @Valid @RequestBody SearchRequestDTO searchRequestDTO) {
        AISearchResponseDTO searchResponseDTO = searchService.processAISearch(documentId, searchRequestDTO);
        return ResponseEntity.ok().body(searchResponseDTO);
    }
}
