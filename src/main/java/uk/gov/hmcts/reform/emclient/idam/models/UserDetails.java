package uk.gov.hmcts.reform.emclient.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = UserDetails.UserDetailsBuilder.class)
public class UserDetails {

    private String id;
    private String email;
    private String forename;
    private String surname;
    private List<String> roles;

}