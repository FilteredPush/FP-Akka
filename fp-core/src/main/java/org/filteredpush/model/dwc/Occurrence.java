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
import java.net.URI;
import java.util.UUID;

@RDFNamespaces({
        "dwc = " + Namespace.DWC,
        "dwcFP = " + Namespace.DWC_FP,
        "occurrence = " + Namespace.BASE_URI + "occurrence/"
})
@RDFBean("dwcFP:Occurrence")
public class Occurrence implements Serializable {
    private String id;

    private String hasOccurrenceID;
    private String associatedOccurrences;
    private String associatedReferences;
    private String associatedSequences;
    private String associatedTaxa;
    private String behavior;
    private String catalogNumber;
    private String collectionCode;
    private String disposition;
    private String establishmentMeans;
    private String individualCount;
    private String institutionCode;
    private String lifeStage;
    private String occurrenceRemarks;
    private String occurrenceStatus;
    private String otherCatalogNumbers;
    private String preparations;
    private String previousIdentifications;
    private String recordNumber;
    private String recordedBy;
    private String reproductiveCondition;
    private String sex;

    private Event collectingEvent;
    private Agent collector;
    private Identification identification;
    private String basisOfRecord;
    private URI collectionById;

    public Occurrence() {
        id = UUID.randomUUID().toString();
    }

    @JsonIgnore
    @RDFSubject(prefix = "occurrence:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("dwcFP:hasOccurrenceID")
    public String getHasOccurrenceID() {
        return hasOccurrenceID;
    }

    public void setHasOccurrenceID(String hasOccurrenceID) {
        this.hasOccurrenceID = hasOccurrenceID;
    }

    @RDF("dwc:associatedOccurrences")
    public String getAssociatedOccurrences() {
        return associatedOccurrences;
    }

    public void setAssociatedOccurrences(String associatedOccurrences) {
        this.associatedOccurrences = associatedOccurrences;
    }

    @RDF("dwc:associatedReferences")
    public String getAssociatedReferences() {
        return associatedReferences;
    }

    public void setAssociatedReferences(String associatedReferences) {
        this.associatedReferences = associatedReferences;
    }

    @RDF("dwc:associatedSequences")
    public String getAssociatedSequences() {
        return associatedSequences;
    }

    public void setAssociatedSequences(String associatedSequences) {
        this.associatedSequences = associatedSequences;
    }

    @RDF("dwc:associatedTaxa")
    public String getAssociatedTaxa() {
        return associatedTaxa;
    }

    public void setAssociatedTaxa(String associatedTaxa) {
        this.associatedTaxa = associatedTaxa;
    }

    @RDF("dwc:behavior")
    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    @RDF("dwc:catalogNumber")
    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    @RDF("dwc:collectionCode")
    public String getCollectionCode() {
        return collectionCode;
    }

    public void setCollectionCode(String collectionCode) {
        this.collectionCode = collectionCode;
    }

    @RDF("dwc:disposition")
    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    @RDF("dwc:establishmentMeans")
    public String getEstablishmentMeans() {
        return establishmentMeans;
    }

    public void setEstablishmentMeans(String establishmentMeans) {
        this.establishmentMeans = establishmentMeans;
    }

    @RDF("dwc:individualCount")
    public String getIndividualCount() {
        return individualCount;
    }

    public void setIndividualCount(String individualCount) {
        this.individualCount = individualCount;
    }

    @RDF("dwc:institutionCode")
    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    @RDF("dwc:lifeStage")
    public String getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(String lifeStage) {
        this.lifeStage = lifeStage;
    }

    @RDF("dwc:occurrenceRemarks")
    public String getOccurrenceRemarks() {
        return occurrenceRemarks;
    }

    public void setOccurrenceRemarks(String occurrenceRemarks) {
        this.occurrenceRemarks = occurrenceRemarks;
    }

    @RDF("dwc:occurrenceStatus")
    public String getOccurrenceStatus() {
        return occurrenceStatus;
    }

    public void setOccurrenceStatus(String occurrenceStatus) {
        this.occurrenceStatus = occurrenceStatus;
    }

    @RDF("dwc:otherCatalogNumbers")
    public String getOtherCatalogNumbers() {
        return otherCatalogNumbers;
    }

    public void setOtherCatalogNumbers(String otherCatalogNumbers) {
        this.otherCatalogNumbers = otherCatalogNumbers;
    }

    @RDF("dwc:preparations")
    public String getPreparations() {
        return preparations;
    }

    public void setPreparations(String preparations) {
        this.preparations = preparations;
    }

    @RDF("dwc:previousIdentifications")
    public String getPreviousIdentifications() {
        return previousIdentifications;
    }

    public void setPreviousIdentifications(String previousIdentifications) {
        this.previousIdentifications = previousIdentifications;
    }

    @RDF("dwc:recordNumber")
    public String getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
    }

    @RDF("dwc:recordedBy")
    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    @RDF("dwc:reproductiveCondition")
    public String getReproductiveCondition() {
        return reproductiveCondition;
    }

    public void setReproductiveCondition(String reproductiveCondition) {
        this.reproductiveCondition = reproductiveCondition;
    }

    @RDF("dwc:sex")
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @JsonUnwrapped()
    @RDF("dwcFP:hasCollectingEvent")
    public Event getCollectingEvent() {
        return collectingEvent;
    }

    public void setCollectingEvent(Event collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    @JsonUnwrapped(prefix = "collector_")
    @RDF("dwcFP:hasCollector")
    public Agent getCollector() {
        return collector;
    }

    public void setCollector(Agent collector) {
        this.collector = collector;
    }

    @JsonUnwrapped
    @RDF("dwcFP:hasIdentification")
    public Identification getIdentification() {
        return identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    @RDF("dwc:basisOfRecord")
    public String getBasisOfRecord() {
        return basisOfRecord;
    }

    public void setBasisOfRecord(String basisOfRecord) {
        this.basisOfRecord = basisOfRecord;
    }

    @RDF("dwcFP:hasCollectionByID")
    public URI getCollectionById() {
        return collectionById;
    }

    public void setCollectionById(URI collectionById) {
        this.collectionById = collectionById;
    }
}
