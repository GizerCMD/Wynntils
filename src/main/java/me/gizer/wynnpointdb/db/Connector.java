package me.gizer.wynnpointdb.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class Connector {
    private Connection connection;

    public static Connector sqliteConnector(Path file) {
        return new Connector() {
            @Override
            Connection connect() throws SQLException {
                return DriverManager.getConnection(String.format("jdbc:sqlite:%s", file));
            }
        };
    }

    abstract Connection connect() throws SQLException;

    public void doConnected(ConnectedAction action) throws SQLException {
        if (connection == null) {
            try {
                connection = connect();
                action.doAction(connection);
                connection.close();
            } finally {
                connection = null;
            }
        } else {
            action.doAction(connection);
        }
    }

    public <T> T doConnected(ConnectedActionReturn<T> action) throws SQLException {
        if (connection == null) {
            try {
                connection = connect();
                return action.doAction(connection);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } finally {
                    connection = null;
                }
            }
        } else {
            return action.doAction(connection);
        }
    }
}
