package uk.gov.hmcts.reform.emclient.idam.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class IdamTokens {
    String idamOauth2Token;
    String serviceAuthorization;
    String userId;
    String email;
    List<String> roles;
}
