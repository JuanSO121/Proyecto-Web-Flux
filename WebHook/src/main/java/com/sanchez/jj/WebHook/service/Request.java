package com.sanchez.jj.WebHook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class Request {

    private static final String EXPECTED_STEPS = "Step1: Open the refrigerator - Step2: Put the giraffe in - Step3: Close the door";
    private static final Logger LOG = LoggerFactory.getLogger(Request.class);

    @Async
    public CompletableFuture<Void> processAsynchronously(String jsonBody) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String result = validateSteps(jsonBody);
        notifyClient(result);

        return CompletableFuture.completedFuture(null);
    }

    private String validateSteps(String jsonBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonBody);
            String receivedSteps = rootNode.at("/data/0/answer").asText();  // Ruta hacia el campo "answer"

            if (EXPECTED_STEPS.equals(receivedSteps)) {
                return "recibido el mensaje del orquestador";
            } else {
                return "Error: Pasos incorrectos: " + receivedSteps;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing the JSON: " + e.getMessage();
        }
    }

    private void notifyClient(String message) {
        LOG.info("Mensaje WebHook: {}", message);
    }

}

