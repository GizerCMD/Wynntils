package me.gizer.wynnpointdb.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private Connector connector;

    public Database(Connector connector) {
        this.connector = connector;
    }

    public <T> T query(String sql, Decoder<T> decoder) throws SQLException {
        return query(sql, null, decoder);
    }

    public <T> T query(String sql, QueryModifier modifier, Decoder<T> decoder) throws SQLException {
        return connector.doConnected(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);

            if (modifier != null) {
                modifier.modifyQuery(statement);
            }

            ResultSet resultSet = statement.executeQuery();
            return decoder.decode(resultSet);
        });
    }

    public void update(String sql) throws SQLException {
        connector.doConnected(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        });
    }

    public <T> void update(String sql, Encoder<T> encoder, T element) throws SQLException {
        connector.doConnected(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            encoder.encode(statement, element);
            statement.addBatch();
            statement.executeBatch();
        });
    }

    public void update(String sql, QueryModifier modifier) throws SQLException {
        connector.doConnected(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            modifier.modifyQuery(statement);
            statement.executeUpdate();
        });
    }

    public void doConnected(ConnectedAction action) throws SQLException {
        connector.doConnected(action);
    }

    public <T> T doConnected(ConnectedActionReturn<T> action) throws SQLException {
        return connector.doConnected(action);
    }

    @FunctionalInterface
    public interface QueryModifier {
        void modifyQuery(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    public interface Encoder<T> {
        void encode(PreparedStatement statement, T element) throws SQLException;

        static <R> Encoder<List<R>> list(Encoder<R> encoder) {
            return (statement, elements) -> {
                int i = 0;

                for (R element : elements) {
                    encoder.encode(statement, element);
                    statement.addBatch();
                    i++;

                    if (i % 1000 == 0) {
                        statement.executeBatch();
                    }
                }
            };
        }
    }

    @FunctionalInterface
    public interface Decoder<T> {
        T decode(ResultSet result) throws SQLException;

        static <R> Decoder<List<R>> list(Decoder<R> decoder) {
            return result -> {
                List<R> itemList = new ArrayList<>();

                while (result.next()) {
                    itemList.add(decoder.decode(result));
                }

                return itemList;
            };
        }
    }
}
