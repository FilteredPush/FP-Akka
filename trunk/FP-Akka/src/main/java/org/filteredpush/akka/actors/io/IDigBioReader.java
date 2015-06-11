/** A coactor that import collections represented in an XML file.
 *
 * Copyright (c) 2008 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package org.filteredpush.akka.actors.io;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.filteredpush.akka.data.Prov;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 26.04.2013
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */

public class IDigBioReader extends UntypedActor {

    private final ActorRef listener;
    private String limit;
    private int cValidRecords = 0;
    private int totalRecords = 0;
    int invoc;

    private static final long serialVersionUID = 1L;


    public IDigBioReader(String limit, String rq, ActorRef listener) {
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


