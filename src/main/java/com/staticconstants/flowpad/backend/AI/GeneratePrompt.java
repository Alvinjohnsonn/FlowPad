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
                
                Split sentences into multiple segments only when you need to apply different styles or insert media.
                
                You may split a sentence into multiple JSON objects to apply different styles (e.g. rainbow coloring).
                DO NOT include escape characters such as \\n, \\t, or \\r in the Text segment. Use the line break segment instead!
                Do not output HTML, markdown, or any explanation.
                Do not include comments inside or outside the JSON.
                Do not use color names (like "red" or "orange") — use valid 6-digit hex codes only (#RRGGBB) — DO NOT ADD COMMENTS ABOUT WHAT THE COLOR NAME IS.
               
                Please write the JSON following this format: (Note that each segment have their own set of key value pairs, so do not mix them up!)
                1. Text segment:
                   {
                     "type": "text",
                     "content": "string",
                     "style": {
                       "bold": true | false,  // default value = false
                       "italic": true | false,  // default value = false
                       "underline": true | false,  // default value = false
                       "fontSize": integer (e.g. 14),  // default value = 12
                       "textColor": "#RRGGBB",  // default value = #000000
                       "backgroundColor": "#RRGGBB",  // default value = #FFFFFF
                       "headingLevel": 0-5  // default value = 0 (normal text)
                     },
                     "paragraphStyle": {
                        "alignment": "left" | "center" | "right" | "justify",  // default value = left
                        "lineSpacing": 0-5,  // default value = 0
                        "listType": "none" | "bullet" | "numbered",  // default value = none
                        "listLevel": 0-5  // default value = 0
                     }
                   }
                
                2. Hyperlink segment:
                   {
                     "type": "hyperlink",
                     "text": "clickable text",
                     "url": a working url
                   }
                3. Line break:
                   {
                     "type": "br"
                   }
                
                DO NOT COME UP WITH OTHER TYPES OF THE JSON!!! THERE AREA ONLY "text", "hyperlink", and "br"!!!
                You do not have to insert hyperlinks if it is not specifically asked or is not appropriate, and please include a working url if you do so.
                
                For Text segments:
                Only include "paragraphStyle" for segments that begin a new paragraph or require specific alignment/list behavior. Inline text within a paragraph should omit it.
                You may omit keys which have a default value and you're setting the same value as the default value,
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
                
                Return only the outline. Do not include any preamble or explanation.
                
                Only output JSON, one object per line.
                Return structured content as JSON. Do not include \\n or \\t characters in the text. Each paragraph/line should be a separate JSON object.
                You should ALWAYS include "paragraphStyle" as that indicates a new line.
 
                You may omit keys which have a default value and you're setting the same value as the default value,
                
                Please return a JSON following this criteria:
                
                1.  Text segment
                    {
                     "type": "text",
                     "content": "string",
                     "paragraphStyle": {
                        "alignment": "left" | "center" | "right" | "justify", // default value = left
                        "lineSpacing": 0-5, // default value = 0
                        "listType": "bullet" | "numbered",
                        "listLevel": 0-5 // default value = 0
                     }
                   }
                2.  Line break
                    {
                    "type": "br"
                    }
                
                For better clarity these are examples of what you should return in each line:
                {"type": "text", "content": "First point goes here", "paragraphStyle":{"alignment":"left", "listType": "bullet", "listLevel":1}}
                {"type": "text", "content": "Second point goes here", "paragraphStyle":{"alignment":"left", "listType": "bullet", "listLevel":1}}
                {"type": "text", "content": "Third point goes here", "paragraphStyle":{"alignment":"left", "listType": "bullet", "listLevel":1}}
                
                SPECIAL CASE: If the original text already has numbered list or bullet characters, please remove the number or bullet character because setting
                the listType bullet or numbered will give the appropriate prefix. DO NOT UNDER ANY CIRCUMSTANCES SET THE KEY "listType" to another string other than "bullet" or "numbered!
                
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
