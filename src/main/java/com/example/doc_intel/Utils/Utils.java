package com.example.doc_intel.Utils;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.OpenSearchMetaDataDTO;
import com.example.doc_intel.Exceptions.UnSupportedFileException;
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
        metadata.put(Constants.META_DATA_FILE_NAME, openSearchMetaDataDTO.getFileName());
        metadata.put(Constants.META_DATA_LINE_NUMBER, openSearchMetaDataDTO.getLineNumber());
        metadata.put(Constants.META_DATA_PAGE_NUMBER, openSearchMetaDataDTO.getPageNumber());
        metadata.put(Constants.META_DATA_TEXT, openSearchMetaDataDTO.getText());
        return metadata;
    }

    public static String getExtension(MultipartFile file) {
        if (file == null) {
            throw new UnSupportedFileException("No File Available");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new UnSupportedFileException("File format is not proper");
        }

        String extension = filename
                .substring(filename.lastIndexOf('.') + 1)
                .toLowerCase();
        if (!supportedTypes.contains(extension)) {
            return null;
        }
        return extension;
    }

    public static String getObjectKey(@NonNull String userName,
                                      @NonNull UUID documentUUID,
                                      @NonNull String fileName) {
        return userName + File.separator + documentUUID + File.separator + fileName;
    }
}
