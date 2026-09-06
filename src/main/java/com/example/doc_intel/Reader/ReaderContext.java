package com.example.doc_intel.Reader;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ReaderContext {

    private final Reader reader;

    public ReaderContext (Reader reader) {
        this.reader= reader;
    }

    public String getDataFromReader(MultipartFile file) throws Exception {
        return reader.getParseFileData(file);
    }
}
