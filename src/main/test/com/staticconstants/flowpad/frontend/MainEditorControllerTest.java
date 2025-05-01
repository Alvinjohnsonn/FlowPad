package com.staticconstants.flowpad.frontend;

import javafx.scene.control.Button;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

class MainEditorControllerTest {
    private static final HashMap<String, String> STYLE_HASHMAP = new HashMap<String, String>();
    private static final String STYLE_STRING = "-fx-font-weight:bold;-fx-background-color:black;-fx-font-size:12px;";
    @BeforeEach
    void setUp() {
        STYLE_HASHMAP.put("-fx-font-weight", "bold");
        STYLE_HASHMAP.put("-fx-background-color", "black");
        STYLE_HASHMAP.put("-fx-font-size", "12px");

        MainEditorController controller = new MainEditorController();
    }

    @Test
    void testHashMapStyleToString(){
        String styleInString = MainEditorController.hashMapStyleToString(STYLE_HASHMAP);
        assertEquals(STYLE_STRING, styleInString);
    }

    @Test
    void testSetActiveButton(){
        Button active = new Button();
        Button inactive = new Button();
        inactive.getStyleClass().add("selected");

        MainEditorController.setActiveButton(active, inactive);
        assertTrue(active.getStyleClass().contains("selected") && !inactive.getStyleClass().contains("selected"));

        MainEditorController.setActiveButton(active, inactive);
        int numOfClass = 0;
        for (String style : active.getStyleClass()){
            if (style.equals("selected")) numOfClass += 1;
        }
        assertEquals(1, numOfClass);

    }

    @Test
    void testSetSelectedButton(){
        Button btn = new Button();
        MainEditorController.setSelectedButton(btn, true);
        assertTrue(btn.getStyleClass().contains("selected"));

        MainEditorController.setSelectedButton(btn, true);
        assertTrue(btn.getStyleClass().contains("selected"));

        MainEditorController.setSelectedButton(btn, false);
        assertFalse(btn.getStyleClass().contains("selected"));

    }

    @Test
    void testToggleSelectedButton(){
        Button btn = new Button();
        MainEditorController.toggleSelectedButton(btn);
        assertTrue(btn.getStyleClass().contains("selected"));

        MainEditorController.toggleSelectedButton(btn);
        assertFalse(btn.getStyleClass().contains("selected"));
    }


    @Test
    void testExtractFontSize(){
        // not sure if this one is suitable for unit testing
    }

    @Test
    void testAddStyle(){

    }

    @Test void testParseStyle(){

    }

    @Test void testGetStyleValue()
    {

    }
}