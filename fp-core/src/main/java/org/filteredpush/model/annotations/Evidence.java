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

import com.viceversatech.rdfbeans.annotations.RDF;
import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFNamespaces;
import com.viceversatech.rdfbeans.annotations.RDFSubject;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RDFNamespaces({
        "dwc = " + Namespace.DWC,
        "cnt = " + Namespace.CNT,
        "oa = " + Namespace.OA,
        "con = http://filteredpush.org/ontologies/consensus#",
        "evidence = " + Namespace.BASE_URI + "evidence/"
})
@RDFBean("oa:Evidence")
public class Evidence implements Serializable {
    private String id;
    private String chars;

    private List<URI> consenting = new ArrayList<URI>();
    private List<URI> dissenting = new ArrayList<URI>();

    private String verbatimLocality;
    private String verbatimCoordinates;
    private String verbatimLatitude;
    private String verbatimLongitude;
    private String verbatimCoordinateSystem;
    private String verbatimSRS;
    private String verbatimEventDate;
    private String verbatimTaxonRank;
    private String verbatimElevation;
    private String verbatimDepth;

    public Evidence() {
        id = UUID.randomUUID().toString();
    }

    @RDF("con:consenting")
    public List<URI> getConsenting() {
        return consenting;
    }

    public void setConsenting(List<URI> consenting) {
        this.consenting = consenting;
    }

    @RDF("con:dissenting")
    public List<URI> getDissenting() {
        return dissenting;
    }

    public void setDissenting(List<URI> dissenting) {
        this.dissenting = dissenting;
    }

    @RDFSubject(prefix = "evidence:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("cnt:chars")
    public String getChars() {
        return chars;
    }

    public void setChars(String chars) {
        this.chars = chars;
    }

    @RDF("dwc:verbatimLocality")
    public String getVerbatimLocality() {
        return verbatimLocality;
    }

    public void setVerbatimLocality(String verbatimLocality) {
        this.verbatimLocality = verbatimLocality;
    }

    @RDF("dwc:verbatimCoordinates")
    public String getVerbatimCoordinates() {
        return verbatimCoordinates;
    }

    public void setVerbatimCoordinates(String verbatimCoordinates) {
        this.verbatimCoordinates = verbatimCoordinates;
    }

    @RDF("dwc:verbatimLatitude")
    public String getVerbatimLatitude() {
        return verbatimLatitude;
    }

    public void setVerbatimLatitude(String verbatimLatitude) {
        this.verbatimLatitude = verbatimLatitude;
    }

    @RDF("dwc:verbatimLongitude")
    public String getVerbatimLongitude() {
        return verbatimLongitude;
    }

    public void setVerbatimLongitude(String verbatimLongitude) {
        this.verbatimLongitude = verbatimLongitude;
    }

    @RDF("dwc:verbatimCoordinateSystem")
    public String getVerbatimCoordinateSystem() {
        return verbatimCoordinateSystem;
    }

    public void setVerbatimCoordinateSystem(String verbatimCoordinateSystem) {
        this.verbatimCoordinateSystem = verbatimCoordinateSystem;
    }

    @RDF("dwc:verbatimSRS")
    public String getVerbatimSRS() {
        return verbatimSRS;
    }

    public void setVerbatimSRS(String verbatimSRS) {
        this.verbatimSRS = verbatimSRS;
    }

    @RDF("dwc:verbatimEventDate")
    public String getVerbatimEventDate() {
        return verbatimEventDate;
    }

    public void setVerbatimEventDate(String verbatimEventDate) {
        this.verbatimEventDate = verbatimEventDate;
    }

    @RDF("dwc:verbatimTaxonRank")
    public String getVerbatimTaxonRank() {
        return verbatimTaxonRank;
    }

    public void setVerbatimTaxonRank(String verbatimTaxonRank) {
        this.verbatimTaxonRank = verbatimTaxonRank;
    }

    @RDF("dwc:verbatimElevation")
    public String getVerbatimElevation() {
        return verbatimElevation;
    }

    public void setVerbatimElevation(String verbatimElevation) {
        this.verbatimElevation = verbatimElevation;
    }

    @RDF("dwc:verbatimDepth")
    public String getVerbatimDepth() {
        return verbatimDepth;
    }

    public void setVerbatimDepth(String verbatimDepth) {
        this.verbatimDepth = verbatimDepth;
    }
}
