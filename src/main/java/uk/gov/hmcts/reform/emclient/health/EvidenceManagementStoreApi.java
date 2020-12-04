package uk.gov.hmcts.reform.emclient.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EvidenceManagementStoreApi extends WebServiceHealthCheck {

    public EvidenceManagementStoreApi(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate,
                                      @Value("${document.management.store.health.url}") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
