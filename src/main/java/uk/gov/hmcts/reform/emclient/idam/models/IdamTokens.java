package uk.gov.hmcts.reform.emclient.idam.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonDeserialize(builder = IdamTokens.IdamTokensBuilder.class)
public class IdamTokens {
    String idamOauth2Token;
    String serviceAuthorization;
    String userId;
    String email;
    List<String> roles;
}
