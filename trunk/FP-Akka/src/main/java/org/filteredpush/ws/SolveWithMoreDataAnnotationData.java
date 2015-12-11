
package org.filteredpush.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for solveWithMoreDataAnnotationData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="solveWithMoreDataAnnotationData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://filteredpush.org/ws}annotationData"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="chars" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "solveWithMoreDataAnnotationData", propOrder = {
    "chars"
})
public class SolveWithMoreDataAnnotationData
    extends AnnotationData
{

    @XmlElement(required = true)
    protected String chars;

    /**
     * Gets the value of the chars property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChars() {
        return chars;
    }

    /**
     * Sets the value of the chars property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChars(String value) {
        this.chars = value;
    }

}
