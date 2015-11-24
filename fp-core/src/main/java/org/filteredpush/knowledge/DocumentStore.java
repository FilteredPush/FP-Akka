package org.filteredpush.knowledge;

/**
 * Created by lowery on 11/4/15.
 */
public interface DocumentStore {
    public DocumentStoreConnection getConnection(String db, String collection);
}
