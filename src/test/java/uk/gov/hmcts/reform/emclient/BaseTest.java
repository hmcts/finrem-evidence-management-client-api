package uk.gov.hmcts.reform.emclient;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EvidenceManagementClientApplication.class)
@TestPropertySource(locations ="/application-contract.properties")
public abstract class BaseTest {
    @MockBean
    protected AuthTokenGenerator authTokenGenerator;
}


