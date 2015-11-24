/*
 * Copyright (c) 2013 President and Fellows of Harvard College
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of Version 2 of the GNU General Public License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.filteredpush.rdf;


import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.viceversatech.rdfbeans.RDFBeanManager;
import com.viceversatech.rdfbeans.exceptions.RDFBeanException;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.ModelFactory;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.impl.jena.ModelImplJena;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;

import java.io.*;
import java.util.*;

public class RdfUtil<T> {

    private static Map<String, Syntax> syntaxMap = new HashMap<String, Syntax>();

    private final Class<T> cls;
    private Map<String, String> nsPrefixes;
    private boolean bindingClasses;

    static {
        syntaxMap.put("RDF/XML", Syntax.RdfXml);
        syntaxMap.put("N3", Syntax.Turtle);
    }

    public RdfUtil(Class<T> cls) {
        this(cls, Collections.EMPTY_MAP, true);
    }

    public RdfUtil(Class<T> cls, Map<String, String> nsPrefixes) {
        this(cls, nsPrefixes, true);
    }

    public RdfUtil(Class<T> cls, Map<String, String> nsPrefixes, boolean bindingClasses) {
        this.cls = cls;
        this.nsPrefixes = new HashMap<String, String>(nsPrefixes);
        this.bindingClasses = bindingClasses;
    }

    public String serialize(T obj, String syntax) {
        return modelToString(serialize(obj), syntax);
    }

    public com.hp.hpl.jena.rdf.model.Model serialize(T obj) {
        ModelFactory modelFactory = RDF2Go.getModelFactory();
        Model model = modelFactory.createModel();
        model.open();

        try {
            RDFBeanManager manager = new RDFBeanManager(model);
            manager.add(obj);
        } catch (RDFBeanException e) {
            throw new RuntimeException(e);
        }

        com.hp.hpl.jena.rdf.model.Model jenaModel =
                (com.hp.hpl.jena.rdf.model.Model) model.getUnderlyingModelImplementation();

        if (!bindingClasses) {
            removeBindingClasses(jenaModel);
        }

        setNsPrefixes(jenaModel);
        return jenaModel;
    }

    public List<T> deserialize(com.hp.hpl.jena.rdf.model.Model jenaModel) {
        Model model = new ModelImplJena(jenaModel);
        model.open();

        RDFBeanManager manager = new RDFBeanManager(model);
        ClosableIterator<?> iter = null;
        try {
            iter = manager.getAll(cls);
        } catch (RDFBeanException e) {
            throw new RuntimeException(e);
        }

        List<T> result = new ArrayList();

        while (iter.hasNext()) {
            result.add((T) iter.next());
        }
        iter.close();
        model.close();

        return result;
    }

    public List<T> deserialize(String rdf, String syntax) {
        return deserialize(stringToModel(rdf, syntax));
    }

    public String modelToString(com.hp.hpl.jena.rdf.model.Model model, String syntax) {
        StringWriter writer = new StringWriter();
        model.write(writer, syntax);
        return writer.toString();
    }

    public com.hp.hpl.jena.rdf.model.Model stringToModel(String rdf, String syntax) {
        StringReader reader = new StringReader(rdf);
        com.hp.hpl.jena.rdf.model.Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
        model.read(reader, syntax);

        model.setNsPrefixes(nsPrefixes);

        if (bindingClasses) {
            model.setNsPrefix("rdfbeans", "http://viceversatech.com/rdfbeans/2.0/");
        }

        return model;
    }

    public static void removeBindingClasses(com.hp.hpl.jena.rdf.model.Model model) {
        Property bindingClass = ResourceFactory.createProperty("http://viceversatech.com/rdfbeans/2.0/bindingClass");
        ResIterator iter = model.listSubjectsWithProperty(bindingClass);

        while (iter.hasNext()) {
            Resource r = iter.nextResource();
            model.remove(r.getProperty(bindingClass));
        }
    }

    private void setNsPrefixes(com.hp.hpl.jena.rdf.model.Model model) {
        model.setNsPrefixes(nsPrefixes);
        if (bindingClasses) {
            model.setNsPrefix("rdfbeans", "http://viceversatech.com/rdfbeans/2.0/");
        }
    }

}