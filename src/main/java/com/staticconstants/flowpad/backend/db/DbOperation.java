package com.staticconstants.flowpad.backend.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface DbOperation<T> {
    T operation(Connection dbConnection) throws Exception;
}
