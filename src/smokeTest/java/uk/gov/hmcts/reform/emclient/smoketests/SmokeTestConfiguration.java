package uk.gov.hmcts.reform.emclient.smoketests;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan("uk.gov.hmcts.reform.emclient")
@PropertySource("application.properties")
public class SmokeTestConfiguration {
}
