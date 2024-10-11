package co.com.juan.sanchez.PracticaWebFlux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FullAnswer {

    private final StepOne stepOne;
    private final StepTwo stepTwo;
    private final StepThree stepThree;
    private final WebHook webHook;
    private static final Logger LOG = LoggerFactory.getLogger(FullAnswer.class);

    public FullAnswer(StepOne stepOne, StepTwo stepTwo, StepThree stepThree, WebHook webHook) {
        this.stepOne = stepOne;
        this.stepTwo = stepTwo;
        this.stepThree = stepThree;
        this.webHook = webHook;
    }

    public Mono<String> callAllSteps(String requestBody) {
        return webHook.WHStart()
                .then(
                        Mono.zip(
                                        stepOne.callStepOne(requestBody).map(this::extraerAnswer),
                                        stepTwo.StepTwoCall(requestBody).map(this::extraerAnswer),
                                        stepThree.StepThreeCall(requestBody).map(this::extraerAnswer)
                                )
                                .flatMap(tuple3 -> {
                                    String answerStepOne = tuple3.getT1();
                                    String answerStepTwo = tuple3.getT2();
                                    String answerStepThree = tuple3.getT3();

                                    // Log para verificar las respuestas extraídas
                                    LOG.info("Respuesta Step 1: {}", answerStepOne);
                                    LOG.info("Respuesta Step 2: {}", answerStepTwo);
                                    LOG.info("Respuesta Step 3: {}", answerStepThree);

                                    // Manejo de errores si alguno de los pasos falla
                                    if (answerStepOne.contains("Error") || answerStepTwo.contains("Error") || answerStepThree.contains("Error")) {
                                        return Mono.just("Error en uno de los pasos. Respuestas: " + answerStepOne + ", " + answerStepTwo + ", " + answerStepThree);
                                    }

                                    // Crear la respuesta final
                                    String finalAnswer = String.format(
                                            "{\"data\": [{\"header\": {\"id\": \"12345\", \"type\": \"TestGiraffeRefrigerator\"}, \"answer\": \"Step1: %s - Step2: %s - Step3: %s\"}]}",
                                            answerStepOne, answerStepTwo, answerStepThree
                                    );

                                    // Enviar la respuesta final al WebHook
                                    return webHook.WH(finalAnswer)
                                            .flatMap(webHookResponse -> {
                                                LOG.info("Respuesta final enviada al WebHook: {}", webHookResponse);
                                                return Mono.just("Respuesta final enviada al WH: " + webHookResponse + "\nAnswered: " + finalAnswer);
                                            });
                                })
                )
                .onErrorResume(throwable -> {
                    LOG.warn("Error en callAllSteps: {}", throwable.getMessage());
                    return Mono.just("Error al procesar los pasos: " + throwable.getMessage());
                });
    }

    private String extraerAnswer(String response) {
        System.out.println("Respuesta completa: " + response);  // Log para verificar la respuesta
        ObjectMapper mapper = new ObjectMapper();

        // Validar si la respuesta es un JSON o texto plano
        if (response.startsWith("{") || response.startsWith("[")) {
            try {
                // Intentar convertir la respuesta en un JSONNode
                JsonNode root = mapper.readTree(response);

                // Verificar si la ruta existe en el JSON
                // Ajustar la ruta para acceder al primer elemento del array raíz y luego al campo "answer"
                JsonNode answerNode = root.at("/0/data/0/answer");
                if (!answerNode.isMissingNode()) {
                    return answerNode.asText();
                } else {
                    System.err.println("Campo 'answer' no encontrado en la respuesta JSON.");
                    return "Campo 'answer' no encontrado";
                }
            } catch (JsonProcessingException e) {
                // Si ocurre un error al procesar la respuesta, devolver un mensaje apropiado
                System.err.println("Error al procesar la respuesta JSON: " + e.getMessage());
                return "Error al procesar la respuesta JSON";
            }
        } else {
            // Si no es un JSON, retornar la respuesta como error o mensaje de texto plano
            return response;
        }
    }

}
