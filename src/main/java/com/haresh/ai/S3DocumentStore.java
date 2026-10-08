package com.haresh.ai;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight S3-backed document store used as grounding context
 * for model calls. Prefix-scoped so one bucket can serve many projects.
 */
public class S3DocumentStore implements AutoCloseable {

    private final S3Client s3;
    private final String bucket;

    public S3DocumentStore(Region region, String bucket) {
        this.s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.bucket = bucket;
    }

    public void put(String prefix, String docId, String text) {
        s3.putObject(
                PutObjectRequest.builder().bucket(bucket).key(prefix + "/" + docId + ".txt").build(),
                RequestBody.fromString(text, StandardCharsets.UTF_8));
    }

    public String get(String key) {
        return s3.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build())
                .asUtf8String();
    }

    /** Concatenate all documents under a prefix — simple "retrieve everything" baseline. */
    public String getAllAsContext(String prefix, int maxChars) {
        ListObjectsV2Iterable pages = s3.listObjectsV2Paginator(
                ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build());
        StringBuilder sb = new StringBuilder();
        List<String> keys = new ArrayList<>();
        for (var page : pages) {
            page.contents().forEach(o -> keys.add(o.key()));
        }
        for (String key : keys) {
            if (sb.length() >= maxChars) break;
            String doc = get(key);
            sb.append("--- ").append(key).append(" ---\n")
              .append(doc, 0, Math.min(doc.length(), maxChars - sb.length()))
              .append("\n");
        }
        return sb.toString();
    }

    @Override
    public void close() {
        s3.close();
    }
}
