package com.staticconstants.flowpad.backend.db;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class DAO<T extends DbRecord> {

    private static final DbHandler dbHandler = DbHandler.getInstance();

    public CompletableFuture<Void> createTable() {
        return dbHandler.dbOperation(this::createTableImpl);
    }


    public CompletableFuture<Void> insert(T obj) {
        return dbHandler.dbOperation(dbConnection -> insertImpl(dbConnection, obj));
    }


    public CompletableFuture<Void> update(T obj) {
        return dbHandler.dbOperation(dbConnection -> updateImpl(dbConnection, obj));
    }


    public CompletableFuture<Void> delete(UUID id)
    {
        return dbHandler.dbOperation(dbConnection -> deleteImpl(dbConnection, id));
    }

    public CompletableFuture<List<T>> getAll()
    {
        return dbHandler.dbOperation(this::getAllImpl);
    }

    public CompletableFuture<T> getById(UUID id)
    {
        return dbHandler.dbOperation(dbConnection -> getByIdImpl(dbConnection, id));
    }


    protected abstract Void deleteImpl(Connection connection, UUID id);
    protected abstract Void updateImpl(Connection connection, T obj);
    protected abstract Void insertImpl(Connection connection, T obj);
    protected abstract Void createTableImpl(Connection connection);
    protected abstract List<T> getAllImpl(Connection connection);
    protected abstract T getByIdImpl(Connection connection, UUID id);
}
