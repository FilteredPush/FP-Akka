package org.filteredpush.akka.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.mongodb.BasicDBObject;

import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.CurationCommentType;
import org.filteredpush.kuration.util.SpecimenRecord;

import org.filteredpush.akka.data.Collection;
import org.filteredpush.akka.data.Token;
import org.filteredpush.ws.AnnotationData;
import org.filteredpush.ws.GeoreferenceData;
import org.filteredpush.ws.SolveWithMoreDataAnnotationData;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:06
* To change this template use File | Settings | File Templates.
*/
public class AnnotationInserter extends UntypedActor {
    int cRecords = 0;
    int invoc = 0;
    //private final OutputStreamWriter ost;

    public AnnotationInserter(final ActorRef listener) {
        this.listener = listener;
    }

    public void onReceive(Object message) {
        //long start = System.currentTimeMillis();

        if (message instanceof Token) {
            listener.tell(message,getSelf());

            if (((Token) message).getData() instanceof SpecimenRecord) {
                SpecimenRecord record = (SpecimenRecord)((Token) message).getData();
                Set<String> mataData = new HashSet<String>();

                if (record.get("geoRefStatus") != null) {
                    mataData.add("geoRef");
                }
                if (record.get("scinStatus") != null) {
                    mataData.add("scin");
                }
                if (record.get("flwtStatus") != null) {
                    mataData.add("flwt");
                }


            //TODO: how to deal with actors without comment
            //if(commentAnnotationToken!=null){
                //System.out.println("have comment: " + comment.toString());

                Iterator iterator = mataData.iterator();
                while(iterator.hasNext()){
                    String actorName = (String) iterator.next();
                    String status = record.get(actorName+"Status");
                    String comment = record.get(actorName+"comment");

                    AnnotationData annotationData = null;

                    if(!status.equals(CurationComment.CORRECT.toString())){
                        //there are two cases with different types of annotation
                        if(status.equals(CurationComment.CURATED.toString()) ||
                                status.equals(CurationComment.FILLED_IN.toString()) ){

                            GeoreferenceData georeferenceData = new GeoreferenceData();

                            georeferenceData.setEvidence(comment);

                            try {
                                georeferenceData.setDecimalLatitude(record.get("decimalLatitude").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setDecimalLongitude(record.get("decimalLongitude").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setScientificName(record.get("scientificName").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setScientificNameAuthorship(record.get("scientificNameAuthorship").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setTaxonRank(record.get("taxonRank").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setGenus(record.get("genus").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setSubgenus(record.get("subgenus").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setGeoreferenceProtocol(record.get("georeferenceProtocol").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setGeoreferenceRemarks(record.get("georeferenceRemarks").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setGeoreferenceSources(record.get("georeferenceSources").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                georeferenceData.setGeoreferenceVerificationStatus(record.get("georeferenceVerificationStatus").toString().replace("\"", ""));
                            } catch (Exception ignored) {}

                            annotationData = georeferenceData;
                        }
                        else if(status.equals(CurationComment.UNABLE_CURATED.toString()) ){    //||
                            //comment.getStatus().equals(CurationComment.UNABLE_DETERMINE_VALIDITY.toString())){

                            SolveWithMoreDataAnnotationData solveWithMoreDataAnnotationData = new SolveWithMoreDataAnnotationData();
                            solveWithMoreDataAnnotationData.setEvidence(comment);
                            solveWithMoreDataAnnotationData.setChars(comment);

                            annotationData = solveWithMoreDataAnnotationData;
                        }
                        /*
                        else{
                            return super.handleData(collectionManager, object, annotations);
                        }
                          */
                        try {
                            annotationData.setCatalogNumber(record.get("catalogNumber").toString().replace("\"", ""));
                        } catch (Exception ignored) {}
                        try {
                            annotationData.setInstitutionCode(record.get("institutionCode").toString().replace("\"", ""));
                        } catch (Exception ignored) {}
                        try {
                            annotationData.setCollectionCode(record.get("collectionCode").toString().replace("\"", ""));
                        } catch (Exception ignored) {}

                        
                        /*try {
							String annotationRdf = RdfUtil.serialize(annotation, Syntax.RdfXml);
							
	                        System.out.println("Annotation RDF/XML:");
	                        System.out.println();
	                        System.out.println(annotationRdf);
						} catch (RDFBeanException e) {
							e.printStackTrace();
						}*/

                        //Inject the annotation into the FPush stack in a FP Message
                       // boolean injectOrNot = ((BooleanToken)injectToFPush.getToken()).booleanValue();
                        if (injectOrNot){
                            try {
                                //injectAnnotation(annotation);
                            } catch (Exception e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    }
                }
            }
            else if (((Token) message).getData() instanceof CurationCommentType)  {
                //handle later
            }
        }
    }

    @Override
    public void postStop() {
        //System.out.println("Stopped Display");
        //System.out.println("Wrote " + cRecords + " records.");
        /*
        try {
            ost.flush();
            ost.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        getContext().system().shutdown();
    }

    private Object convertObject(Object o) {
        Object value = null;
        if (o instanceof SpecimenRecord) {
            BasicDBObject data = new BasicDBObject();
            for (String label  : ((SpecimenRecord)o).keySet()) {
                String t = ((SpecimenRecord)o).get(label);
                data.put(label, t);
            }
            value = data;
        } else if (o instanceof Collection) {
            BasicDBObject data = new BasicDBObject();
            // TODO
            //for (String label  : ((Collection)o).keySet()) {
            //    Token t = ((Collection) o).get(label);
            //    Object v = convertObject(t);
            //    data.put(label, v);
            //}
            value = data;
        } else if (o instanceof CurationCommentType) {
            value = new BasicDBObject();
            ((BasicDBObject)value).put("Source",((CurationCommentType) o).getSource());
            ((BasicDBObject)value).put("Details",((CurationCommentType) o).getDetails());
            ((BasicDBObject)value).put("Status",((CurationCommentType) o).getStatus());
        } else {
            System.out.println(o.getClass().getName());
            value = o.toString();
        }
        return value;
    }



    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    //private String mongodbHost = null;
    //private String mongodbDB = null;
    //private String mongodbCollection = null;
    //private String resultId;

    //private void injectAnnotation (Annotation annotation) throws Exception {
        // Create a new FPMessage and put the annotation rdf/xml into the FPMessage content

    //	ClientHelperService service = new ClientHelperService(ENDPOINT_HOST, ENDPOINT_PORT);
    //    String response = service.insertIdentification(annotation);
        
    //    System.out.println();
    //    System.out.println("Success: response " + response + " for annotation");
    //}

    //public final Parameter collectionScope;

    private static final String BASE_URI = "http://etaxonomy.org/ontologies/oa";
    private static String ENDPOINT_HOST = "localhost";
    private static int ENDPOINT_PORT = 8081;

    boolean injectOrNot = true;
    private final ActorRef listener;
}
