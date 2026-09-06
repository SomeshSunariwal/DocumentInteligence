package com.example.doc_intel.Reader;

import org.springframework.web.multipart.MultipartFile;

public interface Reader {

    String getParseFileData(MultipartFile file) throws Exception;
}
