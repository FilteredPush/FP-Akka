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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.viceversatech.rdfbeans.annotations.RDF;
import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFNamespaces;
import com.viceversatech.rdfbeans.annotations.RDFSubject;
import org.filteredpush.model.util.JsonPolarityDeserializer;

import java.io.Serializable;
import java.util.UUID;

import static org.filteredpush.model.annotations.Namespace.BASE_URI;
import static org.filteredpush.model.annotations.Namespace.MARL;

@RDFNamespaces({
        "marl = " + MARL,
        "opinion = " + BASE_URI + "opinion/"
})
@RDFBean("marl:Opinion")
public class Opinion implements Serializable {
    private String id;

    private Polarity polarity;
    private Issue describesObject;
    private String opinionText;

    public Opinion() {
        id = UUID.randomUUID().toString();
    }

    @RDFSubject(prefix = "opinion:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonDeserialize(using=JsonPolarityDeserializer.class)
    @RDF("marl:hasPolarity")
    public Polarity getPolarity() {
        if (polarity != null) {
            return Polarity.valueOf(polarity.getName());
        } else {
            return null;
        }
    }

    public void setPolarity(Polarity polarity) {
        this.polarity = Polarity.valueOf(polarity.getName());
    }

    @RDF("marl:describesObject")
    public Issue getDescribesObject() {
        return describesObject;
    }

    public void setDescribesObject(Issue describesObject) {
        this.describesObject = describesObject;
    }

    @RDF("marl:opinionText")
    public String getOpinionText() {
        return opinionText;
    }

    public void setOpinionText(String opinionText) {
        this.opinionText = opinionText;
    }
}
