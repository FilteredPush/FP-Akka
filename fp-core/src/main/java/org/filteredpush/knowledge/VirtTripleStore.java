package org.filteredpush.knowledge;

import virtuoso.jena.driver.VirtGraph;

/**
 * Created by lowery on 10/13/15.
 */
public class VirtTripleStore implements TripleStore {
    private String hostname;
    private int port;
    private String username;
    private String password;

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

    @Override
    public TripleStoreConnection getConnection(String graph) {
        String url = "jdbc:virtuoso://" + hostname + ":" + port;
        return new VirtTripleStoreConnection(new VirtGraph(graph, url, username, password));
    }

    @Override
    public VirtTripleStoreConnection getConnection() {
        String url = "jdbc:virtuoso://" + hostname + ":" + port;
        return new VirtTripleStoreConnection(new VirtGraph(url, username, password));
    }
}
