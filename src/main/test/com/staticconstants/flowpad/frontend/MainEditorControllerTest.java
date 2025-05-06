package com.staticconstants.flowpad.frontend;

import javafx.application.Platform;
import javafx.scene.control.Button;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class MainEditorControllerTest {
    private static final HashMap<String, String> STYLE_HASHMAP = new HashMap<String, String>();
    private static final String STYLE_STRING = "-fx-font-weight:bold;-fx-background-color:black;-fx-font-size:12px;";

    @BeforeAll
    public static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown); // Only once per JVM
        latch.await();
    }

    @BeforeEach
    void setUp() {
        STYLE_HASHMAP.put("-fx-font-weight", "bold");
        STYLE_HASHMAP.put("-fx-background-color", "black");
        STYLE_HASHMAP.put("-fx-font-size", "12px");
    }

    @Test
    void testHashMapStyleToString() {
        String styleInString = MainEditorController.hashMapStyleToString(STYLE_HASHMAP);
//        Apparently doesn't return the keys and value in order when combined
//        assertEquals(STYLE_STRING, styleInString);
        for (String style : styleInString.split(";")) {
            assertTrue(STYLE_STRING.contains(style));
        }
    }

    @Test
    void testSetActiveButton() {
        Button active = new Button();
        Button inactive = new Button();
        inactive.getStyleClass().add("selected");

        MainEditorController.setActiveButton(active, inactive);
        assertTrue(active.getStyleClass().contains("selected") && !inactive.getStyleClass().contains("selected"));

        MainEditorController.setActiveButton(active, inactive);
        int numOfClass = 0;
        for (String style : active.getStyleClass()) {
            if (style.equals("selected")) numOfClass += 1;
        }
        assertEquals(1, numOfClass);

    }

    @Test
    void testSetSelectedButton() {
        Button btn = new Button();
        MainEditorController.setSelectedButton(btn, true);
        assertTrue(btn.getStyleClass().contains("selected"));

        MainEditorController.setSelectedButton(btn, true);
        assertTrue(btn.getStyleClass().contains("selected"));

        MainEditorController.setSelectedButton(btn, false);
        assertFalse(btn.getStyleClass().contains("selected"));

    }

    @Test
    void testToggleSelectedButton() {
        Button btn = new Button();
        MainEditorController.toggleSelectedButton(btn);
        assertTrue(btn.getStyleClass().contains("selected"));

        MainEditorController.toggleSelectedButton(btn);
        assertFalse(btn.getStyleClass().contains("selected"));
    }


}

