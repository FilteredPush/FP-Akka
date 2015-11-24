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

package org.filteredpush.model.dwc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.viceversatech.rdfbeans.annotations.RDF;
import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFNamespaces;
import com.viceversatech.rdfbeans.annotations.RDFSubject;
import org.filteredpush.model.annotations.Namespace;

import java.io.Serializable;
import java.util.UUID;

@RDFNamespaces({
        "dwc = " + Namespace.DWC,
        "dwcFP = " + Namespace.DWC_FP,
        "taxon = " + Namespace.BASE_URI + "taxon/"
})
@RDFBean("dwcFP:Taxon")
public class Taxon implements Serializable {
    private String id;

    private String hasTaxonID;
    private String infraspecificRank;
    private String acceptedNameUsage;
    private String taxonClass;
    private String family;
    private String genus;
    private String higherClassification;
    private String infraspecificEpithet;
    private String kingdom;
    private String nameAccordingTo;
    private String namePublishedIn;
    private String namePublishedInYear;
    private String nomenclaturalCode;
    private String nomenclaturalStatus;
    private String order;
    private String originalNameUsage;
    private String parentNameUsage;
    private String phylum;
    private String scientificName;
    private String scientificNameAuthorship;
    private String specificEpithet;
    private String subgenus;
    private String taxonRank;
    private String taxonRemarks;
    private String taxonomicStatus;
    private String verbatimTaxonRank;
    private String vernacularName;


    public Taxon() {
        id = UUID.randomUUID().toString();
    }

    @JsonIgnore
    @RDFSubject(prefix = "taxon:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("dwcFP:hasTaxonID")
    public String getHasTaxonID() {
        return hasTaxonID;
    }

    public void setHasTaxonID(String hasTaxonID) {
        this.hasTaxonID = hasTaxonID;
    }

    @RDF("dwcFP:infraspecificRank")
    public String getInfraspecificRank() {
        return infraspecificRank;
    }

    public void setInfraspecificRank(String infraspecificRank) {
        this.infraspecificRank = infraspecificRank;
    }

    @RDF("dwc:acceptedNameUsage")
    public String getAcceptedNameUsage() {
        return acceptedNameUsage;
    }

    public void setAcceptedNameUsage(String acceptedNameUsage) {
        this.acceptedNameUsage = acceptedNameUsage;
    }

    @RDF("dwc:class")
    @JsonProperty("class")
    public String getTaxonClass() {
        return taxonClass;
    }

    public void setTaxonClass(String taxonClass) {
        this.taxonClass = taxonClass;
    }

    @RDF("dwc:family")
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    @RDF("dwc:genus")
    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    @RDF("dwc:higherClassification")
    public String getHigherClassification() {
        return higherClassification;
    }

    public void setHigherClassification(String higherClassification) {
        this.higherClassification = higherClassification;
    }

    @RDF("dwc:infraspecificEpithet")
    public String getInfraspecificEpithet() {
        return infraspecificEpithet;
    }

    public void setInfraspecificEpithet(String infraspecificEpithet) {
        this.infraspecificEpithet = infraspecificEpithet;
    }

    @RDF("dwc:kingdom")
    public String getKingdom() {
        return kingdom;
    }

    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }

    @RDF("dwc:nameAccordingTo")
    public String getNameAccordingTo() {
        return nameAccordingTo;
    }

    public void setNameAccordingTo(String nameAccordingTo) {
        this.nameAccordingTo = nameAccordingTo;
    }

    @RDF("dwc:namePublishedIn")
    public String getNamePublishedIn() {
        return namePublishedIn;
    }

    public void setNamePublishedIn(String namePublishedIn) {
        this.namePublishedIn = namePublishedIn;
    }

    @RDF("dwc:namePublishedInYear")
    public String getNamePublishedInYear() {
        return namePublishedInYear;
    }

    public void setNamePublishedInYear(String namePublishedInYear) {
        this.namePublishedInYear = namePublishedInYear;
    }

    @RDF("dwc:nomenclaturalCode")
    public String getNomenclaturalCode() {
        return nomenclaturalCode;
    }

    public void setNomenclaturalCode(String nomenclaturalCode) {
        this.nomenclaturalCode = nomenclaturalCode;
    }

    @RDF("dwc:nomenclaturalStatus")
    public String getNomenclaturalStatus() {
        return nomenclaturalStatus;
    }

    public void setNomenclaturalStatus(String nomenclaturalStatus) {
        this.nomenclaturalStatus = nomenclaturalStatus;
    }

    @RDF("dwc:order")
    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    @RDF("dwc:originalNameUsage")
    public String getOriginalNameUsage() {
        return originalNameUsage;
    }

    public void setOriginalNameUsage(String originalNameUsage) {
        this.originalNameUsage = originalNameUsage;
    }

    @RDF("dwc:parentNameUsage")
    public String getParentNameUsage() {
        return parentNameUsage;
    }

    public void setParentNameUsage(String parentNameUsage) {
        this.parentNameUsage = parentNameUsage;
    }

    @RDF("dwc:phylum")
    public String getPhylum() {
        return phylum;
    }

    public void setPhylum(String phylum) {
        this.phylum = phylum;
    }

    @RDF("dwc:scientificName")
    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    @RDF("dwc:scientificNameAuthorship")
    public String getScientificNameAuthorship() {
        return scientificNameAuthorship;
    }

    public void setScientificNameAuthorship(String scientificNameAuthorship) {
        this.scientificNameAuthorship = scientificNameAuthorship;
    }

    @RDF("dwc:specificEpithet")
    public String getSpecificEpithet() {
        return specificEpithet;
    }

    public void setSpecificEpithet(String specificEpithet) {
        this.specificEpithet = specificEpithet;
    }

    @RDF("dwc:subgenus")
    public String getSubgenus() {
        return subgenus;
    }

    public void setSubgenus(String subgenus) {
        this.subgenus = subgenus;
    }

    @RDF("dwc:taxonRank")
    public String getTaxonRank() {
        return taxonRank;
    }

    public void setTaxonRank(String taxonRank) {
        this.taxonRank = taxonRank;
    }

    @RDF("dwc:taxonRemarks")
    public String getTaxonRemarks() {
        return taxonRemarks;
    }

    public void setTaxonRemarks(String taxonRemarks) {
        this.taxonRemarks = taxonRemarks;
    }

    @RDF("dwc:taxonomicStatus")
    public String getTaxonomicStatus() {
        return taxonomicStatus;
    }

    public void setTaxonomicStatus(String taxonomicStatus) {
        this.taxonomicStatus = taxonomicStatus;
    }

    @RDF("dwc:verbatimTaxonRank")
    public String getVerbatimTaxonRank() {
        return verbatimTaxonRank;
    }

    public void setVerbatimTaxonRank(String verbatimTaxonRank) {
        this.verbatimTaxonRank = verbatimTaxonRank;
    }

    @RDF("dwc:vernacularName")
    public String getVernacularName() {
        return vernacularName;
    }

    public void setVernacularName(String vernacularName) {
        this.vernacularName = vernacularName;
    }
}
