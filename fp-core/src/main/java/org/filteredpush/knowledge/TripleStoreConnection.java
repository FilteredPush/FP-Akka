package org.filteredpush.knowledge;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Created by lowery on 11/4/15.
 */
public interface TripleStoreConnection {
    public QueryExecution getQueryExecution(Query query);
    public void load(Model model);
    public void close();
}
