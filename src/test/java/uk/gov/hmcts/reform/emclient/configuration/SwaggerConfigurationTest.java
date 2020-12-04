package uk.gov.hmcts.reform.emclient.configuration;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class SwaggerConfigurationTest {

    @Test
    public void docketBean() {
        assertThat(new SwaggerConfiguration().api(), is(notNullValue()));
    }
}
