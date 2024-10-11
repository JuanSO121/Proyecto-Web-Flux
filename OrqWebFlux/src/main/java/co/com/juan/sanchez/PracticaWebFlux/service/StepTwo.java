package co.com.juan.sanchez.PracticaWebFlux.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class StepTwo {

    private final WebClient.Builder webClientBuilder;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private static final Logger LOG = LoggerFactory.getLogger(StepTwo.class);

    public StepTwo(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.retry = retryRegistry.retry("stepTwo");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("stepTwo");

        this.retry.getEventPublisher()
                .onRetry(e -> LOG.info("Paso 2. \n Número de intentos: {}", e.getNumberOfRetryAttempts()));

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event -> LOG.info("Transición del Circuit Breaker para el paso 2: de {} a {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }

    public Mono<String> StepTwoCall(String requestBody){
        return webClientBuilder.build()
                .post()
                .uri("http://api-two:8082/getStep")  // URL del otro servicio
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)  // Pasar el JSON como String
                .retrieve()
                .bodyToMono(String.class)  // Esperar la respuesta como String
                .doOnNext(response -> System.out.println("Respuesta del servicio: " + response))  // Imprimir la respuesta en la consola
                .transformDeferred(RetryOperator.of(retry))  // Operador de retry
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))  // Operador de CircuitBreaker
                .onErrorResume(throwable -> {  // Manejo de errores
                    LOG.warn("Demasiados reintentos para el paso 2, error: {}", throwable.getMessage());
                    return Mono.just("Step 3 no encontrado");
                })
                .doOnNext(body -> System.out.println("Respuesta del servicio: " + body));
    }
}
