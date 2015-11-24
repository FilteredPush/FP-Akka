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
        "dcterms = " + Namespace.DCTERMS,
        "record = " + Namespace.BASE_URI + "record/"
})
@RDFBean("dwcFP:Record")
public class Record implements Serializable {
    private String id;

    private String accessRights;
    private String language;
    private String modified;
    private String references;
    private String rights;
    private String rightsHolder;
    private String type;
    private String basisOfRecord;
    private String bibliographicCitation;
    private String dataGeneralizations;
    private String datasetName;
    private String dynamicProperties;
    private String informationWithheld;
    private String ownerInstitutionCode;

    public Record() {
        id = UUID.randomUUID().toString();
    }

    @RDFSubject(prefix = "record:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("dcterms:accessRights")
    public String getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(String accessRights) {
        this.accessRights = accessRights;
    }

    @RDF("dcterms:language")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @RDF("dcterms:modified")
    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    @RDF("dcterms:references")
    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    @RDF("dcterms:rights")
    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    @RDF("dcterms:rightsHolder")
    public String getRightsHolder() {
        return rightsHolder;
    }

    public void setRightsHolder(String rightsHolder) {
        this.rightsHolder = rightsHolder;
    }

    @RDF("dcterms:type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @RDF("dwc:basisOfRecord")
    public String getBasisOfRecord() {
        return basisOfRecord;
    }

    public void setBasisOfRecord(String basisOfRecord) {
        this.basisOfRecord = basisOfRecord;
    }

    @RDF("dwc:bibliographicCitation")
    public String getBibliographicCitation() {
        return bibliographicCitation;
    }

    public void setBibliographicCitation(String bibliographicCitation) {
        this.bibliographicCitation = bibliographicCitation;
    }

    @RDF("dwc:dataGeneralizations")
    public String getDataGeneralizations() {
        return dataGeneralizations;
    }

    public void setDataGeneralizations(String dataGeneralizations) {
        this.dataGeneralizations = dataGeneralizations;
    }

    @RDF("dwc:datasetName")
    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    @RDF("dwc:dynamicProperties")
    public String getDynamicProperties() {
        return dynamicProperties;
    }

    public void setDynamicProperties(String dynamicProperties) {
        this.dynamicProperties = dynamicProperties;
    }

    @RDF("dwc:informationWithheld")
    public String getInformationWithheld() {
        return informationWithheld;
    }

    public void setInformationWithheld(String informationWithheld) {
        this.informationWithheld = informationWithheld;
    }

    @RDF("dwc:ownerInstitutionCode")
    public String getOwnerInstitutionCode() {
        return ownerInstitutionCode;
    }

    public void setOwnerInstitutionCode(String ownerInstitutionCode) {
        this.ownerInstitutionCode = ownerInstitutionCode;
    }
}
