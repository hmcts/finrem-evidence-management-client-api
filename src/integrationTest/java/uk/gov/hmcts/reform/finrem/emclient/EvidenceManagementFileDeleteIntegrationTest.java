package uk.gov.hmcts.reform.finrem.emclient;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.emclient.EvidenceManagementTestUtils.AUTHORIZATION_HEADER_NAME;

@Lazy
@RunWith(SerenityRunner.class)
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.emclient", "uk.gov.hmcts.auth.provider.service"})
@ImportAutoConfiguration({FeignRibbonClientAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class,
        FeignAutoConfiguration.class})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${env}.properties")
public class EvidenceManagementFileDeleteIntegrationTest {

    private static final String FILE_PATH = "src/integrationTest/resources/FileTypes/PNGFile.png";
    private static final String IMAGE_FILE_CONTENT_TYPE = "image/png";
    static final String DELETE_ENDPOINT = "/deleteFile?fileUrl=";

    private final EvidenceManagementTestUtils evidenceManagementTestUtils = new EvidenceManagementTestUtils();

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementStoreUrl;

    private String fileUrl;

    @Before
    public void setup() {
        fileUrl = null;
    }

    @After
    public void cleanUp() {
        if (fileUrl != null) {
            deleteFileFromEvidenceManagement(fileUrl, evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil));
        }
        idamTestSupportUtil.deleteCreatedUser();
    }

    @Test
    public void verifyDeleteRequestForExistingDocumentIsSuccessful() {
        fileUrl = uploadFile();
        Response response = deleteFileFromEvidenceManagement(fileUrl,
            evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil));

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        fileUrl = null;
    }

    @Test
    public void verifyDeleteRequestForNonExistentDocumentIs404NotFound() {
        fileUrl = uploadFile();
        String fileUrlAlt = fileUrl.concat("xyzzy");
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt,
            evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil));

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void verifyDeleteRequestWithMissingDocumentIdIsNotAllowed() {
        fileUrl = uploadFile();
        String fileUrlAlt = fileUrl.substring(0, fileUrl.lastIndexOf("/") + 1);
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt,
            evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), response.getStatusCode());
    }

    @Test
    public void verifyDeleteRequestWithInvalidAuthorizationHeaderIsUnauthorized() {
        fileUrl = uploadFile();
        Map<String, Object> headers = evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil);
        String invalidToken = "x".concat(headers.get(AUTHORIZATION_HEADER_NAME).toString()).concat("x");
        headers.put(AUTHORIZATION_HEADER_NAME, invalidToken);
        Response response = deleteFileFromEvidenceManagement(fileUrl, headers);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());
    }

    @Test
    public void verifyDeleteRequestWithUnauthorisedAuthTokenIsUnauthorized() {
        fileUrl = uploadFile();
        Map<String, Object> headers = evidenceManagementTestUtils.getInvalidAuthenticationTokenHeader();
        Response response = deleteFileFromEvidenceManagement(fileUrl, headers);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());
    }

    private Response deleteFileFromEvidenceManagement(String fileUrl, Map<String, Object> headers) {
        return evidenceManagementTestUtils.deleteFileFromEvidenceManagement(evidenceManagementClientApiBaseUrl.concat(DELETE_ENDPOINT),
            fileUrl, headers);
    }

    private String uploadFile() {
        return evidenceManagementTestUtils.uploadFileToEvidenceManagement(FILE_PATH, IMAGE_FILE_CONTENT_TYPE,
            evidenceManagementClientApiBaseUrl, documentManagementStoreUrl, idamTestSupportUtil);
    }
}
