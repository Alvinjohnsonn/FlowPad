package com.staticconstants.flowpad.backend.db.users;

import com.staticconstants.flowpad.backend.db.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

public class UserDAO extends DAO<User> {

    @Override
    protected Void deleteImpl(Connection connection, UUID id) {

        PreparedStatement ps = connection.prepareStatement("DELETE ");
        ps.execute();
        return null;
    }

    @Override
    protected Void updateImpl(Connection connection, User obj) {
        return null;
    }

    @Override
    protected Void insertImpl(Connection connection, User obj) {
        PreparedStatement ps = connection.prepareStatement("INSERT ");
        ps.setString(2, obj.getFirstName());
        return null;
    }

    @Override
    protected Void createTableImpl(Connection connection) {
        return null;
    }

    @Override
    protected List<User> getAllImpl(Connection connection) {
        return List.of();
    }

    @Override
    protected User getByIdImpl(Connection connection, UUID id) {
        return null;
    }

}
