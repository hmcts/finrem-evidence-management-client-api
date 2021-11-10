package uk.gov.hmcts.reform.emclient.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementAuditService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementUploadService;
import uk.gov.hmcts.reform.emclient.validation.constraint.EvidenceFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementSecureDocStoreService;

import java.util.List;

@RestController
@RequestMapping(path = "/emclientapi")
@Validated
@RequiredArgsConstructor
public class EvidenceManagementClientController {

    private final EvidenceManagementDeleteService emDeleteService;
    private final EvidenceManagementUploadService emUploadService;
    private final EvidenceManagementDownloadService emReadService;
    private final EvidenceManagementAuditService emAuditService;

    @Autowired
    private EvidenceManagementSecureDocStoreService evidenceManagementSecureDocStoreService;

    @Autowired
    private UserService userService;

    @Value("${feature.secure-doc-store:false}")
    protected boolean secureDocStoreEnabled;

    @ApiOperation(value = "Handles file upload to Evidence Management Document Store")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Files uploaded successfully",
                    response = List.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @PostMapping(value = "/version/1/upload", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public List<FileUploadResponse> upload(
            @RequestHeader(value = "Authorization", required = false) String authorizationToken,
            @RequestHeader(value = "requestId", required = false) String requestId,
            @RequestParam("file") List<@EvidenceFile MultipartFile> files) {

            if (secureDocStoreEnabled) {
                return evidenceManagementSecureDocStoreService.upload(files, userService.getIdamTokens(authorizationToken));
            } else {
                return emUploadService.upload(files, authorizationToken, requestId);
            }
    }

    @ApiOperation(value = "Downloads file from Evidence Management Document Store.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Files downloaded successfully",
            response = List.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping(value = "/version/1/download", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> download(
        @RequestParam("binaryFileUrl") String binaryFileUrl) {
        return emReadService.download(binaryFileUrl);
    }

    @ApiOperation(value = "Handles file deletion from Evidence Management Document Store.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Files deleted successfully",
                    response = List.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @DeleteMapping(value = "/version/1/deleteFile", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> deleteFile(@RequestHeader(value = "Authorization") String authorizationToken,
                @RequestHeader(value = "requestId", required = false) String requestId,
                @RequestParam("fileUrl") String fileUrl) {
        if (secureDocStoreEnabled) {
            evidenceManagementSecureDocStoreService.delete(fileUrl, userService.getIdamTokens(authorizationToken));
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return emDeleteService.deleteFile(fileUrl, authorizationToken, requestId);
        }
    }

    @ApiOperation(value = "Handles file auditing with Document Management Store")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Files audited successfully",
            response = List.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping(value = "/version/1/audit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<FileUploadResponse> audit(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestParam List<String> fileUrls) {

        return emAuditService.audit(fileUrls, authorizationToken);
    }
}
