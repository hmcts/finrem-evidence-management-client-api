package uk.gov.hmcts.reform.finrem.emclient.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserCode {
    private String code;
}
