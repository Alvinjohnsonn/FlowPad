package com.staticconstants.flowpad.backend.AI;

import com.staticconstants.flowpad.frontend.textarea.CustomStyledArea;
import com.staticconstants.flowpad.frontend.textarea.ParStyle;
import com.staticconstants.flowpad.frontend.textarea.RichSegment;
import com.staticconstants.flowpad.frontend.textarea.TextStyle;

public class GeneratePrompt {
    public static String send(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, Prompt type, String text){
        textArea.deleteText(0, textArea.getLength());
        textArea.setStyle(0, textArea.getLength(), TextStyle.EMPTY.setFontSize(14));
        return switch(type){
            case GENERATE_SUMMARY -> summarize(textArea, text);
            case AI_HIGHLIGHT -> highlight(textArea, text);
            case AUTO_CORRECT -> autoCorrect(textArea, text);
            case REFACTOR_CONTENT -> refactorContent(textArea, text);
            case GENERATE_OUTLINE -> generateOutline(textArea, text);
            case FORMAT_WRITING -> formatWriting(textArea, text);
            case SHORT_TO_FULL -> shortToFull(textArea, text);
            case CUSTOM_PROMPT -> customPrompt(textArea, text, "");
        };
    }


    private static String shortToFull(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to find abbreviated word and convert them to the full word. Leave other complete words in tact.
                Return only the full text, do not include any preamble or explanation.
                
                Here is the text:
                
                """ + text;
        AsyncQuery.sendQuery(textArea, prompt);
        return prompt;
    }

    private static String JSONFormat(){
        return """
                You are generating structured content for a styled text editor. Output only JSON — one JSON object per line, with no commentary or extra text.
                
                Each object represents a segment and may be a single character, word, phrase, or full paragraph.
                
                You may split a sentence into multiple JSON objects to apply different styles (e.g. rainbow coloring).
                Do not include escape characters such as \\n, \\t, or \\r in the text.
                Do not output HTML, markdown, or any explanation.
                Do not include comments inside or outside the JSON.
                Do not use color names (like "red" or "orange") — use valid 6-digit hex codes only (#RRGGBB).
                You may not include "paragraphStyle" when you're inserting inline characters or words.
                You may omit some keys if the value is false, because false is the default value.
                
                Each segment can be of one of the following types:
                1. Text segment:
                   {
                     "type": "text",   // if want to insert heading have this set to text and change the heading level below!
                     "content": " . . . ", // plain string of the user's original text
                     "style": {
                       "bold": true | false,
                       "italic": true | false,
                       "underline": true | false,
                       "fontSize": integer (e.g. 14),
                       "textColor": "#RRGGBB", // THIS CAN ONLY CONTAIN 1 COLOR VALUE WHICH APPLY TO EACH SEGMENT (EACH JSON OBJECT)
                       "backgroundColor": "#RRGGBB", // THIS CAN ONLY CONTAIN 1 COLOR VALUE WHICH APPLY TO EACH SEGMENT (EACH JSON OBJECT)
                       "headingLevel": 0 to 5  // 0 means normal text
                     },
                     "paragraphStyle": {
                        "alignment": "left" | "center" | "right" | "justify",
                        "lineSpacing": 0 to 5, // 0 = no line spacing
                        "listType": "none" | "bullet" | "numbered",
                        "listLevel": 0 to 5 // level of indentation, with 0 being no indent
                     }
                   }
                
                2. Image segment:
                   {
                     "type": "image",
                     "url": a working url // Please insert a valid image URL
                   }
                
                3. Hyperlink segment:
                   {
                     "type": "hyperlink",
                     "text": "clickable text",
                     "url": a working url // Please insert a valid URL
                   }
                
                ## SPECIAL INSTRUCTION EXAMPLE — RAINBOW TEXT
                
                If asked to style a sentence like "Hello" with rainbow colors (left to right), output:
                {"type":"text","content":"H","style":{"textColor":"#FF0000",...}}
                {"type":"text","content":"e","style":{"textColor":"#FF7F00",...}}
                {"type":"text","content":"l","style":{"textColor":"#FFFF00",...}}
                {"type":"text","content":"l","style":{"textColor":"#00FF00",...}}
                {"type":"text","content":"o","style":{"textColor":"#0000FF",...}}
                Note: If this output is "Hello" (inline), you shouldn't include "paragraphStyle"
                
                ## SPECIAL INSTRUCTION EXAMPLE — INSERTING SPACE BETWEEN PARAGRAPH
                
                {"type":"text","content":"Paragraph 1","style":{...}}
                {"type":"text","content":"\\n","style":{...}}
                {"type":"text","content":"Paragraph 2","style":{...}}
                Note: This will insert "Paragraph 2" below "Paragraph 1". If you want to add a space between the two paragraphs, insert a duplicate of the second JSON.
              
                DO NOT ADD COMMENTS INSIDE THE JSON!!!!!!!!!!!!!!!!
                You are allowed to split any content into multiple JSON lines if needed to change styling.
                Only valid structured JSON objects should be returned — one per line, no additional text.
                """;
    }
    private static String formatWriting(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text) {
        String prompt = """ 
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to format the user's writing and return it as a structured JSON stream. Each line must be a complete and valid JSON object, describing a segment to insert into the editor. Do not wrap the segments in a list or array. Output one segment per line, no other commentary or text.
                
                """ +JSONFormat()+ """
                
                Do not change any of the user's original wording.
                
                Here is the text to format:
                
                """ + text;
        AsyncQuery.sendFormattedQuery(textArea, prompt);
        return prompt;
    }

    private static String generateOutline(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to analyze the given text and generate a clear, hierarchical outline capturing the main ideas and supporting points. 
                You MUST use bullet points or numbered lists, and indent subpoints under their parent topics. 
                Do not rewrite or summarize the content—just structure it into an outline.
                
                Return only the outline. Do not include any preamble or explanation.
                
                Do not change any of the user's original wording.
                Only output JSON, one object per line.
                Return structured content as JSON. Do not include \\\\n or \\\\t characters in the text. Each paragraph/line should be a separate JSON object.
                You should always include "paragraphStyle" as that indicates a new line.
                
                Please return a JSON following this criteria:
                
                1.   {
                     "type": "text",
                     "content": "<insert content here>",
                     "paragraphStyle": {
                        "alignment": "left" | "center" | "right" | "justify",
                        "lineSpacing": 0 to 5, // 0 = no line spacing
                        "listType": "bullet" | "numbered",
                        "listLevel": 1 to 5 // level of indentation
                     }
                   }
                
                For better clarity these are examples of what you should return in each line:
                {"type": "text", "content": "First point goes here", "paragraphStyle":{"alignment":"left", "listType": "bullet", "listLevel":1}}
                {"type": "text", "content": "Second point goes here", "paragraphStyle":{"alignment":"left", "listType": "bullet", "listLevel":1}}
                {"type": "text", "content": "Third point goes here", "paragraphStyle":{"alignment":"left", "listType": "bullet", "listLevel":1}}
                
                
                Here is the text:
                
                """ + text;
        AsyncQuery.sendFormattedQuery(textArea, prompt);
        return prompt;
    }

    private static String refactorContent(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is refine the text by removing and adding any word/s to improve the quality of the text.
                Return only the full text, do not include any preamble or explanation.
                
                Here is the text:
                
                """ + text;
        AsyncQuery.sendQuery(textArea, prompt);
        return prompt;
    }

    private static String autoCorrect(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to find grammatical and spelling errors and fix them from a text, essentially auto correcting them.
                Return only the corrected text, do not include any preamble or explanation.
                
                Here is the text:
                
                """ + text;
        AsyncQuery.sendQuery(textArea, prompt);
        return prompt;
    }

    private static String highlight(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text) {
        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                Your task is to find abbreviated word and convert them to the full word. Leave other complete words in tact.
                Return only the given text (with some already highlighted), do not include any preamble or explanation.
                
                Do not change any of the user's original wording.
                Only output JSON, one object per line.
                Return structured content as JSON.
                If you need to insert a new line/paragraph, please return a JSON with the content only containing new line char (backslash n), preferably include 2 chars of new line to have a space between paragraphs.
                You may use different color of highlight, but make sure it is contrast to a light theme and a black colored text.
                Do not include HTML tags!!! Each part of the text should be in each separate JSON
                
                Please return a JSON following this criteria:
                
                1. {
                     "type": "text",
                     "content": "plain string of the user's original text",
                     style": {
                       "backgroundColor": "#RRGGBB", // the highlight color, set "transparent" if returning text with no highlight
                     }
                   }
                
                DO NOT INCLUDE COMMENTS INSIDE THE JSON!!!
               
                Here is the text:
                
                """ + text;
        AsyncQuery.sendFormattedQuery(textArea, prompt);
        return prompt;
    }


    private static String summarize(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text){

        String prompt = """
    You are an AI assistant inside a rich text note-taking application.
   
    Your task is to to summarize the following for a student who needs to quickly understand the key points.
    Keep it concise and focused on the main ideas.
    Return only the summary, do not include any preamble or explanation.
 
    Text to summarize:
    
    """ + text;

        AsyncQuery.sendQuery(textArea, prompt);
        return prompt;
    }

    public static void sendOptionalRequest(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String request, AISavedMemory memory, boolean needFormatting){
        if (!memory.previousPrompts.isEmpty()){
            textArea.deleteText(0, textArea.getLength());
            textArea.setStyle(0, textArea.getLength(), TextStyle.EMPTY.setFontSize(14));

            if (needFormatting) AsyncQuery.sendFormattedQuery(textArea, memory.constructNewPrompt(request));
            else AsyncQuery.sendQuery(textArea, memory.constructNewPrompt(request));
        }
    }

    private static boolean needAdvanced(String prompt){
        String prefix = """
                Below is a custom prompt written by the user. Determine whether ths prompt requires advanced
                features (formatting styles, changing colors, handling image/hyperlink/paragraph, etc) or if it merely needs to return a regular text
                Return "true" if it does need those advanced features and "false" otherwise
                Only return "true" or "false"
                The prompt:
                
                """ + prompt;

        return (AsyncQuery.sendQueryAndReceiveResponse(prompt).equals("true"));
    }


    public static String customPrompt(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, String text, String customPrompt) {
        boolean needAdvanced = needAdvanced(customPrompt);
        if (textArea.getAiConnector() != null ) textArea.getAiConnector().setAdvancedResponse(needAdvanced);

        String prompt = """
                You are an AI assistant inside a rich text note-taking application.
                
                """+(needAdvanced?JSONFormat():"")+"""
                
                Your task is to:""" + customPrompt + """
                
                Here is the text:
                
                """ + text;
        if (needAdvanced) AsyncQuery.sendFormattedQuery(textArea, prompt);
        else AsyncQuery.sendQuery(textArea, prompt);

        return prompt;
    }
}
