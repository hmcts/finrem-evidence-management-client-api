package uk.gov.hmcts.reform.emclient.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementAuditServiceTest {

    @Mock private RestTemplate restTemplate;
    @Mock private UserService userService;
    @Mock private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private EvidenceManagementAuditService evidenceManagementAuditService;

    @Test
    public void whenAuditRequested_thenDocumentManagementResponseIsProcessed() {
        when(userService.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class))).thenReturn(jsonNode());

        List<FileUploadResponse> response = evidenceManagementAuditService.audit(singletonList("mockFileUrl"), "mockToken");

        assertThat(response, hasSize(1));
        assertThat(response.get(0).getFileName(), is("PNGFile.png"));
    }

    @SneakyThrows
    private ResponseEntity<JsonNode> jsonNode() {
        return ResponseEntity.ok().body(new ObjectMapper().readTree(new String(readAllBytes(get("src/test/resources/fileauditresponse.txt")))));
    }

    @Test
    public void whenAuditRequested_thenDocumentManagementResponseIsProcessedEvenLastupdatedByNotPresent() {
        when(userService.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class))).thenReturn(jsonNodeV2());

        List<FileUploadResponse> response = evidenceManagementAuditService.audit(singletonList("mockFileUrl"), "mockToken");

        assertThat(response, hasSize(1));
        assertThat(response.get(0).getFileName(), is("PNGFile.png"));
    }

    @SneakyThrows
    private ResponseEntity<JsonNode> jsonNodeV2() {
        return ResponseEntity.ok().body(new ObjectMapper().readTree(new String(readAllBytes(get("src/test/resources"
                + "/fileauditresponseV2.txt")))));
    }
}
