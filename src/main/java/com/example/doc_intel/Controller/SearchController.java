package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.ChatModel.*;
import com.example.doc_intel.Service.SearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class SearchController {

    private SearchService searchService;

    @PostMapping("/users/{email}/search")
    public ResponseEntity<SearchResponseDTO> postSearch(@PathVariable @Email String email,
                                                        @Valid @RequestBody SearchRequestDTO searchRequestDTO) {
        SearchResponseDTO searchResponseDTO = searchService.processSearch(email, searchRequestDTO);
        return ResponseEntity.ok().body(searchResponseDTO);
    }

    @PostMapping("/users/{email}/generate")
    public ResponseEntity<AISearchResponseDTO> postAISearch(@PathVariable @Email String email,
                                                            @Valid @RequestBody SearchRequestDTO searchRequestDTO) {
        AISearchResponseDTO searchResponseDTO = searchService.processAISearch(email, searchRequestDTO);
        return ResponseEntity.ok().body(searchResponseDTO);
    }
}
