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
        "location = " + Namespace.BASE_URI + "location/"
})
@RDFBean("dwcFP:Location")
public class Location implements Serializable {
    private String id;

    private String hasLocationID;
    private String continent;
    private String country;
    private String countryCode;
    private String county;
    private String habitat;
    private String higherGeography;
    private String island;
    private String islandGroup;
    private String locality;
    private String locationAccordingTo;
    private String locationRemarks;
    private String maximumDepthInMeters;
    private String maximumDistanceAboveSurfaceInMeters;
    private String maximumElevationInMeters;
    private String minimumDepthInMeters;
    private String minimumDistanceAboveSurfaceInMeters;
    private String minimumElevationInMeters;
    private String municipality;
    private String stateProvince;
    private String verbatimCoordinates;
    private String verbatimDepth;
    private String verbatimElevation;
    private String verbatimLatitude;
    private String verbatimLocality;
    private String verbatimLongitude;
    private String waterBody;

    public Location() {
        id = UUID.randomUUID().toString();
    }

    @JsonIgnore
    @RDFSubject(prefix = "location:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("dwcFP:hasLocationID")
    public String getHasLocationID() {
        return hasLocationID;
    }

    public void setHasLocationID(String hasLocationID) {
        this.hasLocationID = hasLocationID;
    }

    @RDF("dwc:continent")
    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    @RDF("dwc:country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @RDF("dwc:countryCode")
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @RDF("dwc:county")
    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    @RDF("dwc:habitat")
    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    @RDF("dwc:higherGeography")
    public String getHigherGeography() {
        return higherGeography;
    }

    public void setHigherGeography(String higherGeography) {
        this.higherGeography = higherGeography;
    }

    @RDF("dwc:island")
    public String getIsland() {
        return island;
    }

    public void setIsland(String island) {
        this.island = island;
    }

    @RDF("dwc:islandGroup")
    public String getIslandGroup() {
        return islandGroup;
    }

    public void setIslandGroup(String islandGroup) {
        this.islandGroup = islandGroup;
    }

    @RDF("dwc:locality")
    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    @RDF("dwc:locationAccordingTo")
    public String getLocationAccordingTo() {
        return locationAccordingTo;
    }

    public void setLocationAccordingTo(String locationAccordingTo) {
        this.locationAccordingTo = locationAccordingTo;
    }

    @RDF("dwc:locationRemarks")
    public String getLocationRemarks() {
        return locationRemarks;
    }

    public void setLocationRemarks(String locationRemarks) {
        this.locationRemarks = locationRemarks;
    }

    @RDF("dwc:maximumDepthInMeters")
    public String getMaximumDepthInMeters() {
        return maximumDepthInMeters;
    }

    public void setMaximumDepthInMeters(String maximumDepthInMeters) {
        this.maximumDepthInMeters = maximumDepthInMeters;
    }

    @RDF("dwc:maximumDistanceAboveSurfaceInMeters")
    public String getMaximumDistanceAboveSurfaceInMeters() {
        return maximumDistanceAboveSurfaceInMeters;
    }

    public void setMaximumDistanceAboveSurfaceInMeters(String maximumDistanceAboveSurfaceInMeters) {
        this.maximumDistanceAboveSurfaceInMeters = maximumDistanceAboveSurfaceInMeters;
    }

    @RDF("dwc:maximumElevationInMeters")
    public String getMaximumElevationInMeters() {
        return maximumElevationInMeters;
    }

    public void setMaximumElevationInMeters(String maximumElevationInMeters) {
        this.maximumElevationInMeters = maximumElevationInMeters;
    }

    @RDF("dwc:minimumDepthInMeters")
    public String getMinimumDepthInMeters() {
        return minimumDepthInMeters;
    }

    public void setMinimumDepthInMeters(String minimumDepthInMeters) {
        this.minimumDepthInMeters = minimumDepthInMeters;
    }

    @RDF("dwc:minimumDistanceAboveSurfaceInMeters")
    public String getMinimumDistanceAboveSurfaceInMeters() {
        return minimumDistanceAboveSurfaceInMeters;
    }

    public void setMinimumDistanceAboveSurfaceInMeters(String minimumDistanceAboveSurfaceInMeters) {
        this.minimumDistanceAboveSurfaceInMeters = minimumDistanceAboveSurfaceInMeters;
    }

    @RDF("dwc:minimumElevationInMeters")
    public String getMinimumElevationInMeters() {
        return minimumElevationInMeters;
    }

    public void setMinimumElevationInMeters(String minimumElevationInMeters) {
        this.minimumElevationInMeters = minimumElevationInMeters;
    }

    @RDF("dwc:municipality")
    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    @RDF("dwc:stateProvince")
    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    @RDF("dwc:verbatimCoordinates")
    public String getVerbatimCoordinates() {
        return verbatimCoordinates;
    }

    public void setVerbatimCoordinates(String verbatimCoordinates) {
        this.verbatimCoordinates = verbatimCoordinates;
    }

    @RDF("dwc:verbatimDepth")
    public String getVerbatimDepth() {
        return verbatimDepth;
    }

    public void setVerbatimDepth(String verbatimDepth) {
        this.verbatimDepth = verbatimDepth;
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

    @RDF("dwc:waterBody")
    public String getWaterBody() {
        return waterBody;
    }

    public void setWaterBody(String waterBody) {
        this.waterBody = waterBody;
    }
}
