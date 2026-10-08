package com.haresh.ai;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InferenceConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;

/**
 * Chat completions via Amazon Bedrock (Converse API).
 * Model id and region come from the environment so the same code
 * works across accounts and regions.
 */
public class BedrockChatClient implements AutoCloseable {

    private static final String DEFAULT_MODEL = "anthropic.claude-3-5-sonnet-20240620-v1:0";

    private final BedrockRuntimeClient client;
    private final String modelId;

    public BedrockChatClient(Region region) {
        this.client = BedrockRuntimeClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.modelId = System.getenv().getOrDefault("BEDROCK_MODEL_ID", DEFAULT_MODEL);
    }

    /** Ask a question with optional retrieved context and a system prompt. */
    public String ask(String systemPrompt, String context, String question) {
        String userText = context == null || context.isBlank()
                ? question
                : "Context:\n" + context + "\n\nQuestion: " + question;

        ConverseRequest request = ConverseRequest.builder()
                .modelId(modelId)
                .system(SystemContentBlock.builder().text(systemPrompt).build())
                .messages(Message.builder()
                        .role(ConversationRole.USER)
                        .content(ContentBlock.builder().text(userText).build())
                        .build())
                .inferenceConfig(InferenceConfiguration.builder()
                        .maxTokens(1024)
                        .temperature(0.2f)
                        .build())
                .build();

        ConverseResponse response = client.converse(request);
        return response.output().message().content().get(0).text();
    }

    public String ask(String question) {
        return ask("You are a helpful assistant.", null, question);
    }

    @Override
    public void close() {
        client.close();
    }
}
