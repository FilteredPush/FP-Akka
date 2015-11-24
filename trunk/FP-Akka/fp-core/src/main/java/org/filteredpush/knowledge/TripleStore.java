package org.filteredpush.knowledge;

/**
 * Created by lowery on 11/4/15.
 */
public interface TripleStore {
    public TripleStoreConnection getConnection();
    public TripleStoreConnection getConnection(String graph);
}
