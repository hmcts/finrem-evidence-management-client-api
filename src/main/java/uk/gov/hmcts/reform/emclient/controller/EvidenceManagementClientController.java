package uk.gov.hmcts.reform.emclient.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementUploadService;
import uk.gov.hmcts.reform.emclient.validation.constraint.EvidenceFile;

import java.util.List;

@RestController
@RequestMapping(path = "/emclientapi")
@Validated
public class EvidenceManagementClientController {

    @Autowired
    private EvidenceManagementDeleteService emDeleteService;

    @Autowired
    private EvidenceManagementUploadService emUploadService;

    @Autowired
    private EvidenceManagementDownloadService emReadService;

    @ApiOperation(value = "Handles file upload to evidence management document store.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully uploaded the files",
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

        return emUploadService.upload(files, authorizationToken, requestId);
    }


    @ApiOperation(value = "Downloads file from evidence management document store.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The files are downloaded Successfully",
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

    @ApiOperation(value = "Handles file deletion  from evidence management document store.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted the files",
                    response = List.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @DeleteMapping(value = "/version/1/deleteFile", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> deleteFile(@RequestHeader(value = "Authorization") String authorizationToken,
                                        @RequestHeader(value = "requestId", required = false) String requestId,
                                        @RequestParam("fileUrl") String fileUrl) {
        return emDeleteService.deleteFile(fileUrl, authorizationToken, requestId);
    }
}