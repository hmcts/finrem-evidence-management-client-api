package uk.gov.hmcts.reform.emclient.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.logging.httpcomponents.OutboundRequestIdSettingInterceptor;
import uk.gov.hmcts.reform.logging.httpcomponents.OutboundRequestLoggingInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

@Configuration
@RequiredArgsConstructor
public class EvidenceManagementClientConfiguration {

    public static final List<String> SUPPORTED_APPLICATION_SUBTYPES = asList(
        "vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json",
        "vnd.uk.gov.hmcts.reform.dm.document-collection.v1+hal+json",
        "vnd.uk.gov.hmcts.dm.document.v1+hal+json");

    private final ObjectMapper objectMapper;
    private final MappingJackson2HttpMessageConverter jackson2HttpMessageConverter;

    @Value("${http.connect.timeout}")
    private int httpConnectTimeout;

    @Value("${http.connect.request.timeout}")
    private int httpConnectRequestTimeout;

    @Bean
    public RestTemplate restTemplate() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.registerModule(new Jackson2HalModule());

        jackson2HttpMessageConverter.setObjectMapper(objectMapper);
        jackson2HttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes());

        RestTemplate restTemplate = new RestTemplate(asList(jackson2HttpMessageConverter,
                new FormHttpMessageConverter(),
                new ResourceHttpMessageConverter(),
                new ByteArrayHttpMessageConverter()));

        restTemplate.setRequestFactory(getClientHttpRequestFactory());

        return restTemplate;
    }

    private List<MediaType> supportedMediaTypes() {
        List<MediaType> supportedMediaTypes = newArrayList(MediaType.APPLICATION_JSON);
        supportedMediaTypes.addAll(
            SUPPORTED_APPLICATION_SUBTYPES.stream()
            .map(subtype -> new MediaType("application", subtype, StandardCharsets.UTF_8))
            .collect(Collectors.toList()));

        return supportedMediaTypes;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(httpConnectTimeout)
                .setConnectionRequestTimeout(httpConnectRequestTimeout)
                .build();

        CloseableHttpClient client = HttpClientBuilder
                .create()
                .useSystemProperties()
                .addInterceptorFirst(new OutboundRequestIdSettingInterceptor())
                .addInterceptorFirst((HttpRequestInterceptor) new OutboundRequestLoggingInterceptor())
                .addInterceptorLast((HttpResponseInterceptor) new OutboundRequestLoggingInterceptor())
                .setDefaultRequestConfig(config)
                .build();

        return new HttpComponentsClientHttpRequestFactory(client);
    }
}
