package com.example.reactive.security.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequestMapping("/testing")
@RestController
public class PingPongController {
    @PostMapping
    public Mono<ResponseEntity<Void>> testing(){

        return Mono.just(ResponseEntity.noContent().build());
    }
}
