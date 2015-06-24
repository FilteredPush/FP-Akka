package org.filteredpush.akka.actors.io;

import akka.actor.UntypedActor;
import akka.routing.Broadcast;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.filteredpush.kuration.util.SpecimenRecordTypeConf;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;

/**
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:06
*/
public class MongoSummaryWriter extends UntypedActor {
	
    int invoc = 0;
    private int reportSize = 1000;
    //private final OutputStreamWriter ost;

    private int validCount = 0;
    private MongoClient _mongoClient;
    private DB _db;
    private DBCollection _collection;
    private OutputStreamWriter _outputFile;
    private boolean outputToFile;
    private boolean firstRecord = true;

    //private Map<String, SpecimenRecord> _OriginalRecordMap = new HashMap<String, SpecimenRecord>();
    //private Map<String, SpecimenRecord> _validatedRecordMap = new HashMap<String, SpecimenRecord>();
    //private Map<String, HashMap> _recordMarkersMap = new HashMap<String, HashMap>();
    private HashMap<String, HashSet<String>> highlightedLabelsMap = new HashMap<String, HashSet<String>>();
    //private HashMap<String, HashSet> _recordDetailsMap = new HashMap<String, HashSet>();

    private String overallLabel = "Kuration Workflow";    
    
    public MongoSummaryWriter(String mongodbHost, String mongodbDB, String mongodbCollection, String fileEncoding) {
        //System.out.println("MongoSummaryWriter created!!!!!!!!!!!");
        outputToFile = false;
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
                //System.out.println("collection exit");
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

    public MongoSummaryWriter(String filePath){
        outputToFile = true;

        if (filePath == null) {
            System.out.println("no file path specified");
            //filePath = "./test.json";
        }

        try {
            //FileWriter file = new FileWriter("/home/tianhong/data/akka/test.json");
            _outputFile = new OutputStreamWriter(new FileOutputStream(new File(filePath)), "UTF-8");
            _outputFile.write("[");
        } catch (IOException e) {
            e.printStackTrace();
        }


        outputToFile = true;
        constructMaps();
    }

    public void onReceive(Object message) {
        long start = System.currentTimeMillis();
        if (message instanceof TokenWithProv) {
           /* Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                    ((TokenWithProv) message).getActorCreated(),
                    ((TokenWithProv) message).getInvocCreated(),
                    this.getClass().getSimpleName(),
                    invoc,
                    ((TokenWithProv) message).getTimeCreated(),
                    System.currentTimeMillis());     */

            /*
            //original record
            if (((TokenWithProv) message).getActorCreated().equals("MongoDBReader")) {
                SpecimenRecord originalRecord = (SpecimenRecord)((Token) message).getData();
                _OriginalRecordMap.put(originalRecord.get("id"), originalRecord);
            }                           */
            //validated record

            //assume the tokenwithprov is specimenrecord
            SpecimenRecord record = (SpecimenRecord)((Token) message).getData();
            //System.out.println("record.prettyPrint() = " + record.prettyPrint());

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
            if (record.get(SpecimenRecord.SciName_Status_Label) != null) {
                Map<String, String> actorStatusMap = new HashMap<String, String>();
                actorStatusMap.put("actor", "ScientificNameValidator");
                actorStatusMap.put("status", record.get(SpecimenRecord.SciName_Status_Label));
                actorStatusMap.put("comment", record.get(SpecimenRecord.SciName_Comment_Label));
                actorStatusMap.put("source", record.get(SpecimenRecord.SciName_Source_Label));
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
            if (record.get("borStatus") != null) {
                Map<String, String> actorStatusMap = new HashMap<String, String>();
                actorStatusMap.put("actor", "BasisOfRecordValidator");
                actorStatusMap.put("status", record.get("borStatus"));
                actorStatusMap.put("comment", record.get("borComment"));
                actorStatusMap.put("source", record.get("borSource"));
                actorSet.add(actorStatusMap);
            }            

            //remove a set of added fields in the record which have been read before
            HashSet<String> removeLables = new HashSet<String>();
            removeLables.add("geoRefStatus");removeLables.add("geoRefComment");removeLables.add("geoRefSource");
            removeLables.add(SpecimenRecord.SciName_Status_Label);removeLables.add(SpecimenRecord.SciName_Comment_Label);removeLables.add(SpecimenRecord.SciName_Source_Label);
            removeLables.add("flwtStatus");removeLables.add("flwtComment");removeLables.add("flwtSource");
            removeLables.add("dateStatus");removeLables.add("dateComment");removeLables.add("dateSource");
            removeLables.add("borStatus");removeLables.add("borComment");removeLables.add("borSource");
            for (String label:removeLables){
                if (record.keySet().contains(label)) record.remove(label);
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
                    marker = "CORRECT";
                }else if(eachActorStatusMap.get("status").equals(CurationComment.CURATED.toString())) {
                    marker = "CURATED";
                }else if(eachActorStatusMap.get("status").equals(CurationComment.FILLED_IN.toString())){
                    marker = "FILLED_IN";
                }else if(eachActorStatusMap.get("status").equals(CurationComment.UNABLE_CURATED.toString())){
                    marker = "UNABLE_CURATE";
                }else if(eachActorStatusMap.get("status").equals(CurationComment.UNABLE_DETERMINE_VALIDITY.toString())){
                    marker = "UNABLE_DETERMINE_VALIDITY";  //highlight = "UNABLE_CURATE_FIELD";
                }else {
                    System.out.println(" start comment type is wrong");
                }

                markers.put(eachActorStatusMap.get("actor"), marker);
                HashSet<String> highlightLabels = highlightedLabelsMap.get(eachActorStatusMap.get("actor"));


                //TODO: may have problem if original record arrives late
                detailSet.add(constructDetail(record, eachActorStatusMap, marker, highlightLabels));
                //highlights.put(record.get("actor"),highlight);
            }

            //calculate the overall marker, comparing the new one and the one in the record
            String wfmarker = "CORRECT";
            for (String lable : markers.keySet()){
                String mk = markers.get(lable);
                if (mk.equals("UNABLE_CURATE") || wfmarker.equals("UNABLE_CURATE")){
                    wfmarker = "UNABLE_CURATE";
                }else {
                    if (mk.equals("UNABLE_DETERMINE_VALIDITY") || wfmarker.equals("UNABLE_DETERMINE_VALIDITY")){
                        wfmarker = "UNABLE_DETERMINE_VALIDITY";
                    }else {
                        if (mk.equals("CURATED") && wfmarker.equals("CURATED")) {
                            wfmarker = "CURATED";
                        } else wfmarker = "CORRECT";
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
            //System.out.println("summaryWriter stopped");
            getSelf().tell(((Broadcast) message).message(), getSender());
        } else {
            unhandled(message);
        }
        //Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
        invoc++;
        
        if(validCount % reportSize == 0) { 
            System.out.println("Wrote " + reportSize + " records, total " + validCount);
       }
    }

    @Override
    public void postStop() {
        System.out.println("Stopped MongoSummaryWriter");

        if(outputToFile) {
            try {
                _outputFile.write("]");
                _outputFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Wrote out " + validCount + " records");
        getContext().system().shutdown();

        super.postStop();
    }


    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private HashMap constructDetail(SpecimenRecord record, HashMap<String, String> actorDetail, String marker, HashSet highlights){
        HashMap<String, Object> detailRecord = new HashMap<String, Object>();
        HashMap<String, String> validationState = new HashMap<String, String>();
        //SpecimenRecord validatedRecord = _validatedRecordMap.get(record.get("id"));
        //todo:assume origianl record and validated record have the same arity

        detailRecord.put("Actor Name", actorDetail.get("actor"));
        detailRecord.put("Comment", actorDetail.get("comment"));
        //get the original value
        HashMap<String, String> originMap = new HashMap<String, String>();

        /*//old code that put old valud in the source field
        if(source != null && source.contains("#")){
            //not sure how to handle the case source = "...:...#", i.e., no source case
            if(source.substring(source.length()-1,source.length()).equals("#"))  source += " ";
            String[] sub = source.split("#");
            for(int i = 0; i < sub.length-1; i++){
                //to handle null value
                String[] each = sub[i].split(":");
                try {
                    if (each.length > 1) originMap.put(each[0], each[1]);
                    else originMap.put(each[0], null);
                } catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("source = " + source);
                }
            }
            detailRecord.put("Source", sub[sub.length-1]);
        }else{
            detailRecord.put("Source", actorDetail.get("source"));
        }*/

        if(record.containsKey(SpecimenRecord.Original_SciName_Label)) originMap.put(SpecimenRecord.dwc_scientificName, record.get(SpecimenRecord.Original_SciName_Label));
        if(record.containsKey(SpecimenRecord.Original_Authorship_Label)) originMap.put(SpecimenRecord.dwc_scientificNameAuthorship, record.get(SpecimenRecord.Original_Authorship_Label));
        if(record.containsKey(SpecimenRecord.Original_EventDate_Label)) originMap.put(SpecimenRecord.dwc_eventDate, record.get(SpecimenRecord.Original_EventDate_Label));
        if(record.containsKey(SpecimenRecord.Original_Latitude_Label)) originMap.put(SpecimenRecord.dwc_decimalLatitude, record.get(SpecimenRecord.Original_Latitude_Label));
        if(record.containsKey(SpecimenRecord.Original_Longitude_Label)) originMap.put(SpecimenRecord.dwc_decimalLongitude, record.get(SpecimenRecord.Original_Longitude_Label));
        if(record.containsKey(SpecimenRecord.Original_BasisOfRecord_Label)) originMap.put(SpecimenRecord.dwc_basisOfRecord, record.get(SpecimenRecord.Original_BasisOfRecord_Label));

        detailRecord.put("Source", actorDetail.get("source"));
        detailRecord.put("Actor Result", marker);

        // TODO: Need to replace these magic strings with referenced constants.
        for (String label : record.keySet()) {
            if (highlights.contains(label)){
                validationState.put(label, marker);
                //four different cases have different content
                if (marker.equals("CORRECT")) detailRecord.put(label, "CORRECT: " + record.get(label));
                //no original record is available
                //else if (marker.equals("CURATED")) detailRecord.put(label, "yellow: WAS: " + record.get(label) + " CHANGED TO: "  + record.get("label"));
                else if (marker.equals("CURATED") || marker.equals("FILLED_IN")){
                    //System.out.println("source = " + actorDetail.get("source"));
                    //System.out.println("marker = " + marker);
                    //System.out.println("originMap = " + originMap);
                    //System.out.println("record = " + record);
                    //System.out.println("label = " + label);
                    //System.out.println("originMap(label) = " + originMap.get(label));
                    if (originMap.containsKey(label) && !originMap.get(label).equals(record.get(label))) {
                        detailRecord.put(label, "WAS: " + originMap.get(label) + "; CHANGED TO: " + record.get(label));
                    } else {
                        detailRecord.put(label, "CORRECT: " + record.get(label));
                    }
                }
                else if (marker.equals("UNABLE_CURATE")) detailRecord.put(label, "UNABLE_TO_CURATE: " + record.get(label));
                else if (marker.equals("UNABLE_DETERMINE_VALIDITY")) detailRecord.put(label, "UNABLE_DETERMINE_VALIDITY_OF: " + record.get(label));
                else System.out.println("detail comment ("+marker+") type is not known");
            }else{
                //detailRecord.put(label, "");
            }

        }

        detailRecord.put("ValidationState", validationState);
        return detailRecord;
    }

    private void writeOut(SpecimenRecord record, HashMap<String, String> markers, HashSet<HashMap> detailSet){

        //add validationStatus in record
        HashMap validationState = new HashMap<String, String>();
        for (HashMap item: detailSet){
            validationState.putAll((Map) item.get("ValidationState"));
        }

        //record.put("ValidationState", validationState);

        //BasicDBObject recordObject = new BasicDBObject("Record", record);

        HashMap<String, Object> modifiedRecord = new HashMap<String, Object>();
        for (String label : record.keySet()){
            modifiedRecord.put(label,record.get(label));
        }
        modifiedRecord.put("ValidationState", validationState);



        if(!outputToFile){
            BasicDBObject data = new BasicDBObject("Record", modifiedRecord).
                    append("Markers", markers).
                    append("ActorDetails", detailSet);
            _collection.insert(data);
            //System.err.println("writeout#"+modifiedRecord.get("oaiid").toString() + "#" + System.currentTimeMillis());
        }else{
            JSONObject obj = new JSONObject();
            obj.put("Record", modifiedRecord);
            obj.put("Markers", markers);

            JSONArray detailList = new JSONArray();
            for (HashMap item : detailSet){
                detailList.add(item);
            }
            obj.put("ActorDetails", detailList);

            try {
                //_outputFile.write("["+obj.toString()+"]");

                if(firstRecord){
                    firstRecord = false;
                }else{
                    _outputFile.write(",\n");
                }

                _outputFile.write(obj.toJSONString());

                _outputFile.flush();
                /*
                JSONObject obj1 = new JSONObject();
                obj.put("name", "mkyong.com");

                FileWriter file = new FileWriter("/home/tianhong/data/test2.json");
                file.write(obj1.toJSONString());
                file.flush();
                file.close();*/

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        validCount++;
    }

    private void constructMaps() {

        String latitudeLabel;
        String longitudeLabel;
        String floweringTimeLabel;
        String scientificNameLabel;
        String scientificNameAuthorLabel;
        String eventDateLabel;
        String basisOfRecordLabel;

        SpecimenRecordTypeConf specimenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

        scientificNameLabel = specimenRecordTypeConf.getLabel("ScientificName");
        if(scientificNameLabel == null) scientificNameLabel = "scientificName";

        // TODO: The repeated scientificNameLabel = ... statements below feel like a copy/paste error.
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

        basisOfRecordLabel = specimenRecordTypeConf.getLabel("BasisOfRecord");
        if(basisOfRecordLabel == null) scientificNameLabel = "basisOfRecord";


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
        
        HashSet<String> borset = new HashSet<String>();
        borset.add(basisOfRecordLabel);
        highlightedLabelsMap.put("BasisOfRecordValidator", borset);        
        
    }


}
