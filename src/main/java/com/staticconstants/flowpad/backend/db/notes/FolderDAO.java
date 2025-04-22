package com.staticconstants.flowpad.backend.db.notes;

import com.staticconstants.flowpad.backend.db.DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class FolderDAO extends DAO<Folder> {
    @Override
    protected Void createTableImpl(Connection connection) throws SQLException {
        return null;
    }

    @Override
    protected Void deleteImpl(Connection connection, UUID id) throws SQLException {
        return null;
    }

    @Override
    protected Void insertImpl(Connection connection, Folder obj) throws SQLException {
        return null;
    }

    @Override
    protected Void updateImpl(Connection connection, Folder obj) throws SQLException {
        return null;
    }

    @Override
    protected Folder getByIdImpl(Connection connection, UUID id) throws SQLException {
        return null;
    }

    @Override
    protected List<Folder> getAllImpl(Connection connection) throws SQLException {
        return List.of();
    }
}
