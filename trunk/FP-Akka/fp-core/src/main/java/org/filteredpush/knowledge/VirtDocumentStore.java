package org.filteredpush.knowledge;

import java.sql.*;
import java.util.Properties;

/**
 * Created by lowery on 11/4/15.
 */
public class VirtDocumentStore implements DocumentStore {
    private String hostname;
    private int port;
    private String username;
    private String password;

    private static String DEFAULT_SERIALIZATION = "RDF/XML";

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DocumentStoreConnection getConnection(String db, String collection) {
        String url = "jdbc:virtuoso://" + hostname + ":" + port + "/DATABASE=" + db;

        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);
        try {
            Connection conn = DriverManager.getConnection(url, connectionProps);

            return new VirtDocumentStoreConnection(conn, collection);
        } catch (SQLException e) {
            throw new RuntimeException("Could not open connection to document store", e);
        }
    }
}
