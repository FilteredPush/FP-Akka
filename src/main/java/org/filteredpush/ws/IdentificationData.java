
package org.filteredpush.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for identificationData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="identificationData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://filteredpush.org/ws}annotationData"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="scientificName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="scientificNameAuthorship" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="identifiedBy" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateIdentified" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identificationData", propOrder = {
    "scientificName",
    "scientificNameAuthorship",
    "identifiedBy",
    "dateIdentified"
})
public class IdentificationData
    extends AnnotationData
{

    @XmlElement(required = true)
    protected String scientificName;
    @XmlElement(required = true)
    protected String scientificNameAuthorship;
    @XmlElement(required = true)
    protected String identifiedBy;
    @XmlElement(required = true)
    protected String dateIdentified;

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
     * Gets the value of the identifiedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentifiedBy() {
        return identifiedBy;
    }

    /**
     * Sets the value of the identifiedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentifiedBy(String value) {
        this.identifiedBy = value;
    }

    /**
     * Gets the value of the dateIdentified property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateIdentified() {
        return dateIdentified;
    }

    /**
     * Sets the value of the dateIdentified property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateIdentified(String value) {
        this.dateIdentified = value;
    }

}
