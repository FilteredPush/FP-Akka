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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
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
        "event = " + Namespace.BASE_URI + "event/"
})
@RDFBean("dwcFP:Event")
public class Event implements Serializable {
    private String id;

    private String fieldNotes;
    private String fieldNumber;
    private String habitat;
    private String samplingEffort;
    private String samplingProtocol;
    private String eventDate;
    private String eventRemarks;
    private String eventTime;
    private String year;
    private String month;
    private String day;
    private String startOfYear;

    private Location locality;

    public Event() {
        id = UUID.randomUUID().toString();
    }

    @JsonIgnore
    @RDFSubject(prefix = "event:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("dwc:fieldNotes")
    public String getFieldNotes() {
        return fieldNotes;
    }

    public void setFieldNotes(String fieldNotes) {
        this.fieldNotes = fieldNotes;
    }

    @RDF("dwc:fieldNumber")
    public String getFieldNumber() {
        return fieldNumber;
    }

    public void setFieldNumber(String fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    @RDF("dwc:habitat")
    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    @RDF("dwc:samplingEffort")
    public String getSamplingEffort() {
        return samplingEffort;
    }

    public void setSamplingEffort(String samplingEffort) {
        this.samplingEffort = samplingEffort;
    }

    @RDF("dwc:samplingProtocol")
    public String getSamplingProtocol() {
        return samplingProtocol;
    }

    public void setSamplingProtocol(String samplingProtocol) {
        this.samplingProtocol = samplingProtocol;
    }

    @RDF("dwc:eventDate")
    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    @RDF("dwc:eventRemarks")
    public String getEventRemarks() {
        return eventRemarks;
    }

    public void setEventRemarks(String eventRemarks) {
        this.eventRemarks = eventRemarks;
    }

    @RDF("dwc:eventTime")
    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    @RDF("dwc:year")
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @RDF("dwc:month")
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    @RDF("dwc:day")
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    @RDF("dwc:startOfYear")
    public String getStartOfYear() {
        return startOfYear;
    }

    public void setStartOfYear(String startOfYear) {
        this.startOfYear = startOfYear;
    }

    @JsonUnwrapped
    @JsonProperty("location")
    @RDF("dwcFP:hasLocality")
    public Location getLocality() {
        return locality;
    }

    public void setLocality(Location locality) {
        this.locality = locality;
    }
}
