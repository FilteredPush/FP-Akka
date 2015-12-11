
package org.filteredpush.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for responseAnnotationData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="responseAnnotationData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="annotatorName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="annotatorEmail" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="describesObject" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *         &lt;element name="polarity" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="resolution" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="opinion" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseAnnotationData", propOrder = {
    "annotatorName",
    "annotatorEmail",
    "describesObject",
    "polarity",
    "resolution",
    "opinion"
})
public class ResponseAnnotationData {

    @XmlElement(required = true)
    protected String annotatorName;
    @XmlElement(required = true)
    protected String annotatorEmail;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String describesObject;
    @XmlElement(required = true)
    protected String polarity;
    @XmlElement(required = true)
    protected String resolution;
    @XmlElement(required = true)
    protected String opinion;

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
     * Gets the value of the describesObject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescribesObject() {
        return describesObject;
    }

    /**
     * Sets the value of the describesObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescribesObject(String value) {
        this.describesObject = value;
    }

    /**
     * Gets the value of the polarity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolarity() {
        return polarity;
    }

    /**
     * Sets the value of the polarity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolarity(String value) {
        this.polarity = value;
    }

    /**
     * Gets the value of the resolution property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * Sets the value of the resolution property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResolution(String value) {
        this.resolution = value;
    }

    /**
     * Gets the value of the opinion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpinion() {
        return opinion;
    }

    /**
     * Sets the value of the opinion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpinion(String value) {
        this.opinion = value;
    }

}
