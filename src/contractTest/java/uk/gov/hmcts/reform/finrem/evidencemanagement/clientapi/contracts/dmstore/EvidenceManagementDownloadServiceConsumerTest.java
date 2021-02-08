package uk.gov.hmcts.reform.finrem.evidencemanagement.clientapi.contracts.dmstore;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.emclient.BaseTest;
import uk.gov.hmcts.reform.emclient.idam.api.IdamApiClient;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDownloadService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

public class EvidenceManagementDownloadServiceConsumerTest extends BaseTest {
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private final String someServiceAuthToken = "someServiceAuthToken";
    private static final String DOCUMENT_ID = "5c3c3906-2b51-468e-8cbb-a4002eded075";
    private static final String DOWNLOAD_FILE_URL = "/" + DOCUMENT_ID + "/binary";
    private static final String USER_ROLES = "user-roles";
    private static final String FINANCIAL_REMEDY_COURT_ADMIN = "caseworker-divorce-financialremedy-courtadmin";

    @MockBean
    private UserService userService;

    @MockBean
    private IdamApiClient idamApiClient;

    @Mock
    private UserDetails mockUserDetails;

    @Autowired
    private EvidenceManagementDownloadService evidenceManagementDownloadService;

    @Autowired
    RestTemplate restTemplate;

    @Value("${document.management.store.download.url}")
    private String documentManagementStoreDownloadUrl;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("em_dm_store", "localhost", 8889, this);

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Pact(provider = "em_dm_store", consumer = "fr_evidenceManagementClient")
    public RequestResponsePact generatePactFragment(final PactDslWithProvider builder) throws JSONException {
        // @formatter:off
        return builder
          .given("A document exists in dm-store")
          .uponReceiving("A request to download the aforesaid document from dm-store")
          .method("GET")
          .headers(SERVICE_AUTHORIZATION_HEADER, someServiceAuthToken, USER_ROLES,FINANCIAL_REMEDY_COURT_ADMIN)
          .path(DOWNLOAD_FILE_URL)
          .willRespondWith()
          .status(200)
          .toPact();
    }


    @Test
    @PactVerification()
    public void verifyDocumentDownloadFromDmStore() throws Exception {

        final UserDetails userDetails = getUserDetails();

        given(userService.getUserDetails(anyString())).willReturn(userDetails);
        given(authTokenGenerator.generate()).willReturn(someServiceAuthToken);

        ResponseEntity<byte[]> responses = evidenceManagementDownloadService.download(documentManagementStoreDownloadUrl);
        assertTrue(responses.getStatusCode().is2xxSuccessful());
    }

    private UserDetails getUserDetails() {
        UserDetails userDetails = UserDetails.builder().id("id1")
            .email("joe@bloggs.com")
            .forename("Joe")
            .surname("Bloggs")
            .build();
        return userDetails;
    }
}
