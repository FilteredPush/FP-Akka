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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.viceversatech.rdfbeans.annotations.RDF;
import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFNamespaces;
import com.viceversatech.rdfbeans.annotations.RDFSubject;
import org.filteredpush.model.annotations.Agent;
import org.filteredpush.model.annotations.Namespace;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: lowery
 * Date: 10/8/13
 * Time: 3:56 PM
 * To change this template use File | Settings | File Templates.
 */
@RDFNamespaces({
        "dwc = " + Namespace.DWC,
        "dwcFP = " + Namespace.DWC_FP,
        "identification = " + Namespace.BASE_URI + "identification/"
})
@RDFBean("dwcFP:Identification")
public class Identification implements Serializable {
    private String id;

    private String subgenus;

    @JsonUnwrapped
    private Taxon taxon;

    @JsonUnwrapped
    private Agent determiner;

    private String hasIdentificationID;
    //private String infraspecificRank;
    private String dateIdentified;
    //private String genus;
    private String identificationQualifier;
    private String identificationReferences;
    private String identificationRemarks;
    private String identificationVerificationStatus;
    private String identifiedBy;
    //private String infraspecificEpithet;
    private String lifeStage;
    //private String scientificName;
    //private String scientificNameAuthorship;
    private String sex;
    //private String specificEpithet;
    //private String taxonRank;
    private String typeStatus;
    private String filedUnderNameInCollection;

	private String hasOccurrenceID;

    public Identification() {
        id = UUID.randomUUID().toString();
    }

    @JsonIgnore
    @RDFSubject(prefix = "identification:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("dwc:identifiedBy")
    public String getIdentifiedBy() {
        return identifiedBy;
    }

    public void setIdentifiedBy(String identifiedBy) {
        this.identifiedBy = identifiedBy;
    }

    @RDF("dwc:dateIdentified")
    public String getDateIdentified() {
        return dateIdentified;
    }

    public void setDateIdentified(String dateIdentified) {
        this.dateIdentified = dateIdentified;
    }

/*  TODO: Use object property version instead, duplicate properties in dependant classes complicates serialization

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

    @RDF("dwc:genus")
    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    @RDF("dwc:subgenus")
    public String getSubgenus() {
        return subgenus;
    }

    public void setSubgenus(String subgenus) {
        this.subgenus = subgenus;
    }

    @RDF("dwc:specificEpithet")
    public String getSpecificEpithet() {
        return specificEpithet;
    }

    public void setSpecificEpithet(String specificEpithet) {
        this.specificEpithet = specificEpithet;
    }

    @RDF("dwc:infraspecificEpithet")
    public String getInfraspecificEpithet() {
        return infraspecificEpithet;
    }

    public void setInfraspecificEpithet(String infraspecificEpithet) {
        this.infraspecificEpithet = infraspecificEpithet;
    }

    @RDF("dwcFP:infraspecificRank")
    public String getInfraspecificRank() {
        return infraspecificRank;
    }

    public void setInfraspecificRank(String infraspecificRank) {
        this.infraspecificRank = infraspecificRank;
    }

    @RDF("dwc:taxonRank")
    public String getTaxonRank() {
        return taxonRank;
    }

    public void setTaxonRank(String taxonRank) {
        this.taxonRank = taxonRank;
    }

*/

    @RDF("dwc:identificationQualifier")
    public String getIdentificationQualifier() {
        return identificationQualifier;
    }

    public void setIdentificationQualifier(String identificationQualifier) {
        this.identificationQualifier = identificationQualifier;
    }

    @JsonUnwrapped
    @RDF("dwcFP:usesTaxon")
    public Taxon getTaxon() {
        return taxon;
    }

    public void setTaxon(Taxon taxon) {
        this.taxon = taxon;
    }

    @RDF("dwcFP:hasDeterminer")
    public Agent getDeterminer() {
        return determiner;
    }

    public void setDeterminer(Agent determiner) {
        this.determiner = determiner;
    }


    @RDF("dwcFP:hasIdentificationID")
    public String getHasIdentificationID() {
        return hasIdentificationID;
    }

    public void setHasIdentificationID(String hasIdentificationID) {
        this.hasIdentificationID = hasIdentificationID;
    }

    @RDF("dwc:identificationReferences")
    public String getIdentificationReferences() {
        return identificationReferences;
    }

    public void setIdentificationReferences(String identificationReferences) {
        this.identificationReferences = identificationReferences;
    }

    @RDF("dwc:identificationRemarks")
    public String getIdentificationRemarks() {
        return identificationRemarks;
    }

    public void setIdentificationRemarks(String identificationRemarks) {
        this.identificationRemarks = identificationRemarks;
    }

    @RDF("dwc:identificationVerificationStatus")
    public String getIdentificationVerificationStatus() {
        return identificationVerificationStatus;
    }

    public void setIdentificationVerificationStatus(String identificationVerificationStatus) {
        this.identificationVerificationStatus = identificationVerificationStatus;
    }

    @RDF("dwc:lifeStage")
    public String getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(String lifeStage) {
        this.lifeStage = lifeStage;
    }

    @RDF("dwc:sex")
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @RDF("dwc:typeStatus")
    public String getTypeStatus() {
        return typeStatus;
    }

    public void setTypeStatus(String typeStatus) {
        this.typeStatus = typeStatus;
    }

    @RDF("dwcFP:isFiledUnderNameInCollection")
    public String getFiledUnderNameInCollection() {
        return filedUnderNameInCollection;
    }

    public void setFiledUnderNameInCollection(String filedUnderNameInCollection) {
        this.filedUnderNameInCollection = filedUnderNameInCollection;
    }
    
    @RDF("dwcFP:hasOccurrenceID")
    public String getHasOccurrenceID() {
        return hasOccurrenceID;
    }

    public void setHasOccurrenceID(String hasOccurrenceID) {
        this.hasOccurrenceID = hasOccurrenceID;
    }
}
