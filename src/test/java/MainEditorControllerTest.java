    import com.staticconstants.flowpad.frontend.MainEditorController;
    import javafx.application.Platform;
    import javafx.scene.control.Button;
    import org.junit.jupiter.api.*;

    import java.util.HashMap;
    import java.util.concurrent.CountDownLatch;

    import static org.junit.jupiter.api.Assertions.*;

    class MainEditorControllerTest {
        private static final HashMap<String, String> STYLE_HASHMAP = new HashMap<String, String>();
        private static final String STYLE_STRING = "-fx-font-weight:bold;-fx-background-color:black;-fx-font-size:12px;";

//        @BeforeAll
//        public static void initJFX() throws InterruptedException {
//            System.setProperty("java.awt.headless", "true");
//            CountDownLatch latch = new CountDownLatch(1);
//            Platform.startup(latch::countDown);
//            latch.await();
//        }

        @BeforeEach
        void setUp() {
            STYLE_HASHMAP.put("-fx-font-weight", "bold");
            STYLE_HASHMAP.put("-fx-background-color", "black");
            STYLE_HASHMAP.put("-fx-font-size", "12px");
        }

        @Test
        void testHashMapStyleToString(){
            String styleInString = MainEditorController.hashMapStyleToString(STYLE_HASHMAP);
    //        Apparently doesn't return the keys and value in order when combined
    //        assertEquals(STYLE_STRING, styleInString);
            for (String style : styleInString.split(";")){
                assertTrue(STYLE_STRING.contains(style));
            }
        }


        @Test void testParseStyle(){
            HashMap<String, String> result = MainEditorController.parseStyle(STYLE_STRING);
            for (String key : STYLE_HASHMAP.keySet()){
                assertEquals(STYLE_HASHMAP.get(key), result.get(key));
            }
        }

        @Test void testGetStyleValue()
        {
            String value = MainEditorController.getStyleValue(STYLE_STRING, "-fx-font-weight");
            assertEquals("bold", value);
        }
    }