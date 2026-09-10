package com.example.doc_intel.Utils;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.OpenSearchMetaDataDTO;
import com.example.doc_intel.Exceptions.FileSupportError;
import dev.langchain4j.data.document.Metadata;
import org.jspecify.annotations.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Utils {

    private static final List<String> supportedTypes = Arrays.asList("pdf", "txt");

    public static Metadata convertToMetaData(OpenSearchMetaDataDTO openSearchMetaDataDTO) {
        Metadata metadata = new Metadata();
        metadata.put(Constants.FILE_NAME, openSearchMetaDataDTO.getFileName());
        metadata.put(Constants.LINE_NUMBER, openSearchMetaDataDTO.getLineNumber());
        metadata.put(Constants.PAGE_NUMBER, openSearchMetaDataDTO.getPageNumber());
        metadata.put(Constants.TEXT, openSearchMetaDataDTO.getText());
        return metadata;
    }

    public static @NonNull String getExtension(MultipartFile file) {
        if (file == null) {
            throw new FileSupportError("Unsupported File Format");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new FileSupportError("Unsupported File Format");
        }

        String extension = filename
                .substring(filename.lastIndexOf('.') + 1)
                .toLowerCase();
        if (!supportedTypes.contains(extension)) {
            throw new FileSupportError("Unsupported File Format");
        }
        return extension;
    }

    public static String getObjectKey(@NonNull String userName,
                               @NonNull UUID documentUUID,
                               @NonNull String fileName) {
        return userName + File.separator + documentUUID + File.separator + fileName;
    }
}
