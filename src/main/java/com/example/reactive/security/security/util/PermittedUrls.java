package com.example.reactive.security.security.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@Component
public final class PermittedUrls {
    private final Set<PathPatternParserServerWebExchangeMatcher> permitAllMatchers;

    private PermittedUrls(Set<PathPatternParserServerWebExchangeMatcher> permitAllMatchers) {
        this.permitAllMatchers = permitAllMatchers;
    }

    public Mono<Boolean> isPermitAllRequest(ServerWebExchange exchange) {
        return Mono.just(permitAllMatchers)
                .flatMapMany(Flux::fromIterable)
                .flatMap(matcher -> matcher.matches(exchange))
                .any(ServerWebExchangeMatcher.MatchResult::isMatch);
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private final Set<PathPatternParserServerWebExchangeMatcher> permitAllMatchers = new HashSet<>();

        public Builder addPermitAllMatcher(HttpMethod httpMethod, String pattern) {
            permitAllMatchers.add(new PathPatternParserServerWebExchangeMatcher(pattern, httpMethod));
            return this;
        }

        public Builder addPermitAllMatcher(String pattern) {
            permitAllMatchers.add(new PathPatternParserServerWebExchangeMatcher(pattern));
            return this;
        }

        public PermittedUrls build() {
            return new PermittedUrls(new HashSet<>(permitAllMatchers));
        }
    }
}