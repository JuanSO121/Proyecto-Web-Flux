package co.com.juan.sanchez.PracticaWebFlux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class Orq {

    private final WebClient webClient;

    //El constructor me permite acceder a los metodos de Flux
    public Orq(WebClient.Builder webClient) {
        this.webClient = webClient.build(); //Seguramente la configuración para conectar con la información
    }

    public Mono<String> runService1(String requestBody) {
        return webClient.post()
                .uri("http://api-one:8081/getStep")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(), // Interceptar errores 4xx y 5xx
                        clientResponse -> Mono.just(new RuntimeException("Paso 1 no encontrado"))
                )
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("Respuesta del servicio 1: " + response))
                .onErrorResume(e -> Mono.just("Error en el paso 1: " + e.getMessage()));
    }

    public Mono<String> runService2(String requestBody) {
        return webClient.post()
                .uri("http://api-two:8082/getStep")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.just(new RuntimeException("Paso 2 no encontrado"))
                )
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("Respuesta del servicio 2: " + response))
                .onErrorResume(e -> Mono.just("Error en el paso 2: " + e.getMessage()));
    }

    public Mono<String> runService3(String requestBody) {
        return webClient.post()
                .uri("http://api-three:8083/getStep")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.just(new RuntimeException("Paso 3 no encontrado"))
                )
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("Respuesta del servicio 3: " + response))
                .onErrorResume(e -> Mono.just("Error en el paso 3: " + e.getMessage()));
    }

    ///////////////////////////
    public Mono<String> runServices(String requestBody) {
        Mono<String> servicio1 = runService1(requestBody).map(this::extraerAnswer);
        Mono<String> servicio2 = runService2(requestBody).map(this::extraerAnswer);
        Mono<String> servicio3 = runService3(requestBody).map(this::extraerAnswer);

        return Mono.zip(servicio1, servicio2, servicio3)
                .map(tuple -> {
                    String step1Answer = tuple.getT1();
                    String step2Answer = tuple.getT2();
                    String step3Answer = tuple.getT3();

                    String finalAnswer = String.format(
                            "{\"data\": [{\"header\": {\"id\": \"12345\", \"type\": \"TestGiraffeRefrigerator\"}, \"answer\": \"Step1: %s - Step2: %s - Step3: %s\"}]}",
                            step1Answer, step2Answer, step3Answer
                    );

                    System.out.println("Respuesta final: " + finalAnswer);
                    return finalAnswer;
                })
                .onErrorResume(e -> {
                    System.err.println("Error al ejecutar los servicios: " + e.getMessage());
                    return Mono.just("Error al procesar los pasos: " + e.getMessage());
                });
    }

    // Metodo para extraer el campo "answer" de la respuesta
    private String extraerAnswer(String response) {
        System.out.println("Respuesta completa: " + response);  // Log para verificar la respuesta
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(response);
            JsonNode answerNode = root.at("/data/0/answer");

            if (answerNode.isMissingNode() || answerNode.asText().isEmpty()) {
                System.err.println("El campo 'answer' no se encontró o está vacío.");
                return "Error al procesar la respuesta: Campo 'answer' no encontrado";
            }

            return answerNode.asText();
        } catch (JsonProcessingException e) {
            System.err.println("Error al procesar la respuesta JSON: " + e.getMessage());
            return "Error al procesar la respuesta JSON";
        }
    }

    


}
