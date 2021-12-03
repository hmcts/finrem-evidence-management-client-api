package uk.gov.hmcts.reform.finrem.emclient;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@Lazy
@RunWith(SerenityRunner.class)
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.emclient", "uk.gov.hmcts.auth.provider.service"})
@ImportAutoConfiguration({FeignRibbonClientAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
        FeignAutoConfiguration.class})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${env}.properties")
@TestPropertySource(properties = {"feign.httpclient.enabled=false"})
public class EvidenceManagementFileDownloadIntegrationTest {

    private static final String FILE_PATH = "src/integrationTest/resources/FileTypes/PNGFile.png";
    private static final String IMAGE_FILE_CONTENT_TYPE = "image/png";
    private static final String CASE_TYPE = "FinancialRemedyContested";

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementStoreUrl;

    @Value("${evidence.management.client.api.endpoint.download}")
    private String evidenceManagementClientApiDownloadUrl;

    private EvidenceManagementTestUtils evidenceManagementTestUtils = new EvidenceManagementTestUtils();

    private String fileUrl;

    @After
    public void cleanUp() {
        if (fileUrl != null) {
            evidenceManagementTestUtils.deleteFileFromEvidenceManagement(
                evidenceManagementClientApiBaseUrl + EvidenceManagementFileDeleteIntegrationTest.DELETE_ENDPOINT,
                fileUrl,
                evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil));
            fileUrl = null;
        }
        idamTestSupportUtil.deleteCreatedUser();
    }

    @Test
    public void verifyEvidenceManagementFileDownload() {
        fileUrl = uploadFile();
        evidenceManagementTestUtils.downloadFileToEvidenceManagement(
            fileUrl + "/binary",
            evidenceManagementClientApiDownloadUrl,
            idamTestSupportUtil);
    }

    private String uploadFile() {
        return evidenceManagementTestUtils.uploadFileToEvidenceManagement(
            FILE_PATH,
            IMAGE_FILE_CONTENT_TYPE,
            evidenceManagementClientApiBaseUrl,
            documentManagementStoreUrl,
            idamTestSupportUtil,
            CASE_TYPE);
    }
}
