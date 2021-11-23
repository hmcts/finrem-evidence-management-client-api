package uk.gov.hmcts.reform.emclient.idam.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class IdamTokens {
    String idamOauth2Token;
    String serviceAuthorization;
    String userId;
    String email;
    List<String> roles;
}
