package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.DTO.EncoderModel;
import dev.langchain4j.data.segment.TextSegment;
import lombok.NonNull;

import java.util.List;

public interface DocumentEncoder {
    List<TextSegment> encode(@NonNull final EncoderModel encoderModel);
}
