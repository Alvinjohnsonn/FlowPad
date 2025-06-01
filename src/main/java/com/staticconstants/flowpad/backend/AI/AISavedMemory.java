package com.staticconstants.flowpad.backend.AI;

import java.util.ArrayList;
import java.util.List;

public class AISavedMemory {
    List<String> previousPrompts;
    List<String> previousResponses;
    boolean advancedResponse;

    public boolean isAdvancedResponse() {
        return advancedResponse;
    }

    public void setInitialPrompt(String initPrompt){
        if (previousPrompts != null) {
            previousPrompts.addFirst(initPrompt);
        }
    }
    public void setAdvancedResponse(boolean advancedResponse) {
        this.advancedResponse = advancedResponse;
    }

    public AISavedMemory(String initialPrompt){
        this.previousPrompts = new ArrayList<String>();
        this.previousResponses = new ArrayList<String>();
        addPrompt(initialPrompt);
    }

    public void addPrompt(String prompt){
        previousPrompts.add(prompt);
    }

    public void addAnswer(String answer){
        previousResponses.add(answer);
    }

    public String constructNewPrompt(String prompt){
        if (previousResponses.isEmpty()) return "null";
        previousPrompts.add(prompt);
        StringBuilder promptToSend = new StringBuilder();
        promptToSend.append(previousPrompts.getFirst()).append("\n\n").append("Since you didn't save your previous response, I'll provide the help of showing you what you responded with previously:").append("\n\n");;
//        for (int i = 1; i < previousPrompts.size(); i++){
//            if (i-1<previousResponses.size())promptToSend.append("Your Response ").append(i).append(": \n\n\"").append(previousResponses.get(i-1)).append("\"\n\n");
//            promptToSend.append("Could you revise your answer to consider the following request:").append("\n\n\"").append(previousPrompts.get(i)).append("\"\n\n");
//        }  // Ended up being too long of a prompt if additional requests are piled up
        promptToSend.append("Your Previous Response ").append(": \n\n\"").append(previousResponses.getLast()).append("\"\n\n");
        promptToSend.append("Could you revise your answer to consider the following request:").append("\n\n\"").append(prompt).append("\"\n\n");

        System.out.println(promptToSend);
        return promptToSend.toString();
    }
}
