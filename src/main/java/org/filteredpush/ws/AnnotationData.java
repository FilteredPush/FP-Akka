
package org.filteredpush.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for annotationData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="annotationData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="annotatorName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="annotatorEmail" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="evidence" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="institutionCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="collectionCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="catalogNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "annotationData", propOrder = {
    "annotatorName",
    "annotatorEmail",
    "evidence",
    "institutionCode",
    "collectionCode",
    "catalogNumber"
})
@XmlSeeAlso({
    IdentificationData.class,
    GeoreferenceData.class,
    SolveWithMoreDataAnnotationData.class
})
public class AnnotationData {

    @XmlElement(required = true)
    protected String annotatorName;
    @XmlElement(required = true)
    protected String annotatorEmail;
    @XmlElement(required = true)
    protected String evidence;
    @XmlElement(required = true)
    protected String institutionCode;
    @XmlElement(required = true)
    protected String collectionCode;
    @XmlElement(required = true)
    protected String catalogNumber;

    /**
     * Gets the value of the annotatorName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnnotatorName() {
        return annotatorName;
    }

    /**
     * Sets the value of the annotatorName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnnotatorName(String value) {
        this.annotatorName = value;
    }

    /**
     * Gets the value of the annotatorEmail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnnotatorEmail() {
        return annotatorEmail;
    }

    /**
     * Sets the value of the annotatorEmail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnnotatorEmail(String value) {
        this.annotatorEmail = value;
    }

    /**
     * Gets the value of the evidence property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvidence() {
        return evidence;
    }

    /**
     * Sets the value of the evidence property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvidence(String value) {
        this.evidence = value;
    }

    /**
     * Gets the value of the institutionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstitutionCode() {
        return institutionCode;
    }

    /**
     * Sets the value of the institutionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstitutionCode(String value) {
        this.institutionCode = value;
    }

    /**
     * Gets the value of the collectionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionCode() {
        return collectionCode;
    }

    /**
     * Sets the value of the collectionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionCode(String value) {
        this.collectionCode = value;
    }

    /**
     * Gets the value of the catalogNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCatalogNumber() {
        return catalogNumber;
    }

    /**
     * Sets the value of the catalogNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCatalogNumber(String value) {
        this.catalogNumber = value;
    }

}
