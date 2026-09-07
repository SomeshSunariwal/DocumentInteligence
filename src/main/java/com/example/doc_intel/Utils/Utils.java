package com.example.doc_intel.Utils;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.OpenSearchMetaDataDTO;
import dev.langchain4j.data.document.Metadata;

public class Utils {
    public static Metadata convertToMetaData(OpenSearchMetaDataDTO openSearchMetaDataDTO) {
        Metadata metadata = new Metadata();
        metadata.put(Constants.FILE_NAME, openSearchMetaDataDTO.getFileName());
        metadata.put(Constants.LINE_NUMBER, openSearchMetaDataDTO.getLineNumber());
        metadata.put(Constants.PAGE_NUMBER, openSearchMetaDataDTO.getPageNumber());
        metadata.put(Constants.TEXT, openSearchMetaDataDTO.getText());
        return metadata;
    }
}
