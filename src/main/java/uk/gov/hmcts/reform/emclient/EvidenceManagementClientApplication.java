package uk.gov.hmcts.reform.emclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;
import uk.gov.hmcts.reform.authorisation.healthcheck.ServiceAuthHealthIndicator;
import uk.gov.hmcts.reform.emclient.idam.api.IdamApiClient;

@SpringBootApplication(exclude = {HypermediaAutoConfiguration.class})
@ComponentScan(basePackages = "uk.gov.hmcts", excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ServiceAuthHealthIndicator.class) })
@EnableRetry(proxyTargetClass=true)
@EnableFeignClients(basePackageClasses = {IdamApiClient.class})
@EnableCircuitBreaker
@Slf4j
public class EvidenceManagementClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvidenceManagementClientApplication.class, args);
    }

    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator(
            @Value("${idam.auth.secret}") final String s2sToken,
            @Value("${idam.auth.microservice}") final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        log.info("EMCA App S2S auth : s2sToken='{}', microService='{}', serviceAuthorisationApi='{}' ",
                s2sToken,  microService, serviceAuthorisationApi);
        return AuthTokenGeneratorFactory.createDefaultGenerator(s2sToken, microService, serviceAuthorisationApi);
    }

}
