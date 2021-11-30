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

    protected static final String JURISDICTION_ID = "DIVORCE";
    private final CaseDocumentClient caseDocumentClient;
    private static final int DOC_UUID_LENGTH = 36;

    @Autowired
    public EvidenceManagementSecureDocStoreService(CaseDocumentClient caseDocumentClient) {
        this.caseDocumentClient = caseDocumentClient;
    }

    public List<FileUploadResponse> upload(List<MultipartFile> files, IdamTokens idamTokens, String caseTypeId)
        throws HttpClientErrorException {
        log.info("EvidenceManagementSecureDocStoreService upload file: {} with case id: {}",
            files.toString(), caseTypeId);

        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(), caseTypeId, JURISDICTION_ID, files);

        if (uploadResponse == null) {
            return null; // TODO: refactor to return empty list instead
        }

        log.info("For userId {} : Files uploaded response from Case Doc AM is {}", idamTokens.getEmail(),
            uploadResponse.getDocuments().stream().map(e -> e.links.binary.href).collect(Collectors.toList()));

        return toUploadResponse(uploadResponse);
    }

    public byte[] download(String selfHref, IdamTokens idamTokens) throws HttpClientErrorException {
        ResponseEntity<Resource> responseEntity = downloadResource(selfHref, idamTokens);
        ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();

        return (resource != null) ? resource.getByteArray() : new byte[0];
    }

    public void delete(String selfHref, IdamTokens idamTokens) throws HttpClientErrorException {
        log.info("Request for userId {} and deleteDocUrl {} and docId {}",
            idamTokens.getEmail(), selfHref, getDocumentIdFromSelfHref(selfHref));

        caseDocumentClient.deleteDocument(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(),
            getDocumentIdFromSelfHref(selfHref), Boolean.TRUE);
    }

    protected UUID getDocumentIdFromSelfHref(String selfHref) {
        return UUID.fromString(selfHref.substring(selfHref.length() - DOC_UUID_LENGTH));
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

    private ResponseEntity<Resource> downloadResource(String selfHref, IdamTokens idamTokens) {
        String documentHref = URI.create(selfHref).getPath().replaceFirst("/", "");
        log.info("Request for userId {} and downloadUrl {}", idamTokens.getEmail(), documentHref);

        return caseDocumentClient.getDocumentBinary(idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(), documentHref);
    }

    private String getLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        LocalDateTime ldt = instant.atOffset(ZoneOffset.UTC).toLocalDateTime();

        return ldt.toString();
    }
}
