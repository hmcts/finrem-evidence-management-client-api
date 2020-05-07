package uk.gov.hmcts.reform.emclient.response;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
@Builder
public class FileUploadResponse {

    String fileUrl;
    String fileName;
    String mimeType;
    String createdBy;
    String lastModifiedBy;
    String createdOn;
    String modifiedOn;
    HttpStatus status;
}
