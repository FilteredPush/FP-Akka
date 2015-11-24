package org.filteredpush.knowledge;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class VirtTripleStoreConnection implements TripleStoreConnection {
        private VirtGraph graph;

        public VirtTripleStoreConnection(VirtGraph graph) {
            this.graph = graph;
        }

        @Override
        public QueryExecution getQueryExecution(Query query) {
            return VirtuosoQueryExecutionFactory.create(query, graph);
        }

        @Override
        public void load(Model model) {
            VirtModel virtModel = null;
            virtModel = new VirtModel(graph);
            virtModel.add(model);
        }

        @Override
        public void close() {
            graph.close();
        }
    }