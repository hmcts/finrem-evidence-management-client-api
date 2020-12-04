package uk.gov.hmcts.reform.emclient.smoketests;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;

@RunWith(SpringRunner.class)
public class EmClientSmokeTests {

    @Value("${url}")
    private String url;

    @Value("${http.connect.timeout}")
    private int requestTimeOut;

    @Value("${http.connect.request.timeout}")
    private int connectionTimeOut;

    @Value("${http.connect.socketTimeOut}")
    private int socketTimeOut;

    @Value("${http.connect.connectionManagerTimeOut}")
    private int connectionManagerTimeOut;

    private RestAssuredConfig config;

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        config = RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", connectionTimeOut)
                        .setParam("http.socket.timeout", socketTimeOut)
                        .setParam("http.connection-manager.timeout", connectionManagerTimeOut)
                        .setParam("http.connect.request.timeout", requestTimeOut));

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
