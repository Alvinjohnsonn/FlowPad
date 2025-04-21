package com.staticconstants.flowpad.backend.db.users;

import com.staticconstants.flowpad.backend.db.DAO;
import com.staticconstants.flowpad.backend.security.HashedPassword;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO extends DAO<User> {


    @Override
    protected Void createTableImpl(Connection connection) throws SQLException {
        Statement createUsers = connection.createStatement();
        createUsers.execute(
        "CREATE TABLE IF NOT EXISTS Users ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "firstName VARCHAR(30) NOT NULL, "
                + "lastName VARCHAR(30) NOT NULL, "
                + "username VARCHAR(30) NOT NULL UNIQUE, "
                + "hashedPassword VARCHAR(255) NOT NULL"
                + "encodedSalt VARCHAR(24) NOT NULL"
                + ")"
        );
        return null;
    }

    @Override
    protected Void deleteImpl(Connection connection, UUID id) throws SQLException {
        PreparedStatement deleteUser = connection.prepareStatement(
                "DELETE FROM Users WHERE id = ?"
        );
        deleteUser.setString(1, id.toString()) ;
        deleteUser.execute();
        return null;
    }

    @Override
    protected Void insertImpl(Connection connection, User obj) throws SQLException {
        PreparedStatement insertUser = connection.prepareStatement(
          "INSERT INTO Users (id, firstName, lastName, username, hashedPassword) VALUES (?,?,?,?,?)"
        );
        insertUser.setString(1, obj.getId().toString());
        insertUser.setString(2, obj.getFirstName());
        insertUser.setString(3, obj.getLastName());
        insertUser.setString(4, obj.getUsername());
        insertUser.setString(5, obj.getHashedPassword().hashBase64);
        insertUser.setString(6, obj.getHashedPassword().saltBase64);
        insertUser.execute();
        return null;
    }

    @Override
    protected Void updateImpl(Connection connection, User obj) throws SQLException {
        PreparedStatement updateUser = connection.prepareStatement(
                "UPDATE User SET id = ?, firstName = ?, lastName = ?, username = ?, hashedPassword = ?, encodedSalt = ?"
        );
        updateUser.setString(1, obj.getId().toString());
        updateUser.setString(2, obj.getFirstName());
        updateUser.setString(3, obj.getLastName());
        updateUser.setString(4, obj.getUsername());
        updateUser.setString(5, obj.getHashedPassword().hashBase64);
        updateUser.setString(6, obj.getHashedPassword().saltBase64);
        updateUser.execute();
        return null;
    }

    @Override
    protected User getByIdImpl(Connection connection, UUID id) throws SQLException {
        PreparedStatement getUser = connection.prepareStatement(
          "SELECT * FROM Users WHERE id = ?"
        );
        getUser.setString(1, id.toString());
        ResultSet rs = getUser.executeQuery();
        if (rs.next()) {
            return User.fromExisting(
                    UUID.fromString(rs.getString(1)),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    new HashedPassword(
                            rs.getString(5),
                            rs.getString(6)
                    )
            );
        }
        return null;
    }

    @Override
    protected List<User> getAllImpl(Connection connection) throws SQLException {
        List<User> users = new ArrayList<>();
        Statement getAll = connection.createStatement();
        ResultSet rs = getAll.executeQuery(
            "SELECT * FROM Users"
        );
        while (rs.next())
        {
            User user = User.fromExisting(
                    UUID.fromString(rs.getString(1)),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    new HashedPassword(
                            rs.getString(5),
                            rs.getString(6)
                    )
            );
            users.add(user);
        }
        return users;
    }
}
