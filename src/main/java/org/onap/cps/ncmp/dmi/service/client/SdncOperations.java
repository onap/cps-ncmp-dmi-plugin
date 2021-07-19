package org.onap.cps.ncmp.dmi.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SdncOperations {

    private static final String GET_SCHEMA_URL = "xxx";

    @Autowired
    private SdncRestconfClient sdncRestconfClient;

    public ResponseEntity<String> getSchemasFromNode(final String nodeId, final String acceptMediaType) {
        final StringBuilder builder = new StringBuilder(GET_SCHEMA_URL + nodeId);
        return sdncRestconfClient.getOp(builder.toString(), acceptMediaType);
    }
}
