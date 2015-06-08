package org.filteredpush.akka.actors;

import akka.actor.*;
import org.filteredpush.kuration.services.GeoLocate2;
import org.filteredpush.kuration.interfaces.IGeoRefValidationService;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;
import org.filteredpush.kuration.util.CurationStatus;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.CurationCommentType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;

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

    public GEORefValidator(final String service, final boolean useCache, final double certainty, final int instances, final ActorRef listener) {
        this.listener = listener;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
                    @Override
                    public Actor create() throws Exception {
            return new GEORefValidatorInvocation(service, useCache, certainty, listener);
            }
            }).withRouter(new SmallestMailboxRouter(instances)), "workerRouter");
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

        /*
        //pass through the original token
        if (message instanceof TokenWithProv) {
            if (((TokenWithProv) message).getActorCreated().equals("MongoDBReader")) {
                listener.tell(message, getSelf());
            }
        }
        */
    }

    @Override
    public void postStop() {
        System.out.println("Stopped GeoRefValidator");
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }
    
    public final static double DEFAULT_CERTAINTY = 20; 

    private class GEORefValidatorInvocation extends UntypedActor {

        private boolean useCache;
        private final ActorRef listener;
        private String serviceClassQN;
       	private IGeoRefValidationService geoRefValidationService;
       	private double certainty; 	//the unit is km
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
                /*Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                                        ((TokenWithProv) message).getActorCreated(),
                                        ((TokenWithProv) message).getInvocCreated(),
                                        this.getClass().getSimpleName(),
                                        invoc,
                                        ((TokenWithProv) message).getTimeCreated(),
                                        System.currentTimeMillis());    */
            }

            if (message instanceof Token) {
                if (((Token) message).getData() instanceof SpecimenRecord) {
                    SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
                    //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());
                    Map<String,String> fields = new HashMap<String,String>();
                    for (String key : record.keySet()) {
                        fields.put(key,record.get(key));
                    }


                     //if missing, let it run, handle the error in service
                    String country = record.get("country");
                    String stateProvince = record.get("stateProvince");
                    String county = record.get("county");
                    String locality = record.get("locality");
                    /*
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
                     */
                    int isCoordinateMissing = 0;
                    String latitudeToken = record.get("decimalLatitude");
                    double latitude = -1;

                    if (latitudeToken != null && !latitudeToken.isEmpty()){
                        //if(!(latitudeToken instanceof ScalarToken)){
                        //    CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"latitudeLabel of the input is not of scalar type.",getName());
                        //    constructOutput(fields, curationComment);
                        //    return;
                        //}
                        try{
                            latitude = Double.valueOf(latitudeToken);
                        }catch (Exception e){
                            System.out.println("latitude token has issue: |" + latitudeToken + "|");
                        }
                    }else{
                        isCoordinateMissing++;
                    }

                    String longitudeToken = record.get("decimalLongitude");
                    double longitude = -1;
                    if (longitudeToken != null && !longitudeToken.isEmpty()) {
                        //if(!(longitudeToken instanceof ScalarToken)){
                        //CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"longitudeLabel of the input is not of scalar type.",getName());
                        //constructOutput(fields, curationComment);
                        //return;
                        //}
                        try{
                            longitude = Double.valueOf(longitudeToken);
                        }catch (Exception e){
                            System.out.println("longitude token has issue: |" + latitudeToken + "|");
                        }
                    } else {
                        isCoordinateMissing++;
                    }

                    //invoke the service to parse the locality and return the coordinates
                    //isCoordinateMissing == 2 means both longitude and latitude are missing
                    if(isCoordinateMissing == 2){
                        //geoRefValidationService.validateGeoRef(country, stateProvince, county, locality,null,null,certainty);
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY, "Both longitude and latitude are missing in the incoming SpecimenRecord", null);
                        constructOutput(new SpecimenRecord(fields),curationComment);
                        return;
                    }else{
                        geoRefValidationService.validateGeoRef(country, stateProvince, county, locality,String.valueOf(latitude),String.valueOf(longitude),certainty);
                    }

                    CurationStatus curationStatus = geoRefValidationService.getCurationStatus();
                    if(curationStatus == CurationComment.CURATED || curationStatus == CurationComment.Filled_in){
                        String originalLat = fields.get(SpecimenRecord.dwc_decimalLatitude);
                        String originalLng = fields.get(SpecimenRecord.dwc_decimalLongitude);
                        String newLat = String.valueOf(geoRefValidationService.getCorrectedLatitude());
                        String newLng = String.valueOf(geoRefValidationService.getCorrectedLongitude());

                        if(originalLat != null && originalLat.length() != 0 &&  !originalLat.equals(newLat)){
                            fields.put(SpecimenRecord.Original_Latitude_Label, originalLat);
                            fields.put(SpecimenRecord.dwc_decimalLatitude, newLat);
                        }
                        if(originalLng != null && originalLng.length() != 0 && !originalLng.equals(newLng)){
                            fields.put(SpecimenRecord.Original_Longitude_Label, originalLng);
                            fields.put(SpecimenRecord.dwc_decimalLongitude, newLng);
                        }
                    }
                    //output
                    //System.out.println("curationStatus = " + curationStatus.toString());
                    //System.out.println("curationComment = " + curationComment.toString());
                    CurationCommentType curationComment = CurationComment.construct(curationStatus,geoRefValidationService.getComment(),geoRefValidationService.getServiceName());
                    constructOutput(fields, curationComment);
                    for (List l : geoRefValidationService.getLog()) {
                        //Prov.log().printf("service\t%s\t%d\t%s\t%d\t%d\t%s\t%s\n", this.getClass().getSimpleName(), invoc, (String)l.get(0), (Long)l.get(1), (Long)l.get(2),l.get(3),curationStatus.toString());
                    }
                }
                //Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
            }
        }

        private void constructOutput(Map<String, String> result, CurationCommentType comment) {
            if (comment != null) {
                result.put(SpecimenRecord.geoRef_Status_Label,comment.getStatus());
            } else {
                result.put(SpecimenRecord.geoRef_Status_Label,CurationComment.CORRECT.toString());
            }
            result.put(SpecimenRecord.geoRef_Comment_Label,comment.getDetails());
            result.put(SpecimenRecord.geoRef_Source_Label,comment.getSource());
            SpecimenRecord r = new SpecimenRecord(result);
            Token token = new TokenWithProv<SpecimenRecord>(r,getName(),invoc);
            //System.err.println("georefend#"+result.get("oaiid").toString() + "#" + System.currentTimeMillis());
            listener.tell(token,getContext().parent());
        }
    }
}
