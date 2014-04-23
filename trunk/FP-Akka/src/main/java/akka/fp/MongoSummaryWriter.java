package akka.fp;

import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import fp.util.CurationComment;
import fp.util.SpecimenRecord;
import fp.util.SpecimenRecordTypeConf;

import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;

/**
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:06
* To change this template use File | Settings | File Templates.
*/
public class MongoSummaryWriter extends UntypedActor {
    int cRecords = 0;
    int invoc = 0;
    //private final OutputStreamWriter ost;

    public MongoSummaryWriter(String mongodbHost, String mongodbDB, String mongodbCollection, String fileEncoding) {
        //System.out.println("MongoSummaryWriter created!!!!!!!!!!!");
        OutputStreamWriter o = null;
        try {
            //System.out.println(" Host: " + mongodbHost);
            _mongoClient = new MongoClient(mongodbHost);
            //for (String dbn : _mongoClient.getDatabaseNames()) {
            //    System.out.println("   DB: " + dbn);
            //}
            //System.out.println(" DB: " + mongodbDB);
            _db = _mongoClient.getDB(mongodbDB);
            //for (String dbn : _db.getCollectionNames()) {
            //    System.out.println("   Collection: " + dbn);
            //}

            String collectionName = mongodbCollection;
            if (!_db.collectionExists(collectionName)) {
                _collection = _db.createCollection(collectionName, new BasicDBObject());
            } else {
                System.out.println("collection exit");
                _collection = _db.getCollection(collectionName);
            }


            Charset enc;
            if (fileEncoding != null) {
                enc = Charset.forName(fileEncoding);
            } else {
                enc = Charset.forName("UTF-8");
            }
//            o = new OutputStreamWriter(new FileOutputStream(fileOut),enc);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        constructMaps();
    }

    public void onReceive(Object message) {
        long start = System.currentTimeMillis();
        if (message instanceof TokenWithProv) {
            Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                    ((TokenWithProv) message).getActorCreated(),
                    ((TokenWithProv) message).getInvocCreated(),
                    this.getClass().getSimpleName(),
                    invoc,
                    ((TokenWithProv) message).getTimeCreated(),
                    System.currentTimeMillis());

            /*
            //original record
            if (((TokenWithProv) message).getActorCreated().equals("MongoDBReader")) {
                SpecimenRecord originalRecord = (SpecimenRecord)((Token) message).getData();
                _OriginalRecordMap.put(originalRecord.get("id"), originalRecord);
            }                           */
            //validated record

            //assume the tokenwithprov is specimenrecord
            SpecimenRecord record = (SpecimenRecord)((Token) message).getData();
            Set<Map<String, String>> actorSet = new HashSet<Map<String, String>>();

            //System.out.println("inputSpecimenRecord = " + record.toString());

            //detect what actor has modified the record, check condition is temp now
            if (record.get("geoRefStatus") != null) {
                Map<String, String> actorStatusMap = new HashMap<String, String>();
                actorStatusMap.put("actor", "GeoRefValidator");
                actorStatusMap.put("status", record.get("geoRefStatus"));
                actorStatusMap.put("comment", record.get("geoRefComment"));
                actorStatusMap.put("source", record.get("geoRefSource"));
                actorSet.add(actorStatusMap);
            }
            if (record.get("scinStatus") != null) {
                Map<String, String> actorStatusMap = new HashMap<String, String>();
                actorStatusMap.put("actor", "ScientificNameValidator");
                actorStatusMap.put("status", record.get("scinStatus"));
                actorStatusMap.put("comment", record.get("scinComment"));
                actorStatusMap.put("source", record.get("scinSource"));
                actorSet.add(actorStatusMap);
            }
            if (record.get("flwtStatus") != null) {
                Map<String, String> actorStatusMap = new HashMap<String, String>();
                actorStatusMap.put("actor", "FloweringTimeValidator");
                actorStatusMap.put("status", record.get("flwtStatus"));
                actorStatusMap.put("comment", record.get("flwtComment"));
                actorStatusMap.put("source", record.get("flwtSource"));
                actorSet.add(actorStatusMap);
            }
            if (record.get("dateStatus") != null) {
                Map<String, String> actorStatusMap = new HashMap<String, String>();
                actorStatusMap.put("actor", "DateValidator");
                actorStatusMap.put("status", record.get("dateStatus"));
                actorStatusMap.put("comment", record.get("dateComment"));
                actorStatusMap.put("source", record.get("dateSource"));
                actorSet.add(actorStatusMap);
            }

            //start
            HashMap<String, String> markers = new HashMap<String, String>();    //Construct the appended field (markers) of summary

            HashSet<HashMap> detailSet = new HashSet<HashMap>();
            Iterator it =  actorSet.iterator();
            while (it.hasNext()){
                HashMap<String, String> eachActorStatusMap = (HashMap)it.next();

                //set the markers first
                String marker = null;
                if(eachActorStatusMap.get("status").equals(CurationComment.CORRECT.toString())){
                    marker = "TICK";
                }else if(eachActorStatusMap.get("status").equals(CurationComment.CURATED.toString()) ||
                        eachActorStatusMap.get("status").equals(CurationComment.Filled_in.toString())){
                    marker = "DELTA";
                }else if(eachActorStatusMap.get("status").equals(CurationComment.UNABLE_CURATED.toString())){
                    marker = "CROSS";
                }else if(eachActorStatusMap.get("status").equals(CurationComment.UNABLE_DETERMINE_VALIDITY.toString())){
                    marker = "QUESTION";  //highlight = "UNABLE_CURATE_FIELD";
                }else {
                    System.out.println(" start comment type is wrong");
                }

                markers.put(eachActorStatusMap.get("actor"), marker);
                HashSet<String> highlightLabels = highlightedLabelsMap.get(eachActorStatusMap.get("actor"));


                //todo: may have problem if original record arrives late
                detailSet.add(constructDetail(record, eachActorStatusMap, marker, highlightLabels));
                //highlights.put(record.get("actor"),highlight);
            }

            //calculate the overall marker, comparing the new one and the one in the record
            String wfmarker = "TICK";
            for (String lable : markers.keySet()){
                String mk = markers.get(lable);
                if (mk.equals("CROSS") || wfmarker.equals("CROSS")){
                    wfmarker = "CROSS";
                }else {
                    if (mk.equals("QUESTION") || wfmarker.equals("QUESTION")){
                        wfmarker = "QUESTION";
                    }else {
                        if (mk.equals("DELTA") && wfmarker.equals("DELTA")) {
                            wfmarker = "DELTA";
                        } else wfmarker = "TICK";
                    }
                }
            }

            markers.put(overallLabel, wfmarker);           //put the overall marker in the same map
            //_recordMarkersMap.put(record.get("id"), markers);
            //_recordDetailsMap.put(record.get("id"), detailSet);
            //_validatedRecordMap.put(record.get("id"), record);

            writeOut(record, markers, detailSet);



        }

        if (message instanceof Broadcast) {
            getSelf().tell(((Broadcast) message).message(), getSender());
        } else {
            unhandled(message);
        }
        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
        invoc++;
    }

    @Override
    public void postStop() {
       // for (String id : _validatedRecordMap.keySet()){

            /*
            BasicDBObject recordObject = new BasicDBObject();
            SpecimenRecord record =  _validatedRecordMap.get(id);
            for (String label : record.keySet()){
                recordObject.put(label, record.get(label));
            }
            BasicDBObject originalObject = new BasicDBObject();
            SpecimenRecord original =  _OriginalRecordMap.get(id);
            for (String label : original.keySet()){
                originalObject.put(label, original.get(label));
            }
            BasicDBObject markerObject = new BasicDBObject();
            HashMap<String, String> markersMap =  _recordMarkersMap.get(id);
            for (String label : markersMap.keySet()){
                markerObject.put(label, markersMap.get(label));
            }

            ArrayList detailEntry = new ArrayList();
            BasicDBObject detailObject = new BasicDBObject();
            HashSet detailMap =  _recordDetailsMap.get(id);
            Iterator itd = detailMap.iterator();
            while (itd.hasNext()){
                HashMap<String, String> eachDetailMap = (HashMap)itd.next();
                BasicDBObject eachDetailObject = new BasicDBObject();
                for (String label : eachDetailMap.keySet()){
                    detailObject.put(label, eachDetailMap.get(label));
                }
                detailEntry.add(eachDetailObject);
            }

            BasicDBObject data = new BasicDBObject("Record", recordObject).
                    append("Original", originalObject).
                    append("Markers", markerObject).
                    append("ActorDetails", detailEntry);

            BasicDBObject data = new BasicDBObject("Record", "t1").
                    append("Original", "t2").
                    append("Markers", "t3").
                    append("ActorDetails", "t4");

            System.out.println("start reading");
            _collection.insert(data);


            BasicDBObject data = new BasicDBObject("Record", _validatedRecordMap.get(id)).
            append("Original", _OriginalRecordMap.get(id)).
            append("Markers", _recordMarkersMap.get(id)).
            append("ActorDetails", _recordDetailsMap.get(id));
            _collection.insert(data);       */


        getContext().system().shutdown();
        super.postStop();
    }


    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private HashMap constructDetail(SpecimenRecord record, HashMap<String, String> actorDetail, String marker, HashSet highlights){
        HashMap<String, String> detailRecord = new HashMap<String, String>();
        //SpecimenRecord validatedRecord = _validatedRecordMap.get(record.get("id"));
        //todo:assume origianl record and validated record have the same arity

        detailRecord.put("Actor Name", actorDetail.get("actor"));
        detailRecord.put("Actor Run", "Tick") ;
        detailRecord.put("Comment", actorDetail.get("comment"));
        detailRecord.put("Source", actorDetail.get("source"));
        detailRecord.put("Actor Result", marker);


        for (String label : record.keySet()) {
            if (highlights.contains(label)){
                //four different cases have different content
                if (marker.equals("TICK")) detailRecord.put(label, "green: TICK");
                //no original record is available
                //else if (marker.equals("DELTA")) detailRecord.put(label, "yellow: WAS: " + record.get(label) + " CHANGED TO: "  + record.get("label"));
                else if (marker.equals("DELTA")) detailRecord.put(label, "yellow: CHANGED TO: "  + record.get("label"));
                else if (marker.equals("CROSS")) detailRecord.put(label, "red: UNABLE_TO_CURATE: " + record.get(label));
                else if (marker.equals("QUESTION")) detailRecord.put(label, "UNABLE_DETERMINE_VALIDITY_OF: " + record.get(label));
                else System.out.println("detail comment type is wrong");
            }else{
                detailRecord.put(label, "");
            }

        }
        return detailRecord;
    }

    private void writeOut(SpecimenRecord record, HashMap<String, String> markers, HashSet<HashMap> detailSet){

        BasicDBObject data = new BasicDBObject("Record", record).
                append("Markers", markers).
                append("ActorDetails", detailSet);
        _collection.insert(data);
    }

    private void constructMaps() {

        String latitudeLabel;
        String longitudeLabel;
        String floweringTimeLabel;
        String scientificNameLabel;
        String scientificNameAuthorLabel;
        String eventDateLabel;

        SpecimenRecordTypeConf specimenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

        scientificNameLabel = specimenRecordTypeConf.getLabel("ScientificName");
        if(scientificNameLabel == null) scientificNameLabel = "scientificName";

        scientificNameAuthorLabel = specimenRecordTypeConf.getLabel("ScientificNameAuthorship");
        if(scientificNameAuthorLabel == null) scientificNameLabel = "scientificNameAuthorship";

        floweringTimeLabel = specimenRecordTypeConf.getLabel("ReproductiveCondition");
        if(floweringTimeLabel == null) scientificNameLabel = "reproductiveCondition";

        longitudeLabel = specimenRecordTypeConf.getLabel("DecimalLongitude");
        if(longitudeLabel == null) scientificNameLabel = "decimalLongitude";

        latitudeLabel = specimenRecordTypeConf.getLabel("DecimalLatitude");
        if(latitudeLabel == null) scientificNameLabel = "decimalLatitude";

        eventDateLabel = specimenRecordTypeConf.getLabel("EventDate");
        if(eventDateLabel == null) scientificNameLabel = "eventDate";


        HashSet<String> fset = new HashSet<String>();
        fset.add(floweringTimeLabel);
        highlightedLabelsMap.put("FloweringTimeValidator", fset);

        HashSet<String> gset = new HashSet<String>();
        gset.add(latitudeLabel);
        gset.add(longitudeLabel);
        highlightedLabelsMap.put("GeoRefValidator", gset);

        HashSet<String> sset = new HashSet<String>();
        sset.add(scientificNameLabel);
        sset.add(scientificNameAuthorLabel);
        highlightedLabelsMap.put("ScientificNameValidator", sset);

        HashSet<String> dset = new HashSet<String>();
        dset.add(eventDateLabel);
        highlightedLabelsMap.put("DateValidator", dset);
    }

    private MongoClient _mongoClient;
    private DB _db;
    private DBCollection _collection;

    //private Map<String, SpecimenRecord> _OriginalRecordMap = new HashMap<String, SpecimenRecord>();
    //private Map<String, SpecimenRecord> _validatedRecordMap = new HashMap<String, SpecimenRecord>();
    //private Map<String, HashMap> _recordMarkersMap = new HashMap<String, HashMap>();
    private HashMap<String, HashSet> highlightedLabelsMap = new HashMap<String, HashSet>();
    //private HashMap<String, HashSet> _recordDetailsMap = new HashMap<String, HashSet>();

    private String overallLabel = "Kuration Workflow";
}
