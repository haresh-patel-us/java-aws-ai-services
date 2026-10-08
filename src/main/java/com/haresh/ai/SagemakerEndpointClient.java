package com.haresh.ai;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;

import java.nio.charset.StandardCharsets;

/**
 * Invokes a SageMaker real-time endpoint with a JSON payload.
 * Used for custom or fine-tuned models that live behind an endpoint
 * rather than Bedrock.
 */
public class SagemakerEndpointClient implements AutoCloseable {

    private final SageMakerRuntimeClient client;
    private final String endpointName;

    public SagemakerEndpointClient(Region region, String endpointName) {
        this.client = SageMakerRuntimeClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.endpointName = endpointName;
    }

    /** Send a JSON payload and return the raw response body. */
    public String invokeJson(String jsonPayload) {
        InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                .endpointName(endpointName)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        InvokeEndpointResponse response = client.invokeEndpoint(request);
        return response.body().asUtf8String();
    }

    @Override
    public void close() {
        client.close();
    }
}
