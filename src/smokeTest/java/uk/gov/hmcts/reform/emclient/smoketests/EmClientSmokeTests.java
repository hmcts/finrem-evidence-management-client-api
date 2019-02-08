package uk.gov.hmcts.reform.emclient.smoketests;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SmokeTestConfiguration.class})
public class EmClientSmokeTests {

    @Value("${url}")
    private String url;

    private RestAssuredConfig config;

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        config = RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 30000)
                        .setParam("http.socket.timeout", 30000)
                        .setParam("http.connection-manager.timeout", 30000)
                        .setParam("http.connect.request.timeout", 30000));

    }

    @Test
    public void shouldGetOkStatusFromHealthEndpoint() {
        given().config(config)
                .when()
                .get(url + "/health")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
