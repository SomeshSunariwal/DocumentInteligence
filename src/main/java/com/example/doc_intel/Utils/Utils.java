package com.example.doc_intel.Utils;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.OpenSearchMetaDataDTO;
import com.example.doc_intel.Enums.FileExtensions;
import com.example.doc_intel.Exceptions.UnAuthenticatedUser;
import com.example.doc_intel.Exceptions.UnSupportedFileException;
import dev.langchain4j.data.document.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Utils {

    private static final List<FileExtensions> supportedTypes = Arrays.asList(FileExtensions.PDF, FileExtensions.TXT);

    public static Metadata convertToMetaData(OpenSearchMetaDataDTO openSearchMetaDataDTO) {
        Metadata metadata = new Metadata();
        metadata.put(Constants.META_DATA_FILE_NAME, openSearchMetaDataDTO.getFileName());
        metadata.put(Constants.META_DATA_LINE_NUMBER, openSearchMetaDataDTO.getLineNumber());
        metadata.put(Constants.META_DATA_PAGE_NUMBER, openSearchMetaDataDTO.getPageNumber());
        metadata.put(Constants.META_USER_ID, openSearchMetaDataDTO.getUserId());
        metadata.put(Constants.META_DOCUMENT_VERSION, openSearchMetaDataDTO.getVersion());
        return metadata;
    }

    public static FileExtensions getExtension(MultipartFile file) {
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
        if (!supportedTypes.contains(FileExtensions.valueOf(extension.toUpperCase()))) {
            return null;
        }
        return FileExtensions.valueOf(extension.toUpperCase());
    }

    public static String getObjectKey(@NonNull String userName,
                                      @NonNull UUID documentUUID,
                                      @NonNull String fileName) {
        return userName + File.separator + documentUUID + File.separator + fileName;
    }

    public static String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getSubject();
        }
        throw new UnAuthenticatedUser("User is not authenticated");
    }
}
