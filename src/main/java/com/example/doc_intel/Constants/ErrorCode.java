package com.example.doc_intel.Constants;

/**
 * Stable, unique numeric codes for individual application failure points.
 */
public final class ErrorCode {

    private ErrorCode() {
    }

    // Document operations and processing: 1000-1099
    public static final Integer DocumentUpdateTargetNotFound = 1000;

    public static final Integer DocumentUpdateVersionNotFound = 1001;

    public static final Integer KafkaDocumentVersionNotFound = 1002;

    public static final Integer KafkaDocumentProcessingFailed = 1003;

    public static final Integer DocumentParserFailed = 1004;

    public static final Integer KafkaEmbeddingCreationFailed = 1005;

    public static final Integer DocumentDeleteTargetNotFound = 1006;

    public static final Integer DocumentGetTargetNotFound = 1007;

    public static final Integer DocumentSummaryTargetNotFound = 1008;

    public static final Integer DocumentLatestVersionNotFound = 1009;

    public static final Integer ChatTargetNotFound = 1010;

    public static final Integer ChatLatestVersionNotFound = 1011;

    public static final Integer ChatRequestedVersionNotFound = 1012;

    public static final Integer AISearchTargetNotFound = 1013;

    public static final Integer AISearchLatestVersionNotFound = 1014;

    public static final Integer AISearchRequestedVersionNotFound = 1015;

    // User and authentication errors: 1100-1199
    public static final Integer DocumentProcessorUserNotFound = 1100;

    public static final Integer DocumentUploadUserNotFound = 1101;

    public static final Integer DocumentDeleteUserNotFound = 1102;

    public static final Integer DocumentListUserNotFound = 1103;

    public static final Integer DocumentGetUserNotFound = 1104;

    public static final Integer DocumentSummaryUserNotFound = 1105;

    public static final Integer ChatUserUnauthenticated = 1106;

    public static final Integer SearchUserNotFound = 1107;

    public static final Integer AISearchUserNotFound = 1108;

    public static final Integer ConfigUserNotFound = 1109;

    public static final Integer UserDeleteTargetNotFound = 1110;

    public static final Integer UserSoftDeleteTargetNotFound = 1111;

    public static final Integer UserEmailOwnershipMismatch = 1112;

    public static final Integer RequestUserUnauthenticated = 1113;

    public static final Integer UserGetUserNotFound = 1114;

    public static final Integer AIConfigNotFound = 1115;

    // File errors: 1200-1299
    public static final Integer DocumentUpdateUnsupportedFile = 1200;

    public static final Integer DocumentUploadUnsupportedFile = 1201;

    public static final Integer FileMissing = 1202;

    public static final Integer FileExtensionMalformed = 1203;

    public static final Integer TextFileReadFailed = 1204;

    public static final Integer PdfFileReadFailed = 1205;

    // AI errors: 1300-1399
    public static final Integer DocumentSummaryAIConfigMissing = 1300;

    public static final Integer ChatAIConfigMissing = 1301;

    public static final Integer AISearchAIConfigMissing = 1302;

    public static final Integer AISearchGenerationFailed = 1303;

    // Object storage errors: 1400-1499
    public static final Integer MinioBucketInitializationFailed = 1400;

    public static final Integer MinioObjectUploadFailed = 1401;

    public static final Integer MinioObjectReadFailed = 1402;

    public static final Integer MinioPresignedUrlFailed = 1403;

    // OpenSearch errors: 1500-1599
    public static final Integer UnsupportedMetadataFilter = 1500;

    public static final Integer OpenSearchIndexInitializationFailed = 1501;

    public static final Integer OpenSearchVectorSearchFailed = 1502;

    public static final Integer OpenSearchVectorSearchUnexpectedError = 1503;

    public static final Integer OpenSearchEmbeddingIndexFailed = 1504;

    public static final Integer OpenSearchFilterSearchFailed = 1505;

    public static final Integer OpenSearchFilterSearchUnexpectedError = 1506;

    public static final Integer OpenSearchFilterConversionFailed = 1507;

    // Database errors: 1600-1699
    public static final Integer UserDatabaseOperationFailed = 1600;

    // Request and unexpected errors: 1700-1799
    public static final Integer InvalidRequest = 1700;

    public static final Integer BadCredentials = 1701;

    public static final Integer InvalidJwtToken = 1702;

    public static final Integer AuthenticationRequired = 1703;

    public static final Integer AccessDenied = 1704;

    public static final Integer LoginUserNotFound = 1705;

    public static final Integer UnexpectedError = 1799;
}
