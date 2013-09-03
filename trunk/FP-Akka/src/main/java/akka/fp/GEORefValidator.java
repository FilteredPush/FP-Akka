package akka.fp;

import akka.actor.*;
import fp.services.GeoLocate2;
import fp.services.IGeoRefValidationService;
import fp.util.CurationComment;
import fp.util.CurationCommentType;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;
import fp.util.CurationStatus;
import fp.util.SpecimenRecord;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GEORefValidator extends UntypedActor {
    private final ActorRef listener;
    private final ActorRef workerRouter;

    public GEORefValidator(final String service, final boolean useCache, final double certainty, final ActorRef listener) {
        this.listener = listener;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
                    @Override
                    public Actor create() throws Exception {
            return new GEORefValidatorInvocation(service, useCache, certainty, listener);
            }
            }).withRouter(new SmallestMailboxRouter(4)), "workerRouter");
        getContext().watch(workerRouter);
	}


    public void onReceive(Object message) {
        if (message instanceof Token) {
            if (!getSender().equals(getSelf())) {
                workerRouter.tell(message, getSelf());
            } else {
                listener.tell(message, getSelf());
            }
        } else if (message instanceof Broadcast) {
            workerRouter.tell(new Broadcast(((Broadcast) message).message()), getSender());
        } else if (message instanceof Terminated) {
            if (((Terminated) message).getActor().equals(workerRouter))
                this.getContext().stop(getSelf());
        } else {
            unhandled(message);
        }
    }

    @Override
    public void postStop() {
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }

    private class GEORefValidatorInvocation extends UntypedActor {

        private boolean useCache;
        private final ActorRef listener;
        private String serviceClassQN;
       	private IGeoRefValidationService geoRefValidationService;
       	private double certainty; 	//the unit is km
       	private double defaultCertainty = 500;
        private int invoc;
        private final Random rand;

        private GEORefValidatorInvocation(final String service, final boolean useCache, final double certainty, final ActorRef listener) {
            this.serviceClassQN = service;
            this.useCache = useCache;
            this.certainty = certainty;
            this.listener = listener;
            this.rand = new Random();
            //resolve service
            try {
                geoRefValidationService = (IGeoRefValidationService)Class.forName(service).newInstance();
                geoRefValidationService.setUseCache(useCache);
            } catch (InstantiationException e) {
                geoRefValidationService = new GeoLocate2();
                geoRefValidationService.setUseCache(useCache);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                geoRefValidationService = new GeoLocate2();
                geoRefValidationService.setUseCache(useCache);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                geoRefValidationService = new GeoLocate2();
                geoRefValidationService.setUseCache(useCache);
                e.printStackTrace();
            }
        }

        public String getName() {
            return "GEORefValidator";
        }

        public void onReceive(Object message) {
            long start = System.currentTimeMillis();
            invoc = rand.nextInt();
            if (message instanceof TokenWithProv) {
                Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                                        ((TokenWithProv) message).getActorCreated(),
                                        ((TokenWithProv) message).getInvocCreated(),
                                        this.getClass().getSimpleName(),
                                        invoc,
                                        ((TokenWithProv) message).getTimeCreated(),
                                        System.currentTimeMillis());
            }

            if (message instanceof Token) {
                if (((Token) message).getData() instanceof SpecimenRecord) {
                    SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
                    Map<String,String> fields = new HashMap<String,String>();
                    for (String key : record.keySet()) {
                        fields.put(key,record.get(key));
                    }

                    //get the needed information from the input SpecimenRecord
                    String country = record.get("country");
                    if(country == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"country is missing in the input",getName());
                        constructOutput(fields,curationComment);
                        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
                        return;
                    }

                    String stateProvince = record.get("stateProvince");
                    if(stateProvince == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"stateProvinceLabel is missing in the input",getName());
                        constructOutput(fields, curationComment);
                        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
                        return;
                    }

                    String county = record.get("county");
                    if (county == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"countyLabel is missing in the input",getName());
                        constructOutput(fields, curationComment);
                        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
                        return;
                    }

                    String locality = record.get("locality");
                    if(locality == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"localityLabel is missing in the input",getName());
                        constructOutput(fields, curationComment);
                        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
                        return;
                    }

                    boolean isCoordinateMissing = false;
                    String latitudeToken = record.get("decimalLatitude");
                    double latitude = -1;
                    if (latitudeToken != null){
                        //if(!(latitudeToken instanceof ScalarToken)){
                        //    CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"latitudeLabel of the input is not of scalar type.",getName());
                        //    constructOutput(fields, curationComment);
                        //    return;
                        //}
                        latitude = Double.valueOf(latitudeToken);
                    }else{
                        isCoordinateMissing = true;
                    }

                    String longitudeToken = record.get("decimalLongitude");
                    double longitude = -1;
                    if (longitudeToken != null) {
                        //if(!(longitudeToken instanceof ScalarToken)){
                        //CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"longitudeLabel of the input is not of scalar type.",getName());
                        //constructOutput(fields, curationComment);
                        //return;
                        //}
                        longitude = Double.valueOf(longitudeToken);
                    } else {
                        isCoordinateMissing = true;
                    }

                    //invoke the service to parse the locality and return the coordinates
                    if(isCoordinateMissing){
                        geoRefValidationService.validateGeoRef(country, stateProvince, county, locality,null,null,certainty);
                    }else{
                        geoRefValidationService.validateGeoRef(country, stateProvince, county, locality,String.valueOf(latitude),String.valueOf(longitude),certainty);
                    }

                    CurationCommentType curationComment = null;
                    CurationStatus curationStatus = geoRefValidationService.getCurationStatus();
                    if(curationStatus == CurationComment.CURATED){
                        curationComment = CurationComment.construct(CurationComment.CURATED,geoRefValidationService.getComment(),geoRefValidationService.getServiceName());
                        fields.put("decimalLatitude", String.valueOf(geoRefValidationService.getCorrectedLatitude()));
                        fields.put("decimalLongitude", String.valueOf(geoRefValidationService.getCorrectedLongitude()));
                    }else if(curationStatus == CurationComment.UNABLE_CURATED){
                        curationComment = CurationComment.construct(CurationComment.UNABLE_CURATED,geoRefValidationService.getComment(),geoRefValidationService.getServiceName());
                    }else if(curationStatus == CurationComment.UNABLE_DETERMINE_VALIDITY){
                        curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,geoRefValidationService.getComment(),geoRefValidationService.getServiceName());
                    }

                    //output
                    constructOutput(fields, curationComment);
                }
                Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
            }
        }

        private void constructOutput(Map<String, String> result, CurationCommentType comment) {
            if (comment != null) {
                result.put("geoRefComment",comment.toString());
                result.put("geoRefStatus",comment.getStatus());
            } else {
                result.put("geoRefStatus",CurationComment.CORRECT.toString());
                result.put("geoRefComment","None");
            }
            SpecimenRecord r = new SpecimenRecord(result);
            Token token = new TokenWithProv<SpecimenRecord>(r,getName(),invoc);
            listener.tell(token,getContext().parent());
        }
    }
}
