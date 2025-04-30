package com.staticconstants.flowpad.backend.db.notes;

import com.staticconstants.flowpad.backend.db.DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class NoteDAO extends DAO<Note> {

    @Override
    protected Void createTableImpl(Connection connection) throws SQLException {
        return null;
    }

    @Override
    protected Void deleteImpl(Connection connection, UUID id) throws SQLException {
        return null;
    }

    @Override
    protected Boolean insertImpl(Connection connection, Note obj) throws SQLException {
        return null;
    }

    @Override
    protected Void updateImpl(Connection connection, Note obj) throws SQLException {
        return null;
    }

    @Override
    protected Note getByIdImpl(Connection connection, UUID id) throws SQLException {
        return null;
    }

    @Override
    protected List<Note> getAllImpl(Connection connection) throws SQLException {
        return List.of();
    }
}
