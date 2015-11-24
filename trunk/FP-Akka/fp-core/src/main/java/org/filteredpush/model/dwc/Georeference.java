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
        "georeference = " + Namespace.BASE_URI + "georeference/"
})
@RDFBean("dwcFP:Georeference")
public class Georeference implements Serializable {
    private String id;

    @JsonUnwrapped
    private Agent georeferencer;

    private URI source;

    private String coordinatePrecision;
    private String coordinateUncertaintyInMeters;
    private String decimalLatitude;
    private String decimalLongitude;
    private String footprintSRS;
    private String footprintSpatialFit;
    private String footprintWKT;
    private String geodeticDatum;
    private String georeferenceProtocol;
    private String georeferenceRemarks;
    private String georeferenceSources;
    private String georeferenceVerificationStatus;
    private String georeferencedBy;
    private String pointRadiusSpatialFit;
    private String verbatimCoordinateSystem;
    private String verbatimCoordinates;
    private String verbatimElevation;
    private String verbatimLatitude;
    private String verbatimLocality;
    private String verbatimLongitude;
    private String verbatimSRS;
    
    private String scientificName;
    private String scientificNameAuthorship;
    private String taxonRank;
    private String genus;
    private String subgenus;

    public Georeference() {
        id = UUID.randomUUID().toString();
    }

    @RDFSubject(prefix = "georeference:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("dwc:coordinatePrecision")
    public String getCoordinatePrecision() {
        return coordinatePrecision;
    }

    public void setCoordinatePrecision(String coordinatePrecision) {
        this.coordinatePrecision = coordinatePrecision;
    }

    @RDF("dwc:footprintSpatialFit")
    public String getFootprintSpatialFit() {
        return footprintSpatialFit;
    }

    public void setFootprintSpatialFit(String footprintSpatialFit) {
        this.footprintSpatialFit = footprintSpatialFit;
    }

    @RDF("dwc:footprintSRS")
    public String getFootprintSRS() {
        return footprintSRS;
    }

    public void setFootprintSRS(String footprintSRS) {
        this.footprintSRS = footprintSRS;
    }

    @RDF("dwc:footprintWKT")
    public String getFootprintWKT() {
        return footprintWKT;
    }

    public void setFootprintWKT(String footprintWKT) {
        this.footprintWKT = footprintWKT;
    }

    @RDF("dwc:geodeticDatum")
    public String getGeodeticDatum() {
        return geodeticDatum;
    }

    public void setGeodeticDatum(String geodeticDatum) {
        this.geodeticDatum = geodeticDatum;
    }

    @RDF("dwc:georeferencedBy")
    public String getGeoreferencedBy() {
        return georeferencedBy;
    }

    public void setGeoreferencedBy(String georeferencedBy) {
        this.georeferencedBy = georeferencedBy;
    }

    @RDF("dwcFP:hasGeoreferencer")
    public Agent getGeoreferencer() {
        return georeferencer;
    }

    public void setGeoreferencer(Agent georeferencer) {
        this.georeferencer = georeferencer;
    }

    @RDF("dwcFP:hasGeoreferenceSource")
    public URI getSource() {
        return source;
    }

    public void setSource(URI source) {
        this.source = source;
    }

    @RDF("dwc:verbatimCoordinates")
    public String getVerbatimCoordinates() {
        return verbatimCoordinates;
    }

    public void setVerbatimCoordinates(String verbatimCoordinates) {
        this.verbatimCoordinates = verbatimCoordinates;
    }

    @RDF("dwc:coordinateUncertaintyInMeters")
    public String getCoordinateUncertaintyInMeters() {
        return coordinateUncertaintyInMeters;
    }

    public void setCoordinateUncertaintyInMeters(String coordinateUncertaintyInMeters) {
        this.coordinateUncertaintyInMeters = coordinateUncertaintyInMeters;
    }

    @RDF("dwc:decimalLatitude")
    public String getDecimalLatitude() {
        return decimalLatitude;
    }

    public void setDecimalLatitude(String decimalLatitude) {
        this.decimalLatitude = decimalLatitude;
    }

    @RDF("dwc:decimalLongitude")
    public String getDecimalLongitude() {
        return decimalLongitude;
    }

    public void setDecimalLongitude(String decimalLongitude) {
        this.decimalLongitude = decimalLongitude;
    }

    @RDF("dwc:georeferenceProtocol")
    public String getGeoreferenceProtocol() {
        return georeferenceProtocol;
    }

    public void setGeoreferenceProtocol(String georeferenceProtocol) {
        this.georeferenceProtocol = georeferenceProtocol;
    }

    @RDF("dwc:georeferenceRemarks")
    public String getGeoreferenceRemarks() {
        return georeferenceRemarks;
    }

    public void setGeoreferenceRemarks(String georeferenceRemarks) {
        this.georeferenceRemarks = georeferenceRemarks;
    }

    @RDF("dwc:georeferenceSources")
    public String getGeoreferenceSources() {
        return georeferenceSources;
    }

    public void setGeoreferenceSources(String georeferenceSources) {
        this.georeferenceSources = georeferenceSources;
    }

    @RDF("dwc:georeferenceVerificationStatus")
    public String getGeoreferenceVerificationStatus() {
        return georeferenceVerificationStatus;
    }

    public void setGeoreferenceVerificationStatus(String georeferenceVerificationStatus) {
        this.georeferenceVerificationStatus = georeferenceVerificationStatus;
    }

    @RDF("dwc:pointRadiusSpatialFit")
    public String getPointRadiusSpatialFit() {
        return pointRadiusSpatialFit;
    }

    public void setPointRadiusSpatialFit(String pointRadiusSpatialFit) {
        this.pointRadiusSpatialFit = pointRadiusSpatialFit;
    }

    @RDF("dwc:verbatimCoordinateSystem")
    public String getVerbatimCoordinateSystem() {
        return verbatimCoordinateSystem;
    }

    public void setVerbatimCoordinateSystem(String verbatimCoordinateSystem) {
        this.verbatimCoordinateSystem = verbatimCoordinateSystem;
    }

    @RDF("dwc:verbatimElevation")
    public String getVerbatimElevation() {
        return verbatimElevation;
    }

    public void setVerbatimElevation(String verbatimElevation) {
        this.verbatimElevation = verbatimElevation;
    }

    @RDF("dwc:verbatimLatitude")
    public String getVerbatimLatitude() {
        return verbatimLatitude;
    }

    public void setVerbatimLatitude(String verbatimLatitude) {
        this.verbatimLatitude = verbatimLatitude;
    }

    @RDF("dwc:verbatimLocality")
    public String getVerbatimLocality() {
        return verbatimLocality;
    }

    public void setVerbatimLocality(String verbatimLocality) {
        this.verbatimLocality = verbatimLocality;
    }

    @RDF("dwc:verbatimLongitude")
    public String getVerbatimLongitude() {
        return verbatimLongitude;
    }

    public void setVerbatimLongitude(String verbatimLongitude) {
        this.verbatimLongitude = verbatimLongitude;
    }

    @RDF("dwc:verbatimSRS")
    public String getVerbatimSRS() {
        return verbatimSRS;
    }

    public void setVerbatimSRS(String verbatimSRS) {
        this.verbatimSRS = verbatimSRS;
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

	@RDF("dwc:taxonRank")
	public String getTaxonRank() {
		return taxonRank;
	}

	public void setTaxonRank(String taxonRank) {
		this.taxonRank = taxonRank;
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
    
    
}
