package com.sanchez.jj.batch.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class Batch {

    private final WebClient webClient;

    String baseUrl = "http://orqwebflux:8086"; // Asegúrate de que este sea el valor correcto

    public Batch(WebClient.Builder webClientBuilder) {
        System.out.println("Base URL: " + baseUrl);  // Verifica el valor de baseUrl
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }


    @Scheduled(fixedRate = 20000, initialDelay = 5000)  // Agrega un retraso inicial de 5 segundos
    public void executeBatchProcess() {
        callOrchestrator();
    }

    private void callOrchestrator() {
        String jsonBody = """
        {
            "data": [
                {
                    "header": {
                        "id": "12345",
                        "type": "StepsGiraffeRefrigerator"
                    },
                    "enigma": "some_value"
                }
            ]
        }
        """;
    
        String uri = "/orquestador/fullAnswer";

        System.out.println("Calling orchestrator at: " + baseUrl + uri); // Imprime la URL antes de hacer la llamada
    
        webClient.post()
                .uri(uri)
                .bodyValue(jsonBody)  // Envía el JSON en el cuerpo de la solicitud
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(response -> {
                    System.out.println("Orchestrator response: " + response);
                }, error -> {
                    System.err.println("Error calling orchestrator: " + error.getMessage());
                });
    }
}    
