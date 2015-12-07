/**
 * IDigBioReader.java
 * 
 * Copyright 2015 President and Fellows of Harvard College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.filteredpush.akka.actors.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;

import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * FP-Akka actor to read records as input from iDigBio's api.
 * 
 * @author Tianhong Song
 */
public class IDigBioReader extends UntypedActor {

    private final ActorRef listener;
    private int limit;
    private int cValidRecords = 0;
    private int totalRecords = 0;
    int invoc;

    private static final long serialVersionUID = 1L;


    public IDigBioReader(int limit, String rq, ActorRef listener) {
        this.listener = listener;
        this.limit = limit;
    }

    @Override
    public void onReceive(Object o) throws Exception {
         /*
        Boolean test = true;
        while (test) {
            if (o instanceof String) {
                System.out.println("o = " + o.toString());
                test = false;
            }
        }
        */

        readData();

    }

    public void readData(){

        System.out.println("Parameters: limit = " + limit);

        StringBuilder result = new StringBuilder();

        URL url;
        try {
            url = new URL("http://beta-search.idigbio.org/v2/search/records/?limit=" + limit);
            //url = new URL("http://beta-search.idigbio.org/v2/search/records/" + "?limit=5")

            URLConnection connection = url.openConnection();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null) {
                result.append(line);
                //System.out.println("line = " + line);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject(result.toString());
        JSONArray rawRecordSet = (JSONArray)json.get("items");//.getJSONArray("item");

        for(int index = 0; index < rawRecordSet.length(); index++){
            //JSONObject rawRecordObject = (JSONObject)rawRecordSet.get(index);

            JSONObject rawRecord = (JSONObject) ((JSONObject)rawRecordSet.get(index)).get("data");
            SpecimenRecord record = new SpecimenRecord();

            for(Object key : rawRecord.keySet()){
                //record.put()
                String label = key.toString();
                if(label.contains(":")) record.put(label.split(":")[1], (String) rawRecord.get(label));
                else record.put(label, (String)rawRecord.get(label));
            }


            Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(record,this.getClass().getSimpleName(),invoc);
            ++cValidRecords;
            //System.err.println("read#"+out.get("oaiid").toString() + "#" + System.currentTimeMillis());
            //System.out.println("record:" + out.prettyPrint());
            if(cValidRecords%10000 == 0) System.out.println("cValidRecords = " + cValidRecords);
            //System.out.println("cValidRecords = " + cValidRecords);
            listener.tell(t,getSelf());
        }
        /*
        SpecimenRecord record = new SpecimenRecord();
        for(Object key : rawRecord.keySet()){
            //record.put()
            String label = key.toString();
            record.put(label.split(":")[1], (String)rawRecord.get(label));
        }
        */

        //Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(record,this.getClass().getSimpleName(),invoc);
    }
 

    @Override
    public void postStop() {
        System.out.println("Read " + cValidRecords + " records");
        System.out.println("Stopped iDigBio Reader");
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
        super.postStop();
    }
}


