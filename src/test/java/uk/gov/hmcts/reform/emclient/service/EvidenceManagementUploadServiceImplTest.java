package uk.gov.hmcts.reform.emclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementUploadServiceImplTest {

    @Mock private RestTemplate restTemplate;
    @Mock private AuthTokenGenerator authTokenGenerator;
    @Mock private UserService userService;

    @InjectMocks
    private EvidenceManagementUploadServiceImpl emUploadService;

    private ArgumentCaptor<HttpEntity> httpEntityReqEntity;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() throws IOException {
        ReflectionTestUtils.setField(emUploadService,"documentManagementStoreUploadUrl", "emuri");
        when(authTokenGenerator.generate()).thenReturn("xxxx");
        when(userService.getUserDetails(authKey())).thenReturn(UserDetails.builder().id("19").build());
        mockRestTemplate();
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectUploadToSucceed() {
        List<FileUploadResponse> responses = emUploadService.upload(getMultipartFiles(),
                authKey(), "ReqId");
        assertTrue(responses.size() > 0);
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmRequestWith3Headers() {
        emUploadService.upload(getMultipartFiles(), authKey(), "ReqId");
        List<HttpEntity> allValues = httpEntityReqEntity.getAllValues();
        assertEquals(3, allValues.get(0).getHeaders().size());
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmReqToHaveSecurityAuthHeader() {
        emUploadService.upload(getMultipartFiles(), authKey(), "ReqId");
        assertTrue(getEmRequestHeaders().containsKey("ServiceAuthorization"));
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmReqToHaveUserIdHeader() {
        emUploadService.upload(getMultipartFiles(), authKey(), "ReqId");
        assertTrue(getEmRequestHeaders().containsKey("user-id"));
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmReqToHaveValidContentTypeHeader() {
        emUploadService.upload(getMultipartFiles(), authKey(), "ReqId");
        assertEquals("multipart/form-data", getEmRequestHeaders().get("Content-Type").get(0));
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectAuthKeyIsParsedForUserId() {
        emUploadService.upload(getMultipartFiles(), authKey(), "ReqId");
        assertEquals("19", getEmRequestHeaders().get("user-id").get(0));
    }

    @Test
    public void givenNullFileParamIsPassed_whenUploadIsCalled_thenExpectError() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("files");
        emUploadService.upload(null, authKey(), "ReqId");
        httpEntityReqEntity.getAllValues();
    }

    private void mockRestTemplate() throws IOException {
        this.httpEntityReqEntity = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForObject(eq("emuri"), httpEntityReqEntity.capture(), any())).thenReturn(getResponse());
    }

    private HttpHeaders getEmRequestHeaders() {
        return httpEntityReqEntity.getAllValues().get(0).getHeaders();
    }

    private ObjectNode getResponse() throws IOException {
        final String response = new String(readAllBytes(get("src/test/resources/fileuploadresponse.txt")));
        return (ObjectNode) new ObjectMapper().readTree(response);
    }

    private static String authKey() {
        return "Bearer dummy-token";
    }

    private List<MultipartFile> getMultipartFiles() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "JDP.pdf",
                "application/pdf", "This is a test pdf file".getBytes());
        return Collections.singletonList(multipartFile);
    }
}
