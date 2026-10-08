package com.haresh.ai;

import software.amazon.awssdk.regions.Region;

/**
 * Demo: retrieve grounding docs from S3, then ask Bedrock with that context.
 * SageMaker endpoint usage is shown behind the SAGEMAKER_ENDPOINT env var
 * (skipped if unset).
 */
public class AiServiceDemo {

    public static void main(String[] args) {
        Region region = Region.of(System.getenv().getOrDefault("AWS_REGION", "us-west-2"));
        String docsBucket = System.getenv().getOrDefault("DOCS_BUCKET", "demo-docs-bucket");

        try (S3DocumentStore store = new S3DocumentStore(region, docsBucket);
             BedrockChatClient bedrock = new BedrockChatClient(region)) {

            // Seed one sample document (idempotent overwrite is fine for a demo)
            store.put("policies", "refunds",
                    "Refund policy: orders may be refunded within 30 days of purchase. "
                    + "Digital goods are refundable within 7 days.");

            String context = store.getAllAsContext("policies", 4000);
            String answer = bedrock.ask(
                    "You answer questions using only the provided context.",
                    context,
                    "What is the refund policy for digital goods?");
            System.out.println("Bedrock answer:\n" + answer);

            String endpoint = System.getenv("SAGEMAKER_ENDPOINT");
            if (endpoint != null && !endpoint.isBlank()) {
                try (SagemakerEndpointClient sm = new SagemakerEndpointClient(region, endpoint)) {
                    String result = sm.invokeJson("{\"inputs\": \"What is the refund window?\"}");
                    System.out.println("SageMaker answer:\n" + result);
                }
            } else {
                System.out.println("SAGEMAKER_ENDPOINT not set — skipping SageMaker demo.");
            }
        }
    }
}
