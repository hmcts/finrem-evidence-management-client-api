package uk.gov.hmcts.reform.emclient.service;

import org.springframework.http.ResponseEntity;

public interface EvidenceManagementDownloadService {

    ResponseEntity<byte[]> download(final String fileUrl);
}
