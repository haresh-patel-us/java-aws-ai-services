# Java + AWS AI Services

Portfolio sample: a Java 17 service calling AWS AI/ML services through the
AWS SDK for Java v2 — Amazon Bedrock (foundation models via the Converse API),
a SageMaker real-time endpoint, and S3 as a document store for retrieval-style
workflows. This mirrors the AI platform patterns I run: Bedrock for
general-purpose LLM calls, SageMaker for custom/deployed models, and S3 for
grounding data.

## Layout

- `BedrockChatClient.java` — chat completion via Bedrock Converse API
- `SagemakerEndpointClient.java` — invoke a SageMaker real-time endpoint
- `S3DocumentStore.java` — put/get/list documents used as model context
- `AiServiceDemo.java` — wires the three together (retrieve -> prompt -> answer)

## Build & run

```bash
mvn -q package
java -jar target/java-aws-ai-services-1.0.0.jar
```

AWS credentials come from the default SDK chain (env vars, `~/.aws/credentials`,
or instance role). Set the model and endpoint via env vars:

- `BEDROCK_MODEL_ID` (default `anthropic.claude-3-5-sonnet-20240620-v1:0`)
- `SAGEMAKER_ENDPOINT` (name of your real-time endpoint)
- `DOCS_BUCKET` (S3 bucket holding context documents)

## Patterns demonstrated

- Bedrock Converse API with system prompt + inference config
- SageMaker endpoint invocation with JSON payload
- S3 as a lightweight document store (prefix-scoped, paginated)
- Clean separation: each AWS service behind a small client class
