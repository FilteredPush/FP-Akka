package org.filteredpush.knowledge;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VirtDocumentStoreConnection implements DocumentStoreConnection {
    private static final String DEFAULT_SERIALIZATION = "RDF/XML";

    private Connection conn;
        private String collection;

        public VirtDocumentStoreConnection(Connection conn, String collection) {
            this.conn = conn;
            this.collection = collection;
        }

        public void insert(String id, Model model) {
            try {
                StringWriter doc = new StringWriter();
                model.write(doc, DEFAULT_SERIALIZATION);

                PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + collection + " (id, doc) VALUES (?, ?)");
                stmt.setString(1, id);
                stmt.setString(2, doc.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                try { conn.close(); } catch (SQLException ex) { };
                throw new RuntimeException("Error executing document store insert", e);
            }
        }

        public Model find(String id) {
            try {
                PreparedStatement stmt = conn.prepareStatement("SELECT doc FROM " + collection + " WHERE id = ?");
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();

                rs.next();
                String doc = rs.getString(1);

                Model model = ModelFactory.createDefaultModel();
                model.read(new StringReader(doc), DEFAULT_SERIALIZATION);

                return model;
            } catch (SQLException e) {
                try { conn.close(); } catch (SQLException ex) { };
                throw new RuntimeException("Error executing document store select", e);
            }
        }

        public void close() { try { conn.close(); } catch (SQLException e) { };}
    }