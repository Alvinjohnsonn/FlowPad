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
        previousPrompts.add(prompt);
        StringBuilder promptToSend = new StringBuilder();
        for (int i = 0; i < previousPrompts.size(); i++){
            if (i == 0) promptToSend.append(previousPrompts.get(i)).append("\n\n");

            for (int j = 0; j < previousResponses.size(); j++){
                if (j==0) promptToSend.append("Since you didn't save your previous responses, I'll provide the help of showing you what you responded with previously:").append("\n\n");
                promptToSend.append("Your Response ").append(j+1).append(": \n\n\"").append(previousResponses.get(j)).append("\"\n\n");
            }

            if (i > 0) promptToSend.append("Could you revise your answer to consider the following request:").append("\n\n\"").append(prompt).append("\"\n\n");

        }

        System.out.println(promptToSend);
        return promptToSend.toString();
    }
}
