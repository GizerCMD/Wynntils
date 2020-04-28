package me.gizer.wynnpointdb.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectedActionReturn<T> {
    T doAction(Connection connection) throws SQLException;
}
