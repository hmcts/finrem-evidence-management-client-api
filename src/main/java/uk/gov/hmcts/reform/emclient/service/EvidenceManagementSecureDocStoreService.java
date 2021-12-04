package uk.gov.hmcts.reform.emclient.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.emclient.idam.models.IdamTokens;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@Service
@Slf4j
public class EvidenceManagementSecureDocStoreService {

    private final CaseDocumentClient caseDocumentClient;
    private static final int DOC_UUID_LENGTH = 36;
    protected static final String JURISDICTION_ID = "DIVORCE";

    @Autowired
    public EvidenceManagementSecureDocStoreService(CaseDocumentClient caseDocumentClient) {
        this.caseDocumentClient = caseDocumentClient;
    }

    public List<FileUploadResponse> upload(List<MultipartFile> files, IdamTokens idamTokens, String caseTypeId)
        throws HttpClientErrorException {
        log.info("EMSDocStore Upload files: {} with user: {} and case id: {}",
            files.toString(), idamTokens.getEmail(), caseTypeId);

        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(), caseTypeId, JURISDICTION_ID, files);

        if (uploadResponse == null) {
            log.info("EMSDocStore Failed to upload files");
            return null; // TODO: refactor to return empty list instead
        }

        log.info("EMSDocStore Uploaded files are: {} with user: {} and case id: {}",
            uploadResponse.getDocuments().stream().map(e -> e.links.binary.href).collect(Collectors.toList()),
            idamTokens.getEmail(), caseTypeId);

        return toUploadResponse(uploadResponse);
    }

    public byte[] download(String binaryFileUrl, IdamTokens idamTokens) throws HttpClientErrorException {
        ResponseEntity<Resource> responseEntity = downloadResource(binaryFileUrl, idamTokens);
        ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();

        return (resource != null) ? resource.getByteArray() : new byte[0];
    }

    public void delete(String fileUrl, IdamTokens idamTokens) throws HttpClientErrorException {
        log.info("EMSDocStore Delete file: {} with user: {} and docId: {}",
            fileUrl, idamTokens.getEmail(), getDocumentIdFromFileUrl(fileUrl));

        caseDocumentClient.deleteDocument(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(),
            getDocumentIdFromFileUrl(fileUrl), Boolean.TRUE);
    }

    protected UUID getDocumentIdFromFileUrl(String fileUrl) {
        return UUID.fromString(fileUrl.substring(fileUrl.length() - DOC_UUID_LENGTH));
    }

    private List<FileUploadResponse> toUploadResponse(UploadResponse uploadResponse) {
        Stream<Document> documentStream = stream(uploadResponse.getDocuments().spliterator(), false);

        return documentStream.map(this::createUploadResponse).collect(Collectors.toList());
    }

    private FileUploadResponse createUploadResponse(Document document) {
        return FileUploadResponse.builder()
            .status(HttpStatus.OK)
            .fileUrl(document.links.self.href)
            .fileName(document.originalDocumentName)
            .mimeType(document.mimeType)
            .createdBy(document.createdBy)
            .createdOn(getLocalDateTime(document.createdOn))
            .lastModifiedBy(document.lastModifiedBy)
            .modifiedOn(getLocalDateTime(document.modifiedOn))
            .build();
    }

    private ResponseEntity<Resource> downloadResource(String binaryFileUrl, IdamTokens idamTokens) {
        String documentHref = URI.create(binaryFileUrl).getPath().replaceFirst("/", "");
        log.info("EMSDocStore Download file: {} with user: {}", documentHref, idamTokens.getEmail());

        return caseDocumentClient.getDocumentBinary(idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(), documentHref);
    }

    private String getLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        LocalDateTime ldt = instant.atOffset(ZoneOffset.UTC).toLocalDateTime();

        return ldt.toString();
    }
}
