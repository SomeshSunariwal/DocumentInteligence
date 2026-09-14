package com.example.doc_intel.DTO.ChatModel;

import com.example.doc_intel.DTO.TextSegmentResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseDTO {

    List<TextSegmentResponseDTO> textSegmentResponseDTOList;

}
