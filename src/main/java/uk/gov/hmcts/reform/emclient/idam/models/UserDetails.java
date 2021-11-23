package uk.gov.hmcts.reform.emclient.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class UserDetails {

    private String id;
    private String email;
    private String forename;
    private String surname;
    private List<String> roles;

}