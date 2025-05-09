package com.staticconstants.flowpad.backend.db;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DbHandlerTest {

    private DbHandler dbHandler;

    @BeforeAll
    public void setup() {
        dbHandler = DbHandler.getInstance();
    }

    @Test
    public void testSingletonInstance() {
        DbHandler anotherInstance = DbHandler.getInstance();
        assertSame(dbHandler, anotherInstance, "Instances should be the same (singleton)");
    }

//    @Test
//    public void testGetConnectionNotNull() {
//        Connection conn = dbHandler.getConnection();
//        assertNotNull(conn, "Database connection should not be null");
//        try {
//            assertFalse(conn.isClosed(), "Database connection should be open");
//        } catch (SQLException e) {
//            fail("SQLException occurred when checking connection: " + e.getMessage());
//        }
//    }

    @Test
    public void testSuccessfulDbOperation() throws Exception {
        String tableName = "test_table";
        dbHandler.dbOperation(conn -> {
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS " + tableName + " (id INTEGER)");
            return null;
        }).get(); // ensure completion

        CompletableFuture<Integer> future = dbHandler.dbOperation(conn -> {
            conn.createStatement().execute("INSERT INTO " + tableName + " (id) VALUES (42)");
            return 42;
        });

        Integer result = future.get();
        assertEquals(42, result);
    }

    @Test
    public void testFailedDbOperation() {
        CompletableFuture<Void> future = dbHandler.dbOperation(conn -> {
            // Invalid SQL to trigger SQLException
            conn.createStatement().execute("INVALID SQL");
            return null;
        });

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof SQLException, "Cause should be SQLException");
    }

    @Test
    public void testMultipleOperationsQueued() throws Exception {
        String table = "queue_test";
        dbHandler.dbOperation(conn -> {
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS " + table + " (val INTEGER)");
            return null;
        }).get();

        CompletableFuture<Integer> insert1 = dbHandler.dbOperation(conn -> {
            conn.createStatement().execute("INSERT INTO " + table + " (val) VALUES (1)");
            return 1;
        });

        CompletableFuture<Integer> insert2 = dbHandler.dbOperation(conn -> {
            conn.createStatement().execute("INSERT INTO " + table + " (val) VALUES (2)");
            return 2;
        });

        assertEquals(1, insert1.get());
        assertEquals(2, insert2.get());
    }

    @Test
    public void testCompletableFutureIsCompletedOnFailure() {
        CompletableFuture<String> future = dbHandler.dbOperation(conn -> {
            throw new SQLException("Simulated failure");
        });

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals("Simulated failure", exception.getCause().getMessage());
    }
}
