package uk.gov.hmcts.reform.finrem.emclient;

import net.serenitybdd.junit.runners.SerenityRunner;
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
    private static final String DELETE_ENDPOINT = "/deleteFile?fileUrls=";

    private final EvidenceManagementTestUtils evidenceManagementTestUtils = new EvidenceManagementTestUtils();

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementStoreUrl;

    private String fileUrl;

}
