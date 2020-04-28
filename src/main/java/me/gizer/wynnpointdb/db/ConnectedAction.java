package me.gizer.wynnpointdb.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectedAction {
    void doAction(Connection connection) throws SQLException;
}
