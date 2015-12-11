
package org.filteredpush.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for georeferenceData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="georeferenceData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://filteredpush.org/ws}annotationData"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="decimalLatitude" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="decimalLongitude" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="scientificName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="scientificNameAuthorship" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="taxonRank" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="genus" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="subgenus" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="georeferenceProtocol" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="georeferenceRemarks" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="georeferenceSources" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="georeferenceVerificationStatus" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "georeferenceData", propOrder = {
    "decimalLatitude",
    "decimalLongitude",
    "scientificName",
    "scientificNameAuthorship",
    "taxonRank",
    "genus",
    "subgenus",
    "georeferenceProtocol",
    "georeferenceRemarks",
    "georeferenceSources",
    "georeferenceVerificationStatus"
})
public class GeoreferenceData
    extends AnnotationData
{

    @XmlElement(required = true)
    protected String decimalLatitude;
    @XmlElement(required = true)
    protected String decimalLongitude;
    @XmlElement(required = true)
    protected String scientificName;
    @XmlElement(required = true)
    protected String scientificNameAuthorship;
    @XmlElement(required = true)
    protected String taxonRank;
    @XmlElement(required = true)
    protected String genus;
    @XmlElement(required = true)
    protected String subgenus;
    @XmlElement(required = true)
    protected String georeferenceProtocol;
    @XmlElement(required = true)
    protected String georeferenceRemarks;
    @XmlElement(required = true)
    protected String georeferenceSources;
    @XmlElement(required = true)
    protected String georeferenceVerificationStatus;

    /**
     * Gets the value of the decimalLatitude property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDecimalLatitude() {
        return decimalLatitude;
    }

    /**
     * Sets the value of the decimalLatitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDecimalLatitude(String value) {
        this.decimalLatitude = value;
    }

    /**
     * Gets the value of the decimalLongitude property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDecimalLongitude() {
        return decimalLongitude;
    }

    /**
     * Sets the value of the decimalLongitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDecimalLongitude(String value) {
        this.decimalLongitude = value;
    }

    /**
     * Gets the value of the scientificName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScientificName() {
        return scientificName;
    }

    /**
     * Sets the value of the scientificName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScientificName(String value) {
        this.scientificName = value;
    }

    /**
     * Gets the value of the scientificNameAuthorship property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScientificNameAuthorship() {
        return scientificNameAuthorship;
    }

    /**
     * Sets the value of the scientificNameAuthorship property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScientificNameAuthorship(String value) {
        this.scientificNameAuthorship = value;
    }

    /**
     * Gets the value of the taxonRank property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxonRank() {
        return taxonRank;
    }

    /**
     * Sets the value of the taxonRank property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxonRank(String value) {
        this.taxonRank = value;
    }

    /**
     * Gets the value of the genus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGenus() {
        return genus;
    }

    /**
     * Sets the value of the genus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGenus(String value) {
        this.genus = value;
    }

    /**
     * Gets the value of the subgenus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubgenus() {
        return subgenus;
    }

    /**
     * Sets the value of the subgenus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubgenus(String value) {
        this.subgenus = value;
    }

    /**
     * Gets the value of the georeferenceProtocol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeoreferenceProtocol() {
        return georeferenceProtocol;
    }

    /**
     * Sets the value of the georeferenceProtocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeoreferenceProtocol(String value) {
        this.georeferenceProtocol = value;
    }

    /**
     * Gets the value of the georeferenceRemarks property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeoreferenceRemarks() {
        return georeferenceRemarks;
    }

    /**
     * Sets the value of the georeferenceRemarks property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeoreferenceRemarks(String value) {
        this.georeferenceRemarks = value;
    }

    /**
     * Gets the value of the georeferenceSources property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeoreferenceSources() {
        return georeferenceSources;
    }

    /**
     * Sets the value of the georeferenceSources property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeoreferenceSources(String value) {
        this.georeferenceSources = value;
    }

    /**
     * Gets the value of the georeferenceVerificationStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeoreferenceVerificationStatus() {
        return georeferenceVerificationStatus;
    }

    /**
     * Sets the value of the georeferenceVerificationStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeoreferenceVerificationStatus(String value) {
        this.georeferenceVerificationStatus = value;
    }

}
