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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sun.xml.internal.txw2.annotation.XmlElement;
import com.viceversatech.rdfbeans.annotations.RDF;
import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFNamespaces;
import com.viceversatech.rdfbeans.annotations.RDFSubject;
import org.filteredpush.model.dwc.DwcTriplet;
import org.filteredpush.model.dwc.Georeference;
import org.filteredpush.model.dwc.Identification;
import org.filteredpush.model.util.JsonDateSerializer;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

@RDFNamespaces({
        "oa = " + Namespace.OA,
        "oad = " + Namespace.OAD,
        "annotation = " + Namespace.BASE_URI + "annotation/"
})
@RDFBean("oa:Annotation")
@XmlRootElement
public class Annotation<T, B> implements Serializable {
    private String id;

    private T target;
    private B body;

    private Evidence evidence;
    private Agent annotator;
    private Agent generator;
    private Date annotated;

    private Date generated;

    private Expectation expectation;
    private Motivation motivation;
    
    public Annotation() {
        id = UUID.randomUUID().toString();
    }

    @JsonIgnore
    @RDFSubject(prefix = "annotation:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URI getUri() {
        URI uri = null;
        try {
           uri = new URI(Namespace.BASE_URI + "annotation/" + id);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }

    @RDF("oa:hasTarget")
    public T getTarget() {
        return target;
    }

    public void setTarget(T target) {
        this.target = target;
    }

    @RDF("oa:hasBody")
    public B getBody() {
        return body;
    }

    public void setBody(B body) {
        this.body = body;
    }

        @RDF("oad:hasExpectation")
        public Expectation getExpectation() {
        	if (expectation != null) {
        		return Expectation.valueOf(expectation.getName());
        	} else {
        		return null;
        	}
        }

    public void setExpectation(Expectation expectation) {
            this.expectation = Expectation.valueOf(expectation.getName());;
        }

    @JsonUnwrapped
    @RDF("oa:motivatedBy")
    public Motivation getMotivation() {
    	if (motivation != null) {
    		return Motivation.valueOf(motivation.getName());
    	} else {
    		return null;
    	}
    }

    public void setMotivation(Motivation motivation) {
    	if (motivation != null && motivation.getName() != null) {
     	  this.motivation = Motivation.valueOf(motivation.getName());
    	} else {
    		motivation = null;
    	}
    }

    @RDF("oad:hasEvidence")
    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    @JsonUnwrapped(prefix = "annotator_")
    @RDF("oa:annotatedBy")
    public Agent getAnnotator() {
        return annotator;
    }

    public void setAnnotator(Agent annotator) {
        this.annotator = annotator;
    }

    @JsonUnwrapped(prefix = "generator_")
    @RDF("oa:serializedBy")
       public Agent getGenerator() {
            return generator;
        }

        public void setGenerator(Agent generator) {
            this.generator = generator;
        }

    @JsonSerialize(using=JsonDateSerializer.class)
    @RDF("oa:annotatedAt")
    public Date getAnnotated() {
        return annotated;
    }

    public void setAnnotated(Date annotated) {
        this.annotated = annotated;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    @RDF("oa:serializedAt")
    public Date getGenerated() {
        return generated;
    }

    public void setGenerated(Date generated) {
        this.generated = generated;
    }

    public AnnotationType getAnnotationType() {
        if (expectation == Expectation.INSERT) {
            if (body instanceof Identification) {
                return AnnotationType.INSERT_IDENTIFICATION;
            } else if (body instanceof Georeference) {
                return AnnotationType.INSERT_GEOREFERENCE;
            }
        } else if (expectation == Expectation.UPDATE) {
            if (body instanceof Identification) {
                return AnnotationType.UPDATE_IDENTIFICATION;
            } else if (body instanceof Georeference) {
                return AnnotationType.UPDATE_GEOREFERENCE;
            }
        } else if (body instanceof Opinion && target instanceof Issue) {
            return AnnotationType.RESPONSE;
        } else if (expectation == Expectation.SOLVE_WITH_MORE_DATA) {
            return AnnotationType.SOLVE_WITH_MORE_DATA;
        } else {
            throw new RuntimeException("The type of this annotation could not be determined!");
        }
        return null;
    }
}
