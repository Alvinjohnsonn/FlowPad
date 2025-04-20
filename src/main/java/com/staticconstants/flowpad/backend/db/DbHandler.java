package com.staticconstants.flowpad.backend.db;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public final class DbHandler {

    private static DbHandler INSTANCE = null;
    private DbHandlerThread runner;
    private LinkedBlockingQueue<DbTask<?>> opQueue;
    private Connection dbConnection;

    private DbHandler()
    {
        if (INSTANCE != null) {
            throw new IllegalStateException("DbHandler.class is singleton.  Multiple instantiations attempted");
        }
        opQueue = new LinkedBlockingQueue<>();
        runner = new DbHandlerThread();
        runner.start();
    }

    public static DbHandler getInstance()
    {
        if (INSTANCE == null) {
            synchronized (DbHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DbHandler();
                }
            }
        }
        return INSTANCE;
    }

    public <T> CompletableFuture<T> dbOperation(DbOperation<T> callback) {
        CompletableFuture<T> f = new CompletableFuture<>();
        try {
            opQueue.put(new DbTask<>(callback, f));
        } catch (InterruptedException ex) {
            f.completeExceptionally(ex);
        }
        return f;
    }

    private static class DbTask<T> {
        final DbOperation<T> operation;
        final CompletableFuture<T> future;

        DbTask(DbOperation<T> operation, CompletableFuture<T> future) {
            this.operation = operation;
            this.future = future;
        }
    }

    private final class DbHandlerThread extends Thread {

        private boolean running;

        @Override
        public void start() {
            try {
                dbConnection = DriverManager.getConnection("jdbc:sqlite:database.db");
            } catch (SQLException sqlEx) {
                System.err.println(sqlEx);
                throw new RuntimeException(sqlEx);
            }
            super.start();
            running = true;
        }

        @Override
        public void run() {
            while (running)
            {
                if (!isDbConnected()) continue;

                try {
                    DbTask<?> task = opQueue.take();
                    handleTask(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        private <T> void handleTask(DbTask<T> task) {
            try {
                T result = task.operation.operation(dbConnection);
                task.future.complete(result);
            } catch (SQLException ex) {
                task.future.completeExceptionally(ex);
            }
        }

        private boolean isDbConnected()
        {
            try {
                return dbConnection.isValid(0) || !dbConnection.isClosed();
            } catch (SQLException ex) {
                return false;
            }
        }
    }
}
