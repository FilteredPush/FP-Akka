package org.filteredpush.knowledge;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Created by lowery on 11/4/15.
 */
public interface DocumentStoreConnection {
    public void insert(String id, Model model);
    public Model find(String id);
    public void close();
}
