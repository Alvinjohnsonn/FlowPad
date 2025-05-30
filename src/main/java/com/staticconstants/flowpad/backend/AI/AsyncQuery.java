package com.staticconstants.flowpad.backend.AI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.staticconstants.flowpad.frontend.textarea.*;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.response.OllamaAsyncResultStreamer;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;

import static com.staticconstants.flowpad.FlowPadApplication.aiExecutor;


public class AsyncQuery {
    static final String LLAMA3 = "llama3.2";
    private static final int HTTP_OK = 200;
    static final String host = "http://localhost:11434/";
    private final static String PROMPT = "List all cricket world cup teams of 2019.";


    public static void main(String[] args) throws Exception {
        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(60);

        OllamaAsyncResultStreamer streamer = ollamaAPI.generateAsync(LLAMA3, PROMPT, false);

        int pollIntervalMilliseconds = 1000;

        while (true) {
            String tokens = streamer.getStream().poll();
            System.out.print("TOKENS: " + tokens);
            if (!streamer.isAlive()) {
                break;
            }
            try {
                Thread.sleep(pollIntervalMilliseconds);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println("\n------------------------");
            System.out.println("Complete Response:");
            System.out.println("------------------------");

            System.out.println(streamer.getCompleteResponse());
        }
    }

    public static void sendQuery(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String prompt) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                OllamaAPI ollamaAPI = new OllamaAPI(host);
                ollamaAPI.setRequestTimeoutSeconds(60);

                OllamaAsyncResultStreamer streamer = ollamaAPI.generateAsync(LLAMA3, prompt, false);
                int pollIntervalMilliseconds = 200;

                while (true) {
                    String tokens = streamer.getStream().poll();
                    if (tokens != null && !tokens.isBlank()) {
                        Platform.runLater(() ->
                                textArea.insertText(textArea.getLength(), tokens)
                        );
                    }

                    if (!streamer.isAlive()) break;

                    Thread.sleep(pollIntervalMilliseconds);
                }
                if (textArea.getAiConnector() != null){
                    textArea.getAiConnector().addPreviousAnswer(streamer.getCompleteResponse());
                }
                return null;
            }
        };

        aiExecutor.submit(task);
    }

    public static void sendFormattedQuery(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String prompt) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                OllamaAPI ollamaAPI = new OllamaAPI(host);
                ollamaAPI.setRequestTimeoutSeconds(60);

                OllamaAsyncResultStreamer streamer = ollamaAPI.generateAsync(LLAMA3, prompt, false);
                ObjectMapper objectMapper = new ObjectMapper();

                StringBuilder currentJson = new StringBuilder();
                int braceCount = 0;
                boolean insideJson = false;

                int pollIntervalMilliseconds = 100;

                while (true) {
                    String token = streamer.getStream().poll();

                    if (token == null) {
                        if (!streamer.isAlive()) break;
                        Thread.sleep(pollIntervalMilliseconds);
                        continue;
                    }

                    for (char c : token.toCharArray()) {
                        if (c == '{') {
                            braceCount++;
                            insideJson = true;
                        }

                        if (insideJson) {
                            currentJson.append(c);
                        }

                        if (c == '}') {
                            braceCount--;
                        }

                        // If we’ve completed a JSON object
                        if (insideJson && braceCount == 0) {
                            String jsonChunk = currentJson.toString().trim();

                            if (jsonChunk.startsWith("{") && jsonChunk.endsWith("}")) {
                                try {
                                    JsonNode node = objectMapper.readTree(jsonChunk);

                                    if (node.isObject() && node.has("type")) {
                                        Platform.runLater(() -> {
                                            try {
                                                System.out.println(node);
                                                new AIHelperUtility(textArea).applyJsonInstruction(node);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                                    } else {
                                        System.err.println("Skipping invalid node: " + node);
                                    }

                                } catch (Exception e) {
                                    System.err.println("Failed to parse JSON: " + jsonChunk);
                                    e.printStackTrace();
                                }
                            }

                            // Reset after successful processing
                            currentJson.setLength(0);
                            insideJson = false;
                        }
                    }

                    if (!streamer.isAlive()) break;
                    Thread.sleep(pollIntervalMilliseconds);
                }

                if (textArea.getAiConnector() != null){
                    textArea.getAiConnector().addPreviousAnswer(streamer.getCompleteResponse());
                }
                return null;
            }
        };

        aiExecutor.submit(task);
    }


    public static String sendQueryAndReceiveResponse(String prompt) {
        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(60);

        OllamaAsyncResultStreamer streamer = ollamaAPI.generateAsync(LLAMA3, prompt, false);
        int pollIntervalMilliseconds = 1000;

        while (true) {
//            String tokens = streamer.getStream().poll();
//
            if (!streamer.isAlive()) {
                break;
            }
            try {
                Thread.sleep(pollIntervalMilliseconds);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        System.out.println(streamer.getCompleteResponse());
        return streamer.getCompleteResponse();
    }
}
