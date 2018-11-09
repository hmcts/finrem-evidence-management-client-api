package uk.gov.hmcts.reform.finrem.emclient;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.emclient.model.CreateUserRequest;
import uk.gov.hmcts.reform.finrem.emclient.model.UserCode;

import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class IDAMUtils {

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${auth.idam.client.redirectUri}")
    private String idamRedirectUri;

    @Value("${auth.idam.client.secret}")
    private String idamSecret;

    private String idamUsername;

    private String idamPassword;

    private String testUserJwtToken;

    private String testCaseworkerJwtToken;

    public String generateNewUserAndReturnToken() {
        String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
        String password = UUID.randomUUID().toString().toUpperCase(Locale.UK);
        createUserInIdam(username, password);
        return generateUserTokenWithNoRoles(username, password);
    }

    public synchronized String getIdamTestUser() {
        if (StringUtils.isBlank(testUserJwtToken)) {
            createUserAndToken();
        }
        return testUserJwtToken;
    }

    protected void createUserAndToken() {
//        createUserInIdam();
        String[] emails = { "kate_fr_courtadmn@mailinator.com",
                "nasim_fr_courtadmn@mailinator.com",
                "vivek_fr_courtadmn@mailinator.com",
                "atique_fr_courtadmn@mailinator.com",
                "phi_fr_courtadmn@mailinator.com",
                "mahesh_fr_courtadmn@mailinator.com"
        };

        String token = null;
        for (String email:emails) {
            token = generateUserTokenWithNoRoles(email, "London01");

            log.info("token found for: token='{}', email='{}'", token, email);
        }

        testUserJwtToken = generateUserTokenWithNoRoles("nasim_fr_courtadmn@mailinator.com", "London01");
    }

    public synchronized String getIdamTestCaseWorkerUser() {
        if (StringUtils.isBlank(testCaseworkerJwtToken)) {
            String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
            String password = UUID.randomUUID().toString().toUpperCase(Locale.UK);
            createCaseworkerUserInIdam(username, password);
            testCaseworkerJwtToken = generateUserTokenWithNoRoles(username, password);
        }

        return testCaseworkerJwtToken;
    }

    public void createUserInIdam(String username, String password) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .email(username)
                .password(password)
                .forename("Test")
                .surname("User")
                .roles(new UserCode[] { UserCode.builder().code("citizen").build() })
                .userGroup(UserCode.builder().code("divorce-private-beta").build())
                .build();

        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(ResourceLoader.objectToJson(userRequest))
                .post(idamCreateUrl());
    }

    private void createUserInIdam() {
        idamUsername = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
        idamPassword = UUID.randomUUID().toString();

        createUserInIdam(idamUsername, idamPassword);
    }

    public void createCaseworkerUserInIdam(String username, String password) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .email(username)
                .password(password)
                .forename("Test")
                .surname("User")
                .roles(new UserCode[] { UserCode.builder().code("caseworker-divorce-courtadmin").build() })
                .userGroup(UserCode.builder().code("caseworker").build())
                .build();

        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(ResourceLoader.objectToJson(userRequest))
                .post(idamCreateUrl());
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    public String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));

        Response response = RestAssured.given()
                .header("Authorization", authHeader)
                .relaxedHTTPSValidation()
                .post(idamCodeUrl());

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                    + " body: " + response.getBody().prettyPrint());
        }
        String authCode = response.getBody().path("code");

        log.info("code found for: user='{}', code='{}'", username, authCode);

        response = RestAssured.given()
                .header("Authorization", authHeader)
                .relaxedHTTPSValidation()
                .post(idamTokenUrl(authCode));

        log.info("status code for token endpoint: code='{}', response body={}", response.getStatusCode(), response.getBody().prettyPrint());

        String token = response.getBody().path("access_token");
        return "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJpZGFtIiwiaWF0IjoxNDgzMjI4ODAwLCJleHAiOjQxMDI0NDQ4MDAsImF1ZCI6ImNtYyIsInN1YiI6ImNtYyJ9.Q9-gf315saUt007Gau0tBUxevcRwhEckLHzC82EVGIM";//+ token;
    }

    private String idamCodeUrl() {
        return idamUserBaseUrl + "/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=divorce"
                + "&redirect_uri=" + idamRedirectUri;
    }

    private String idamTokenUrl(String code) {
        return idamUserBaseUrl + "/oauth2/token"
                + "?code=" + code
                + "&client_id=divorce"
                + "&client_secret=" + idamSecret
                + "&redirect_uri=" + idamRedirectUri
                + "&grant_type=authorization_code";
    }
}