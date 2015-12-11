
package org.filteredpush.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="solveWithMoreDataAnnotationData" type="{http://filteredpush.org/ws}solveWithMoreDataAnnotationData"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "solveWithMoreDataAnnotationData"
})
@XmlRootElement(name = "solveWithMoreDataRequest")
public class SolveWithMoreDataRequest {

    @XmlElement(required = true)
    protected SolveWithMoreDataAnnotationData solveWithMoreDataAnnotationData;

    /**
     * Gets the value of the solveWithMoreDataAnnotationData property.
     * 
     * @return
     *     possible object is
     *     {@link SolveWithMoreDataAnnotationData }
     *     
     */
    public SolveWithMoreDataAnnotationData getSolveWithMoreDataAnnotationData() {
        return solveWithMoreDataAnnotationData;
    }

    /**
     * Sets the value of the solveWithMoreDataAnnotationData property.
     * 
     * @param value
     *     allowed object is
     *     {@link SolveWithMoreDataAnnotationData }
     *     
     */
    public void setSolveWithMoreDataAnnotationData(SolveWithMoreDataAnnotationData value) {
        this.solveWithMoreDataAnnotationData = value;
    }

}
