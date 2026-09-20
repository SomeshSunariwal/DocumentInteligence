package com.example.doc_intel.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.io.InputStream;

@Builder
@AllArgsConstructor
@Data
public class EncoderModel {

    @NonNull
    final InputStream fileStream;

    @NonNull
    final String userId;

    @NonNull
    final Integer documentVersion;

    @NonNull
    final String documentId;

    @NonNull
    final String fileName;

    @NonNull
    final Integer line;

    @NonNull
    final Integer overlapLine;

}
