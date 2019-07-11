package uk.gov.hmcts.reform.emclient.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDownloadServiceImplTest {

    private static final String EVIDENCE_MANAGEMENT_SERVICE_URL = "http://localhost:8080/documents/";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private EvidenceManagementDownloadServiceImpl downloadService = new EvidenceManagementDownloadServiceImpl();


    @Test
    public void shouldPassThruDocumentDownloadSuccessfullyState() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.OK);

        ResponseEntity<?> response = downloadService.download(fileUrl);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfFileDoesNotExsist() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("random");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.NOT_FOUND);

        ResponseEntity<?> response = downloadService.download(fileUrl);
        assertFalse("Failed to receive exception ", true);
    }


    @Test(expected = ResourceAccessException.class)
    public void shouldPassThruExceptionThrownWhenEvidenceManagementServiceNotFound() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("25");

        doThrow(ResourceAccessException.class)
            .when(restTemplate)
            .exchange(Mockito.eq(fileUrl),
                Mockito.eq(HttpMethod.GET),
                any(),
                any(Class.class));

        ResponseEntity<?> response = downloadService.download(fileUrl);
        assertFalse("Failed to receive exception resulting from non-running EM service", true);
    }

    private void setupMockEvidenceManagementService(String fileUrl,
                                                    HttpStatus httpStatus) {
        when(authTokenGenerator.generate()).thenReturn("xxxx");

        doReturn(new ResponseEntity<>(httpStatus))
            .when(restTemplate)
            .exchange(Mockito.eq(fileUrl),
                Mockito.eq(HttpMethod.GET),
                any(),
                any(Class.class));
    }
}