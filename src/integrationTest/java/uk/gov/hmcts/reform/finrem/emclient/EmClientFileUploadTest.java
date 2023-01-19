package uk.gov.hmcts.reform.finrem.emclient;

import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.junit.annotations.TestData;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.junit.Assert.assertEquals;

@Lazy
@RunWith(SerenityParameterizedRunner.class)
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.emclient", "uk.gov.hmcts.auth.provider.service"})
@ImportAutoConfiguration({RibbonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class,
        FeignRibbonClientAutoConfiguration.class, FeignAutoConfiguration.class})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${env}.properties")
public class EmClientFileUploadTest {

    private static final String[] fileName = {"PNGFile.png", "BMPFile.bmp", "PDFFile.pdf", "TIFFile.TIF", "JPEGFile.jpg",
        "PNGFile.png", "BMPFile.bmp", "PDFFile.pdf", "TIFFile.TIF", "JPEGFile.jpg"};
    private static final String[] fileContentType = {"image/png", "image/bmp", "application/pdf", "image/tiff", "image/jpeg",
        "image/png", "image/bmp", "application/pdf", "image/tiff", "image/jpeg"};

    private final String name;
    private final String fileType;
    private final EvidenceManagementTestUtils evidenceManagementTestUtils = new EvidenceManagementTestUtils();

    @Autowired private IdamUtils idamTestSupportUtil;
    @Autowired private AuthTokenGenerator authTokenGenerator;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementStoreUrl;

    public EmClientFileUploadTest(String filename, String fileContentType) {
        this.name = filename;
        this.fileType = fileContentType;
    }

    @After
    public void cleanUp() {
        idamTestSupportUtil.deleteCreatedUser();
    }

    @TestData
    public static Collection<Object[]> testData() {
        return IntStream.range(0, fileName.length)
                .mapToObj(i -> new String[]{fileName[i], fileContentType[i]})
                .collect(Collectors.toList());
    }

    @Test
    public void uploadFile() {
        RestAssured.useRelaxedHTTPSValidation();
        uploadFileToEmStore(this.name, this.fileType);
    }

    @SuppressWarnings("unchecked")
    private void uploadFileToEmStore(String fileToUpload, String fileContentType) {
        File file = new File("src/integrationTest/resources/FileTypes/" + fileToUpload);
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(), new ErrorLoggingFilter());
        Response response = SerenityRest.given()
            .headers(getHeaders())
            .multiPart("file", file, fileContentType)
            .post(evidenceManagementClientApiBaseUrl.concat("/upload"))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        String fileUrl = ((List<String>) response.getBody().path("fileUrl")).get(0);

        assertEmGetFileResponse(fileToUpload, fileContentType, fileRetrieveUrl(fileUrl));

        evidenceManagementTestUtils.deleteFileFromEvidenceManagement(
            evidenceManagementClientApiBaseUrl + EvidenceManagementFileDeleteIntegrationTest.DELETE_ENDPOINT,
            fileUrl,
            evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil));
    }

    private String fileRetrieveUrl(String url) {
        return documentManagementStoreUrl + "/documents/" + url.substring(url.lastIndexOf('/') + 1);
    }

    private void assertEmGetFileResponse(String fileToUpload, String fileContentType, String fileUrl) {
        Response responseFromEvidenceManagement = readDataFromEvidenceManagement(fileUrl);
        assertEquals(HttpStatus.OK.value(), responseFromEvidenceManagement.getStatusCode());
        assertEquals(fileToUpload, responseFromEvidenceManagement.getBody().path("originalDocumentName"));
        assertEquals(fileContentType, responseFromEvidenceManagement.getBody().path("mimeType"));
    }

    public Response readDataFromEvidenceManagement(String uri) {
        String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
        String password = UUID.randomUUID().toString().toUpperCase();
        idamTestSupportUtil.createCaseworkerUserInIdam(username, password);

        Map<String, Object> headers = new HashMap<>();
        headers.put("ServiceAuthorization", authTokenGenerator.generate());
        headers.put("user-id", username);
        headers.put("user-roles", "caseworker-divorce");

        Response response = given()
                .contentType("application/json")
                .headers(headers)
                .when()
                .get(uri)
                .andReturn();

        idamTestSupportUtil.deleteTestUser(username);

        return response;
    }

    private Map<String, Object> getHeaders() {
        String authenticationToken = idamTestSupportUtil.getIdamTestUser();

        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", authenticationToken);
        headers.put("caseTypeId", "FinancialRemedyContested");
        headers.put("Content-Type", "multipart/form-data");

        return headers;
    }
}
