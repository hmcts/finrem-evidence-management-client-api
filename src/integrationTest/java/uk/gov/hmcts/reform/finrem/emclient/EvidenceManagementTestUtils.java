package uk.gov.hmcts.reform.finrem.emclient;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EvidenceManagementTestUtils {

    static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    //this is a hack to make this work with the docker container
    private String getDocumentStoreUri(String uri, String documentManagementUrl) {
        if (uri.contains("http://em-api-gateway-web:3404")) {
            return uri.replace("http://em-api-gateway-web:3404", documentManagementUrl);
        }

        return uri;
    }

    @SuppressWarnings("unchecked")
    String uploadFileToEvidenceManagement(String filePath, String fileContentType,
                                          String evidenceManagementClientApiBaseUrl, String documentManagementUrl,
                                          IdamUtils idamTestSupportUtil, String caseTypeId) {
        File file = new File(filePath);
        Response response = SerenityRest.given()
            .headers(getAuthenticationTokenHeader(idamTestSupportUtil))
            .header("caseTypeId", caseTypeId)
            .multiPart("file", file, fileContentType)
            .post(evidenceManagementClientApiBaseUrl.concat("/upload"))
            .andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), response.statusCode());
        return getDocumentStoreUri(((List<String>) response.getBody().path("fileUrl")).get(0), documentManagementUrl);
    }

    void downloadFileToEvidenceManagement(String filePath, String evidenceManagementClientApiDownloadUrl, IdamUtils idamTestSupportUtil) {
        Response response = SerenityRest.given()
            .headers(getAuthenticationTokenHeader(idamTestSupportUtil))
            .queryParam("binaryFileUrl", filePath)
            .get(evidenceManagementClientApiDownloadUrl)
            .andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), response.statusCode());
    }

    private Map<String, Object> getAuthenticationTokenHeader(IdamUtils idamTestSupportUtil) {
        String authenticationToken = idamTestSupportUtil.getIdamTestUser();
        Map<String, Object> headers = new HashMap<>();
        headers.put(AUTHORIZATION_HEADER_NAME, authenticationToken);
        return headers;
    }
}
