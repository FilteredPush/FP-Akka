package org.filteredpush.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 3.0.6
 * 2015-12-11T12:19:11.858-05:00
 * Generated source version: 3.0.6
 * 
 */
@WebService(targetNamespace = "http://filteredpush.org/ws", name = "AccessPointPort")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface AccessPointPort {

    @WebResult(name = "insertIdentificationResponse", targetNamespace = "http://filteredpush.org/ws", partName = "insertIdentificationResponse")
    @WebMethod
    public InsertIdentificationResponse insertIdentification(
        @WebParam(partName = "insertIdentificationRequest", name = "insertIdentificationRequest", targetNamespace = "http://filteredpush.org/ws")
        InsertIdentificationRequest insertIdentificationRequest
    );

    @WebResult(name = "solveWithMoreDataResponse", targetNamespace = "http://filteredpush.org/ws", partName = "solveWithMoreDataResponse")
    @WebMethod
    public SolveWithMoreDataResponse solveWithMoreData(
        @WebParam(partName = "solveWithMoreDataRequest", name = "solveWithMoreDataRequest", targetNamespace = "http://filteredpush.org/ws")
        SolveWithMoreDataRequest solveWithMoreDataRequest
    );

    @WebResult(name = "insertGeoreferenceResponse", targetNamespace = "http://filteredpush.org/ws", partName = "insertGeoreferenceResponse")
    @WebMethod
    public InsertGeoreferenceResponse insertGeoreference(
        @WebParam(partName = "insertGeoreferenceRequest", name = "insertGeoreferenceRequest", targetNamespace = "http://filteredpush.org/ws")
        InsertGeoreferenceRequest insertGeoreferenceRequest
    );

    @WebResult(name = "queryResponseAnnotationsResponse", targetNamespace = "http://filteredpush.org/ws", partName = "queryResponseAnnotationsResponse")
    @WebMethod
    public QueryResponseAnnotationsResponse queryResponseAnnotations(
        @WebParam(partName = "queryResponseAnnotationsRequest", name = "queryResponseAnnotationsRequest", targetNamespace = "http://filteredpush.org/ws")
        QueryResponseAnnotationsRequest queryResponseAnnotationsRequest
    );

    @WebResult(name = "insertResponseAnnotationResponse", targetNamespace = "http://filteredpush.org/ws", partName = "insertResponseAnnotationResponse")
    @WebMethod
    public InsertResponseAnnotationResponse insertResponseAnnotation(
        @WebParam(partName = "insertResponseAnnotationRequest", name = "insertResponseAnnotationRequest", targetNamespace = "http://filteredpush.org/ws")
        InsertResponseAnnotationRequest insertResponseAnnotationRequest
    );

    @WebResult(name = "queryAnnotationsResponse", targetNamespace = "http://filteredpush.org/ws", partName = "queryAnnotationsResponse")
    @WebMethod
    public QueryAnnotationsResponse queryAnnotations(
        @WebParam(partName = "queryAnnotationsRequest", name = "queryAnnotationsRequest", targetNamespace = "http://filteredpush.org/ws")
        QueryAnnotationsRequest queryAnnotationsRequest
    );
}
