package com.yash.spring_ai.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/PromptTemplates/SystemPromptRandomDataLoader.st")
    Resource promptTemplate;

    public RagController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @GetMapping
    public ResponseEntity<String> randomChat(@RequestHeader("username") String userName,
                                             @RequestParam("chat") String message){

        SearchRequest searchRequest = SearchRequest.builder()
                .query(message).topK(3).similarityThreshold(0.5).build();
        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);
        String similarContent = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));
        return ResponseEntity.ok(
                chatClient.prompt()
                        .system(
                                promptSystemSpec -> promptSystemSpec.text(promptTemplate)
                                .param("documents",similarContent)
                        ).advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID,userName))
                        .user(message)
                        .call().content()
        );
    }

}
