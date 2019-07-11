package uk.gov.hmcts.reform.emclient.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
@Slf4j
public class EvidenceManagementDownloadServiceImpl implements EvidenceManagementDownloadService {


    private static final String FINANCIAL_REMEDY_COURT_ADMIN = "caseworker-divorce-financialremedy-courtadmin";

    private static final String USER_ROLES = "user-roles";

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public ResponseEntity<byte[]> download(@NonNull final String binaryFileUrl) {

        log.info("Binary url for file download : {} ", binaryFileUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.set(USER_ROLES, FINANCIAL_REMEDY_COURT_ADMIN);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(binaryFileUrl, HttpMethod.GET, httpEntity, byte[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to get bytes from document store for document {} ",
                binaryFileUrl);
            throw new RuntimeException(String.format("Unexpected code from DM store: %s ", response.getStatusCode()));
        }

        log.info("File download status : {} ", response.getStatusCode());
        return response;
    }


}
