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

package org.filteredpush.model.annotations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.viceversatech.rdfbeans.annotations.RDF;
import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFNamespaces;
import com.viceversatech.rdfbeans.annotations.RDFSubject;
import org.filteredpush.model.dwc.Occurrence;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import static org.filteredpush.model.annotations.Namespace.*;

@RDFNamespaces({
        "oa = " + OA,
        "oad = " + OAD,
        "bom = " + BOM,
        "target = " + BASE_URI + "target/"
})
@RDFBean("oa:SpecificResource")
public class SpecificResource<S> implements Serializable {
    private String id;

    private URI source;
    private State state;
    private S selector;

    private Resolution resolution;

    public SpecificResource() {
        id = UUID.randomUUID().toString();
        source = ANY_SOURCE;
    }

    @JsonIgnore
    @RDFSubject(prefix="target:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonUnwrapped
    @RDF("oa:hasSource")
    public URI getSource() {
        return source;
    }

    public void setSource(URI source) {
        this.source = source;
    }

    @RDF("oa:hasState")
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @JsonUnwrapped
    @RDF("oa:hasSelector")
    public S getSelector() {
        return selector;
    }

    public void setSelector(S selector) {
        this.selector = selector;
    }

    @RDF("bom:hasResolution")
    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    @JsonIgnore
    public Occurrence getSelectorAsOccurrence() {
        if (selector instanceof DwcTripletSelector) {
            Occurrence occurrence = new Occurrence();
            occurrence.setCatalogNumber(((DwcTripletSelector) selector).getCatalogNumber());
            occurrence.setCollectionCode(((DwcTripletSelector)selector).getCollectionCode());
            occurrence.setInstitutionCode(((DwcTripletSelector)selector).getInstitutionCode());

        return occurrence;
    } else {
            throw new IllegalStateException("Unsupported selector type for this operation: " + selector.getClass());
        }
    }
}