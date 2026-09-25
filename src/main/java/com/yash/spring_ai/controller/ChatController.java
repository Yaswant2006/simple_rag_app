package com.yash.spring_ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient=chatClientBuilder
                .defaultAdvisors(List.of(MessageChatMemoryAdvisor.builder(chatMemory).build(),new SimpleLoggerAdvisor())).
                build();
    }

    @GetMapping
    public ResponseEntity<Flux<String>> message(@RequestParam("message") String message,
                                                @RequestHeader("conversation_id") String conversation_id) {
        System.out.println(message);
        return ResponseEntity.ok(
                chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID,conversation_id))
                .stream()
                .content());
    }
}
