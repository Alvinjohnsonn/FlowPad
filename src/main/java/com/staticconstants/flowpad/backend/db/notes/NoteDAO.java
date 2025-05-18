package com.staticconstants.flowpad.backend.db.notes;

import com.staticconstants.flowpad.backend.LoggedInUser;
import com.staticconstants.flowpad.backend.db.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class NoteDAO extends DAO<Note> {

    @Override
    protected Void createTableImpl(Connection connection) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS notes (
                id VARCHAR(36) PRIMARY KEY,
                filename TEXT NOT NULL,
                serialized_text BYTEA NOT NULL,
                folders TEXT,
                created_time INTEGER NOT NULL,
                last_modified_time INTEGER NOT NULL,
                user_id VARCHAR(36) NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        return null;
    }

    @Override
    protected Boolean insertImpl(Connection connection, Note note) throws SQLException {
        String sql = """
            INSERT INTO notes 
                (id, filename, serialized_text, folders, created_time, last_modified_time, user_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, note.getId().toString());
            ps.setString(2, note.filename);
            ps.setBytes(3, note.serializedText);
            ps.setString(4, String.join(",", note.folders));
            ps.setLong(5, note.createdTime);
            ps.setLong(6, note.lastModifiedTime);
            ps.setString(7, LoggedInUser.user.getId().toString());
            ps.executeUpdate();
            return true;
        }
    }

    @Override
    protected Void updateImpl(Connection connection, Note note) throws SQLException {
        String sql = "UPDATE notes SET filename = ?, serialized_text = ?, folders = ?, last_modified_time = ? WHERE id = ?";
        long time = System.currentTimeMillis();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, note.filename);
            ps.setBytes(2, note.serializedText);
            ps.setString(3, String.join(",", note.folders));
            ps.setLong(4, time);
            ps.setString(5, note.getId().toString());
            ps.executeUpdate();
        }
        note.lastModifiedTime = time;
        return null;
    }

    @Override
    protected Void deleteImpl(Connection connection, UUID id) throws SQLException {
        String sql = "DELETE FROM notes WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        }
        return null;
    }

    @Override
    protected Note getByIdImpl(Connection connection, UUID id) throws SQLException {
        String sql = "SELECT * FROM notes WHERE id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.setString(2, LoggedInUser.user.getId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractNoteFromResultSet(rs);
                }
            }
        }
        return null;
    }


    @Override
    protected List<Note> getAllImpl(Connection connection) throws SQLException {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, LoggedInUser.user.getId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notes.add(extractNoteFromResultSet(rs));
                }
            }
        }
        return notes;
    }

    private Note extractNoteFromResultSet(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String filename = rs.getString("filename");
        byte[] serializedText = rs.getBytes("serialized_text");
        String foldersStr = rs.getString("folders");
        long createdTime = rs.getLong("created_time");
        long modTime = rs.getLong("last_modified_time");
        String[] folders = foldersStr != null ? foldersStr.split(",") : new String[0];
        return Note.fromExisting(id, false, createdTime, modTime, filename, serializedText, folders);
    }
}
