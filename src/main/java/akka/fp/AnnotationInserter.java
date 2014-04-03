package akka.fp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.filteredpush.client.util.ClientHelperService;
import org.filteredpush.knowledge.rdf.RdfUtil;
import org.filteredpush.model.annotations.Agent;
import org.filteredpush.model.annotations.Annotation;
import org.filteredpush.model.annotations.ContentAsText;
import org.filteredpush.model.annotations.Evidence;
import org.filteredpush.model.annotations.Expectation;
import org.filteredpush.model.annotations.Selector;
import org.filteredpush.model.annotations.SpecificResource;
import org.filteredpush.model.dwc.Georeference;
import org.ontoware.rdf2go.model.Syntax;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.mongodb.BasicDBObject;
import com.viceversatech.rdfbeans.exceptions.RDFBeanException;

import fp.util.CurationComment;
import fp.util.CurationCommentType;
import fp.util.SpecimenRecord;

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


                    if(!status.equals(CurationComment.CORRECT.toString())){

                        // First we construct an annotation from the following POJOs
                        Annotation annotation = new Annotation();
                        SpecificResource target = new SpecificResource();
                        Selector selector = new Selector();
                        //Source source = new Source();
                        Expectation expectation;
                        Evidence evidence = new Evidence();
                        Agent annotator = new Agent();
                        //Agent georefBy = new Agent();
                        //Generator generator = new Generator();
                        String configurationFile = null;

                        annotator.setName("Kepler Workflow System");
                        //annotator.setMbox_sha1sum("043856881f6b4d6c87b89a0da68c96b460d07787");
                        annotation.setAnnotator(annotator);

                        //georefBy.setName("Paul J. Morris");

                        //there are two cases with different types of annotation
                        if(status.equals(CurationComment.CURATED.toString()) ||
                                status.equals(CurationComment.Filled_in.toString()) ){

                            //distinguish two types of curated status: update or insert
                            if (status.equals(CurationComment.CURATED.toString())) {
                                expectation = Expectation.UPDATE;
                            }
                            else{
                                expectation = Expectation.INSERT;
                            }
                            Georeference body = new Georeference();
                            
                            annotation.setExpectation(expectation);
                            evidence.setChars(comment);
                            annotation.setEvidence(evidence);

                            //body.setCoordinatePrecision("9");
                            //body.setCoordinateUncertaintyInMeters("281");
                            // body.setGeodeticDatum("WGS84");
                            try {
                                body.setDecimalLatitude(record.get("decimalLatitude").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setDecimalLongitude(record.get("decimalLongitude").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setScientificName(record.get("scientificName").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setScientificNameAuthorship(record.get("scientificNameAuthorship").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setTaxonRank(record.get("taxonRank").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setGenus(record.get("genus").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setSubgenus(record.get("subgenus").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setGeoreferenceProtocol(record.get("georeferenceProtocol").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setGeoreferenceRemarks(record.get("georeferenceRemarks").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setGeoreferenceSources(record.get("georeferenceSources").toString().replace("\"", ""));
                            } catch (Exception ignored) {}
                            try {
                                body.setGeoreferenceVerificationStatus(record.get("georeferenceVerificationStatus").toString().replace("\"", ""));
                            } catch (Exception ignored) {}


                            //body.setFootprintSRS("4092");
                            //body.setFootprintSpatialFit("footprintSpatialFit");
                            //body.setFootprintWKT("footpringWKT");
                            //body.setGeoreferenceProtocol("Biogeomancer, Point Radius");
                            //body.setGeoreferenceRemarks("Locality on shoreline, reached on foot off of dirt road.  Geoferenced from PLSS coordinate and memory of locality, P.J.Morris, 2012.");
                            //body.setGeoreferenceSources("Schopf, K., Morris, P.. 1994. \"Description of a Muscle Scar and two other novel features from steinkerns of Hypomphalocirrus (Mollusca: Paragastropoda)\" Jour.Paleontology, 68(1):47-58");
                            //body.setGeoreferenceVerificationStatus("verified by collector");
                            //body.setGeoreferencedBy(georefBy);
                            annotation.setBody(body);

                            configurationFile = "georeference.xml";
                        }
                        else if(status.equals(CurationComment.UNABLE_CURATED.toString()) ){    //||
                            //comment.getStatus().equals(CurationComment.UNABLE_DETERMINE_VALIDITY.toString())){

                        	ContentAsText body = new ContentAsText();
                            expectation = Expectation.SOLVE_WITH_MORE_DATA;
                            annotation.setExpectation(expectation);

                            evidence.setChars(comment);
                            annotation.setEvidence(evidence);

                            body.setChars(comment);
                            annotation.setBody(body);

                            configurationFile = "solve_with_more_data.xml";
                        }
                        /*
                        else{
                            return super.handleData(collectionManager, object, annotations);
                        }
                          */
                        try {
                            selector.setCatalogNumber(record.get("catalogNumber").toString().replace("\"", ""));
                        } catch (Exception ignored) {}
                        try {
                            selector.setInstitutionCode(record.get("institutionCode").toString().replace("\"", ""));
                        } catch (Exception ignored) {}
                        try {
                            selector.setCollectionCode(record.get("collectionCode").toString().replace("\"", ""));
                        } catch (Exception ignored) {}

                        //target.setHasSource(source);
                        target.setSelector(selector);
                        annotation.setTarget(target);

                        //evidence.setChars(comment.getDetails());
                        //annotation.setHasEvidence(evidence);

                        //annotation.setGenerator(generator);

                        
                        try {
							String annotationRdf = RdfUtil.serialize(annotation, Syntax.RdfXml);
							
	                        System.out.println("Annotation RDF/XML:");
	                        System.out.println();
	                        System.out.println(annotationRdf);
						} catch (RDFBeanException e) {
							e.printStackTrace();
						}

                        //Inject the annotation into the FPush stack in a FP Message
                       // boolean injectOrNot = ((BooleanToken)injectToFPush.getToken()).booleanValue();
                        if (injectOrNot){
                            try {
                                injectAnnotation(annotation);
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
        super.postStop();
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

    private void injectAnnotation (Annotation annotation) throws Exception {
        // Create a new FPMessage and put the annotation rdf/xml into the FPMessage content

    	ClientHelperService service = new ClientHelperService(ENDPOINT_HOST, ENDPOINT_PORT);
        String response = service.insertIdentification(annotation);
        
        System.out.println();
        System.out.println("Success: response " + response + " for annotation");
    }

    //public final Parameter collectionScope;

    private static final String BASE_URI = "http://etaxonomy.org/ontologies/oa";
    private static String ENDPOINT_HOST = "localhost";
    private static int ENDPOINT_PORT = 8082;

    boolean injectOrNot = true;
    private final ActorRef listener;
}
