package uk.gov.hmcts.reform.finrem.evidencemanagement.clientapi.contracts.dmstore;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
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
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDeleteService;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

public class EvidenceManagementDeleteServiceConsumerTest extends BaseTest {

    private static final String authorizationToken = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String USER_ID_VALUE = "1000";
    public static final String REQUEST_ID = "reqId";
    private final String someServiceAuthToken = "someServiceAuthToken";
    private static final String USER_ID_HEADER = "user-id";
    private static final String DOCUMENT_ID = "5c3c3906-2b51-468e-8cbb-a4002eded075";
    private static final String DELETE_FILE_URL = "/documents/" + DOCUMENT_ID;

    @MockBean
    private UserService userService;

    @MockBean
    private IdamApiClient idamApiClient;

    @Mock
    private UserDetails mockUserDetails;

    @Autowired
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Autowired
    RestTemplate restTemplate;

    @Value("${document.management.store.delete.url}")
    private String documentManagementStoreDeleteUrl;

    @Rule
      public PactProviderRule mockProvider = new PactProviderRule("em_dm_store", "localhost", 8889, this);

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Pact(provider = "em_dm_store", consumer = "fr_evidenceManagementClient")
      public RequestResponsePact generatePactFragment(final PactDslWithProvider builder) throws JSONException, IOException {
        // @formatter:off
        return builder
          .given("A document to upload exists")
          .uponReceiving("A request to Delete the  document from dm-store")
          .method("DELETE")
          .headers(SERVICE_AUTHORIZATION_HEADER, someServiceAuthToken,USER_ID_HEADER, "1000")
          .path(DELETE_FILE_URL)
          .willRespondWith()
          .status(HttpStatus.SC_NO_CONTENT)
          .toPact();
    }

    @Test
    @PactVerification()
      public void verifyDocumentDeletedInDmStore() throws Exception {
        final UserDetails userDetails  = UserDetails.builder().id(USER_ID_VALUE).build();
        given(userService.getUserDetails(anyString())).willReturn(userDetails);
        given(authTokenGenerator.generate()).willReturn(someServiceAuthToken);

        final ResponseEntity<String> responseFromOperation  =  evidenceManagementDeleteService
            .deleteFile(documentManagementStoreDeleteUrl + "/" + DOCUMENT_ID,authorizationToken, REQUEST_ID);
        assertNotNull(responseFromOperation);
        assertTrue(responseFromOperation.getStatusCode().value() == HttpStatus.SC_NO_CONTENT);
    }
}
