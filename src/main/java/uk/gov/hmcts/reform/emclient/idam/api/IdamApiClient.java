package uk.gov.hmcts.reform.emclient.idam.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface IdamApiClient {

    @RequestMapping(method = RequestMethod.GET, value = "/details")
    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

}
