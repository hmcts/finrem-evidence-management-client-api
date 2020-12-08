package uk.gov.hmcts.reform.emclient.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementAuditService {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String USER_ID_HEADER = "user-id";

    private final RestTemplate restTemplate;
    private final UserService userService;
    private final AuthTokenGenerator authTokenGenerator;


    public List<FileUploadResponse> audit(List<String> fileUrls, String authorizationToken) {
        log.info("Deleting evidence management document: fileUrl='{}', requestId='{}'", fileUrls);

        UserDetails userDetails = userService.getUserDetails(authorizationToken);
        HttpEntity httpEntity = new HttpEntity(headers(userDetails.getId()));

        List<FileUploadResponse> filesAuditDetails = new ArrayList<>();
        fileUrls.forEach(fileUrl -> {
            JsonNode document = restTemplate.exchange(
                fileUrl,
                HttpMethod.GET,
                httpEntity,
                JsonNode.class).getBody();

            System.out.println(document.toPrettyString());

            filesAuditDetails.add(createUploadResponse(document));
        });

        return filesAuditDetails;
    }

    private FileUploadResponse createUploadResponse(JsonNode document) {
        Stream.of("originalDocumentName", "createdBy", "createdOn", "lastModifiedBy", "modifiedOn")
            .map(document::get)
            .forEach(System.out::println);

        return FileUploadResponse.builder()
            .status(HttpStatus.OK)
            .fileUrl(new HalLinkDiscoverer()
                .findLinkWithRel("self", document.toString())
                .orElseThrow(() -> new IllegalStateException("self rel link not found"))
                .getHref())
            .fileName(document.get("originalDocumentName").asText())
            .createdBy(document.get("createdBy").asText())
            .createdOn(document.get("createdOn").asText())
            .lastModifiedBy(document.get("lastModifiedBy").asText())
            .modifiedOn(document.get("modifiedOn").asText())
            .mimeType(document.get("mimeType").asText())
            .build();
    }

    private HttpHeaders headers(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        headers.set(USER_ID_HEADER, userId);

        return headers;
    }
}
