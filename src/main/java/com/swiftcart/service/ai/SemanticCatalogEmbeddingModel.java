package com.swiftcart.service.ai;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
public class SemanticCatalogEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSIONS = 128;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> instructions = request.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            float[] vector = computeEmbedding(instructions.get(i));
            embeddings.add(new Embedding(vector, i));
        }
        return new EmbeddingResponse(embeddings, new EmbeddingResponseMetadata());
    }

    @Override
    public float[] embed(Document document) {
        return computeEmbedding(document.getContent());
    }

    @Override
    public float[] embed(String text) {
        return computeEmbedding(text);
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    /**
     * Computes a normalized semantic embedding vector using subword n-gram hashing and term weighting.
     */
    public float[] computeEmbedding(String text) {
        float[] vector = new float[DIMENSIONS];
        if (text == null || text.isBlank()) {
            return vector;
        }

        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        String[] tokens = normalized.split(" ");

        for (String token : tokens) {
            if (token.isBlank()) continue;

            // Token level hash
            int tokenHash = Math.abs(hashString(token));
            int tokenIndex = tokenHash % DIMENSIONS;
            float weight = token.length() > 3 ? 1.5f : 1.0f;
            vector[tokenIndex] += weight;

            // Character 3-gram hashes for subword capture (e.g. "wireless", "headphone", "laptop")
            for (int i = 0; i <= token.length() - 3; i++) {
                String sub = token.substring(i, i + 3);
                int subHash = Math.abs(hashString(sub));
                int subIndex = subHash % DIMENSIONS;
                vector[subIndex] += 0.5f;
            }
        }

        // L2 Normalization
        double sumSq = 0.0;
        for (float val : vector) {
            sumSq += val * val;
        }

        if (sumSq > 0.0) {
            float norm = (float) Math.sqrt(sumSq);
            for (int i = 0; i < DIMENSIONS; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    private int hashString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return ((bytes[0] & 0xFF) << 24) |
                    ((bytes[1] & 0xFF) << 16) |
                    ((bytes[2] & 0xFF) << 8) |
                    (bytes[3] & 0xFF);
        } catch (NoSuchAlgorithmException e) {
            return input.hashCode();
        }
    }
}
