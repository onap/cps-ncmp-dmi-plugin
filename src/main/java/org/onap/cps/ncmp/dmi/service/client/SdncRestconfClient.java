package org.onap.cps.ncmp.dmi.service.client;

import org.onap.cps.ncmp.dmi.config.DmiConfiguration.SdncProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SdncRestconfClient {
    private SdncProperties sdncProperties;
    private RestTemplate restTemplate;

    public SdncRestconfClient(final SdncProperties sdncProperties, final RestTemplate restTemplate) {
        this.sdncProperties = sdncProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * restconf get operation on sdnc.
     * @param sdncGetUrl sdnc get url
     *
     * @return the response entity
     */
    public ResponseEntity<String> getOp(final String sdncGetUrl, final String mediaType){
        final var httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(sdncProperties.getAuthUsername(), sdncProperties.getAuthPassword());
        httpHeaders.set(HttpHeaders.ACCEPT, mediaType);
        final var httpEntity = new HttpEntity<>(httpHeaders);
        return restTemplate.exchange(sdncGetUrl, HttpMethod.GET, httpEntity, String.class);
    }
}
