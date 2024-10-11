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
public class StepThree {

    private final WebClient.Builder webClientBuilder;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private static final Logger LOG = LoggerFactory.getLogger(StepThree.class);


    public StepThree(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.retry = retryRegistry.retry("stepThree");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("stepThree");

        this.retry.getEventPublisher()
                .onRetry(e -> LOG.info("Paso 3. \n Número de intentos: {}", e.getNumberOfRetryAttempts()));

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event -> LOG.info("Transición del Circuit Breaker para el paso 3: de {} a {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }

    public Mono<String> StepThreeCall(String requestBody){
        return webClientBuilder.build()
                .post()
                .uri("http://api-three:8083/getStep")  // URL del otro servicio
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)  // Pasar el JSON como String
                .retrieve()
                .bodyToMono(String.class)  // Esperar la respuesta como String
                .doOnNext(response -> System.out.println("Respuesta del servicio: " + response))  // Imprimir la respuesta en la consola
                .transformDeferred(RetryOperator.of(retry))  // Operador de retry
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))  // Operador de CircuitBreaker
                .onErrorResume(throwable -> {  // Manejo de errores
                    LOG.warn("Demasiados reintentos para el paso 3, error: {}", throwable.getMessage());
                    return Mono.just("Step 3 no encontrado");
                })
                .doOnNext(body -> System.out.println("Respuesta del servicio: " + body));
    }

}
