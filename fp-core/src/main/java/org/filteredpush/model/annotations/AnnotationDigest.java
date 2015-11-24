package org.filteredpush.model.annotations;

import org.filteredpush.model.dwc.DwcTriplet;
import org.filteredpush.model.util.AnnotationUtil;

import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

/**
 * Created by lowery on 10/19/15.
 */
public class AnnotationDigest {
    private String uri;
    private String summary;
    private String annotator;
    private Date annotationDate;
    private AnnotationType annotationType;

    private DwcTriplet dwcTriplet;

    public AnnotationDigest(String uri, AnnotationType annotationType, String annotator, Date annotationDate, DwcTriplet dwcTriplet) {
        this.uri = uri;
        this.annotator = annotator;
        this.annotationDate = annotationDate;
        this.annotationType = annotationType;

        this.dwcTriplet = dwcTriplet;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAnnotator() {
        return annotator;
    }

    public void setAnnotator(String annotator) {
        this.annotator = annotator;
    }

    public Date getAnnotationDate() {
        return annotationDate;
    }

    public void setAnnotationDate(Date annotationDate) {
        this.annotationDate = annotationDate;
    }

    public AnnotationType getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(AnnotationType annotationType) {
        this.annotationType = annotationType;
    }

    public String getCollectionCode() {
        return dwcTriplet.getCollectionCode();
    }

    public void setCollectionCode(String collectionCode) {
        this.dwcTriplet.setCollectionCode(collectionCode);
    }

    public String getInstitutionCode() {
        return dwcTriplet.getInstitutionCode();
    }

    public void setInstitutionCode(String institutionCode) {
        this.dwcTriplet.setInstitutionCode(institutionCode);
    }

    public String getCatalogNumber() {
        return dwcTriplet.getCatalogNumber();
    }

    public void setCatalogNumber(String catalogNumber) {
        this.dwcTriplet.setCatalogNumber(catalogNumber);
    }
}
