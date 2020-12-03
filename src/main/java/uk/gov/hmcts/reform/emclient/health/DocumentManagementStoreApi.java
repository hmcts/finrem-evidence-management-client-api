package uk.gov.hmcts.reform.emclient.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DocumentManagementStoreApi extends WebServiceHealthCheck {
    @Autowired
    public DocumentManagementStoreApi(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate,
                                      @Value("${document.management.store.health.url}") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
