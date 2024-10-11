package co.com.juan.sanchez.PracticaWebFlux.service;

import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

@Service
public class WebHook {

    private final WebClient webClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private static final Logger LOG = LoggerFactory.getLogger(WebHook.class);


    public WebHook(WebClient.Builder webClient, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.webClient = webClient.build();
        this.retry = retryRegistry.retry("webHook");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("webHook");

        this.retry.getEventPublisher()
                .onRetry(e -> LOG.info("webHook. \n Número de intentos: {}", e.getNumberOfRetryAttempts()));

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event -> LOG.info("Transición del Circuit Breaker para el paso webHook: de {} a {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }

    public Mono<String> WH(String request) {
        return webClient.post()
                .uri("http://webhook:8085/validate")
                .bodyValue(request)  // Pasar el JSON como String
                .retrieve()
                .bodyToMono(String.class)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(throwable -> {
                    LOG.warn("Demasiados reintentos para webHook, error: {}", throwable.getMessage());
                    return Mono.just("webHook no encontrado");
                })
                .doOnNext(body -> System.out.println("Respuesta del servicio: " + body));
    }



    ///
    public Mono<String> WHStart(){
        return webClient
                .get()
                .uri("http://webhook:8085/start")
                .retrieve()
                .bodyToMono(String.class)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(throwable -> {
                    LOG.warn("Demasiados reintentos para el WebHookStart, error: {}", throwable.getMessage());
                    return Mono.just("WebHookStart no encontrado");
                })
                .doOnNext(body -> System.out.println(" " + body));

    }
}