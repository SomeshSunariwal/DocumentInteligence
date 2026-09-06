package com.example.doc_intel.Reader;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Primary
public class FileReader implements Reader{


    @Override
    public String getParseFileData(MultipartFile file) throws Exception {
        String[] lines = {
                "Java is a programming language used to build backend applications.",
                "Spring Boot is a Java framework used to create production-ready web applications.",
                "LangChain4j helps Java applications work with Large Language Models and vector stores."
        };

        // 2. Convert the 3 lines into one document
        return String.join("\n", lines);
    }
}
