package uk.gov.hmcts.reform.finrem.emclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.HttpStatus;
import org.junit.After;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(SerenityRunner.class)
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.emclient", "uk.gov.hmcts.auth.provider.service"})
@ImportAutoConfiguration({FeignRibbonClientAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
    FeignAutoConfiguration.class})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${env}.properties")
public class EvidenceManagementFileAuditIntegrationTest {

    private static final String FILE_PATH = "src/integrationTest/resources/FileTypes/PNGFile.png";
    private static final String IMAGE_FILE_CONTENT_TYPE = "image/png";

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

    @After
    public void cleanUp() {
        idamTestSupportUtil.deleteCreatedUser();
    }

    @Test
    public void givenUploadedFiles_whenFilesAudited_thenAuditResponseIsReturned() throws JsonProcessingException {
        fileUrl = uploadFile();

        Response response = SerenityRest.given()
            .header("Authorization", idamTestSupportUtil.getIdamTestUser())
            .get(evidenceManagementClientApiBaseUrl + String.format("/audit?fileUrls=%s&fileUrls=%s", fileUrl, fileUrl))
            .andReturn();
        assertThat(response.getStatusCode(), is(HttpStatus.SC_OK));

        List<FileUploadResponse> auditResponses = new ObjectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
        assertThat(auditResponses, hasSize(2));
        auditResponses.forEach(auditResponse -> assertThat(auditResponse.getFileName(), is(Paths.get(FILE_PATH).getFileName().toString())));
    }

    private String uploadFile() {
        return evidenceManagementTestUtils.uploadFileToEvidenceManagement(FILE_PATH, IMAGE_FILE_CONTENT_TYPE,
            evidenceManagementClientApiBaseUrl, documentManagementStoreUrl, idamTestSupportUtil);
    }
}
