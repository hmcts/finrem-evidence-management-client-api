package uk.gov.hmcts.reform.emclient.configuration;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SwaggerConfigurationTest {

    @Test
    public void docketBean() {
        assertThat(new SwaggerConfiguration().api(), is(notNullValue()));
    }
}
