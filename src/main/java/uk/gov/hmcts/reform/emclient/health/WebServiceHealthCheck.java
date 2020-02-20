package uk.gov.hmcts.reform.emclient.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.unknown;
import static org.springframework.boot.actuate.health.Health.up;

@Slf4j
public abstract class WebServiceHealthCheck implements HealthIndicator {

    protected final RestTemplate restTemplate;
    private final HttpEntityFactory httpEntityFactory;
    private final String uri;

    public WebServiceHealthCheck(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate, String uri) {
        this.httpEntityFactory = httpEntityFactory;
        this.restTemplate = restTemplate;
        this.uri = uri;
    }

    public Health health() {
        HttpEntity<Object> httpEntity = httpEntityFactory.createRequestEntityForHealthCheck();
        ResponseEntity<Object> responseEntity;

        try {
            responseEntity = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, Object.class, new HashMap<>());
        } catch (HttpServerErrorException | ResourceAccessException serverException) {
            log.error("Exception occurred while doing health check", serverException);
            return down().withDetail("uri", uri).build();
        } catch (Exception exception) {
            log.info("Unable to access upstream service", exception);
            return unknown().withDetail("uri", uri).build();
        }

        return responseEntity.getStatusCode().equals(HttpStatus.OK) ?
                up().withDetail("uri", uri).build() :
                unknown().withDetail("uri", uri).build();
    }
}
