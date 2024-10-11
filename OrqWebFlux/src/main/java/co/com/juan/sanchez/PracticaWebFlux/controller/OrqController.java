package co.com.juan.sanchez.PracticaWebFlux.controller;

import co.com.juan.sanchez.PracticaWebFlux.service.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orquestador")
public class OrqController {

    private final StepOne stepOne;
    private final StepTwo stepTwo;
    private final StepThree stepThree;
    private final FullAnswer fullCompleted;

    // Inyectar los servicios en el constructor
    public OrqController(StepOne stepOne, StepTwo stepTwo, StepThree stepThree, FullAnswer fullCompleted) {
        this.stepOne = stepOne;
        this.stepTwo = stepTwo;
        this.stepThree = stepThree;
        this.fullCompleted = fullCompleted;
    }

    // Callback para StepOne
    @PostMapping("/stepOne")
    public Mono<ResponseEntity<String>> callStepOne(@RequestBody String requestBody) {
        return stepOne.callStepOne(requestBody)
                .map(ResponseEntity::ok)
                .onErrorResume(throwable -> {
                    logError("StepOne", throwable);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error en Step 1: " + throwable.getMessage()));
                });
    }

    // Callback para StepTwo
    @PostMapping("/stepTwo")
    public Mono<ResponseEntity<String>> callStepTwo(@RequestBody String requestBody) {
        return stepTwo.StepTwoCall(requestBody)
                .map(ResponseEntity::ok)
                .onErrorResume(throwable -> {
                    logError("StepTwo", throwable);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error en Step 2: " + throwable.getMessage()));
                });
    }

    // Callback para StepThree
    @PostMapping("/stepThree")
    public Mono<ResponseEntity<String>> callStepThree(@RequestBody String requestBody) {
        return stepThree.StepThreeCall(requestBody)
                .map(ResponseEntity::ok)
                .onErrorResume(throwable -> {
                    logError("StepThree", throwable);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error en Step 3: " + throwable.getMessage()));
                });
    }

    // Callback para obtener la respuesta completa
    @PostMapping("/fullAnswer")
    public Mono<ResponseEntity<String>> getCompleteAnswer(@RequestBody String requestBody) {
        return fullCompleted.callAllSteps(requestBody)
                .map(ResponseEntity::ok)
                .onErrorResume(throwable -> {
                    logError("FullAnswer", throwable);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error en la respuesta completa: " + throwable.getMessage()));
                });
    }

    // Método para registrar los errores en un log
    private void logError(String step, Throwable throwable) {
        // Aquí podrías agregar el manejo de log para errores
        System.err.println("Error en " + step + ": " + throwable.getMessage());
    }
}

