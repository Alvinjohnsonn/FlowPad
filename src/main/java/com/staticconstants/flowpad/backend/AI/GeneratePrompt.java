package com.staticconstants.flowpad.backend.AI;

import com.staticconstants.flowpad.frontend.textarea.CustomStyledArea;
import com.staticconstants.flowpad.frontend.textarea.ParStyle;
import com.staticconstants.flowpad.frontend.textarea.RichSegment;
import com.staticconstants.flowpad.frontend.textarea.TextStyle;

public class GeneratePrompt {
    public static void send(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, Prompt type, String text, String optionalRequest){
        textArea.deleteText(0, textArea.getLength());
        textArea.setStyle(0, textArea.getLength(), TextStyle.EMPTY.setFontSize(14));
        switch(type){
            case GENERATE_SUMMARY -> summarize(textArea, text, optionalRequest);
            case AI_HIGHLIGHT -> highlight(textArea, text, optionalRequest);
            case AUTO_CORRECT -> autoCorrect(textArea, text, optionalRequest);
            case REFACTOR_CONTENT -> refactorContent(textArea, text, optionalRequest);
            case GENERATE_OUTLINE -> generateOutline(textArea, text, optionalRequest);
            case FORMAT_WRITING -> formatWriting(textArea, text, optionalRequest);
            case SHORT_TO_FULL -> shortToFull(textArea, text, optionalRequest);
            case CUSTOM_PROMPT -> customPrompt(textArea, text, optionalRequest, "");
        }
    }

    private static void customPrompt(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text, String optionalRequest, String customPrompt) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to:""" + customPrompt + """
                
                This is less important, but please consider the following request: (ignore if it's empty)
                                \\""\"" + optionalRequest + ""\"
                                "
               
                Here is the text:
                
                """ + text;
        AsyncQuery.sendQuery(textArea, prompt);
//        TODO: Make sure this is a flexible as custom request could ask for formatting
    }

    private static void shortToFull(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text, String optionalRequest) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to find abbreviated word and convert them to the full word. Leave other complete words in tact.
                
                This is less important, but please consider the following request: (ignore if it's empty)
                                \\""\"" + optionalRequest + ""\"
                                "
               
                Here is the text:
                
                """ + text;
        AsyncQuery.sendQuery(textArea, prompt);
    }

    private static void formatWriting(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text, String optionalRequest) {
        String prompt = """ 
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to format the user's writing and return it as a structured JSON stream. Each line must be a **complete and valid JSON object**, describing a segment to insert into the editor. Do **not** wrap the segments in a list or array. Output **one segment per line**, no other commentary or text.
                
                Each segment can be of one of the following types:
                
                1. **Text segment**:
                   {
                     "type": "text",   // if want to insert heading have this set to text and change the heading level below!
                     "content": "plain string of the user's original text",
                     "style": {
                       "bold": true | false,
                       "italic": true | false,
                       "underline": true | false,
                       "fontSize": integer (e.g. 14),
                       "textColor": "#RRGGBB",
                       "backgroundColor": "#RRGGBB",
                       "headingLevel": 0 to 5  // 0 means normal text
                     },
                     "paragraphStyle": {
                        "alignment": "left" | "center" | "right" | "justify",
                        "lineSpacing": 0 to 5, // 0 = no line spacing
                        "listType": "none" | "bullet" | "numbered",
                        "listLevel": 0 to 5 // level of indentation, with 0 being no indent
                     }
                   }
                
                2. **Image segment**:
                   {
                     "type": "image",
                     "url": "https://example.com/image.png"
                   }
                
                3. **Hyperlink segment**:
                   {
                     "type": "hyperlink",
                     "text": "clickable text",
                     "url": a working url (e.g., https://google.com)
                   }
                
                Do not add any commentary, markdown, or additional explanation.
                Do not change any of the user's original wording.
                Only output JSON, one object per line.
                Return structured content as JSON. Do not include \\\\n or \\\\t characters in the text. Each paragraph or heading should be a separate JSON object.
                You may format a span of words, you can do this without including "paragraphStyle"
                
                This is less important, but please consider the following request: (ignore if it's empty)
                \"""" + optionalRequest + """
                "
                
                Here is the text to format:
                
                """ + text;
        AsyncQuery.sendFormattedQuery(textArea, prompt);
    }

    private static void generateOutline(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text, String optionalRequest) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to find abbreviated word and convert them to the full word. Leave other complete words in tact.
                
                This is less important, but please consider the following request: (ignore if it's empty)
                                \\""\"" + optionalRequest + ""\"
                                "
               
                Here is the text:
                
                """ + text;
        AsyncQuery.sendQuery(textArea, prompt);
    }

    private static void refactorContent(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text, String optionalRequest) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                
                Your task is refine the text by removing and adding any word/s to improve the quality of the text.
                
                This is less important, but please consider the following request: (ignore if it's empty)
                \\""\"" + optionalRequest + ""\"
                "
               
                Here is the text:
                
                """ + text;
        AsyncQuery.sendQuery(textArea, prompt);
    }

    private static void autoCorrect(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text, String optionalRequest) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to find grammatical errors and fix them from a text, essentially auto correcting them.
                
                This is less important, but please consider the following request: (ignore if it's empty)
                                \\""\"" + optionalRequest + ""\"
                                "
               
                Here is the text:
                
                """ + text;
        AsyncQuery.sendQuery(textArea, prompt);
    }

    private static void highlight(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text, String optionalRequest) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to find abbreviated word and convert them to the full word. Leave other complete words in tact.
                
                This is less important, but please consider the following request: (ignore if it's empty)
                                \\""\"" + optionalRequest + ""\"
                                "
               
                Here is the text:
                
                """ + text;
        AsyncQuery.sendFormattedQuery(textArea, prompt);
    }


    private static void summarize(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text, String optionalRequest){

        String prompt = """
    You are an AI assistant inside a rich text note-taking application.
   
    Your task is to to summarize the following for a student who needs to quickly understand the key points.
    Keep it concise and focused on the main ideas. Only return the summary and omit any additional commentary or explanation.
   """ + (optionalRequest.isEmpty()?"":" Additional Request: " + optionalRequest) + """
 
    
    Text to summarize:
    
    """ + text;

        AsyncQuery.sendQuery(textArea, prompt);
    }
}
