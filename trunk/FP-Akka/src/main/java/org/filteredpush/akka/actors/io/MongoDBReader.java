/** 
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.filteredpush.kuration.util.SpecimenRecord;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.filteredpush.akka.data.Curate;
import org.filteredpush.akka.data.Prov;
import org.filteredpush.akka.data.ReadMore;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;

/**
 * Actor to read data from a mongodb database 
 * 
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 26.04.2013
 * Time: 16:41
 * 
 * @author cobalt
 * @author mole 
 *   
 */
public class MongoDBReader extends UntypedActor {

	private static final Log logger = LogFactory.getLog(MongoDBReader.class);
	
    private final ActorRef listener;

    private String mongoHost = "localhost";
    private String mongoDB = "db";
    private String mongoCollection = "Occurrence";
    private String mongoQuery = "{year:\"1898\"}";

    private DBCursor cursor = null;
    private int cRecords = 0;
    private int cValidRecords = 0;
    private int seen = 0;
    private long start; 
    
    /**
     * Report on load progress every reportSize records.
     */
    private int reportSize = 1000;
    int invoc;

    private static final long serialVersionUID = 1L;


    public MongoDBReader(String mongodbHost, String mongodbDB, String mongodbCollection, String mongodbQuery, ActorRef listener) {
        this.listener = listener;
        if (mongodbHost != null) this.mongoHost = mongodbHost;
        if (mongodbDB != null) this.mongoDB = mongodbDB;
        if (mongodbCollection != null) this.mongoCollection = mongodbCollection;
        if (mongodbQuery != null) this.mongoQuery = mongodbQuery;
        System.out.println("MongoDB Reader");
        System.out.println("Host: " + mongodbHost);
        System.out.println("DB: " + mongodbDB);
        System.out.println("Collection: " + mongodbCollection);
        System.out.println("Query: " + mongodbQuery);
        invoc = 0;
    }

    /**
	 * @return the reportSize
	 */
	public int getChunkSize() {
		return reportSize;
	}

	/**
	 * @param reportSize the reportSize to set
	 */
	public void setChunkSize(int chunkSize) {
		this.reportSize = chunkSize;
	}

    @Override
    public void onReceive(Object message) throws Exception {

    	logger.debug("MongoDBReader.onReceive()");
    	
    	if (message instanceof Curate) { 
    		start = System.currentTimeMillis();
    		System.out.println("Loading data from MongoDB ...");
    		try {
    			MongoClient mongoClient = new MongoClient(mongoHost);
    			//for (String dbn : mongoClient.getDatabaseNames()) {
    			//    System.out.println("   DB: " + dbn);
    			//}
    			DB db = mongoClient.getDB(mongoDB);
    			//for (String dbn : db.getCollectionNames()) {
    			//    System.out.println("   Collection: " + dbn);
    			//}
    			DBCollection coll = db.getCollection(mongoCollection);

    			if (!mongoQuery.isEmpty()) {
    				Object query = JSON.parse(mongoQuery);
    				cursor = coll.find((DBObject)query);
    			} else {
    				cursor = coll.find();
    			}
    			System.out.println(" Records to load: "+ cursor.count());
    		} catch (UnknownHostException e) {
    			e.printStackTrace();
    		}
    		int initialLoad = 30;
    		while (cursor.hasNext() && cValidRecords < initialLoad) {
    			seen = cursor.numSeen();
    			try {
    				readData();
    			} catch (SocketException se) {
    				System.out.println("Recovering...");
    				MongoClient mongoClient = new MongoClient(mongoHost);
    				DB db = mongoClient.getDB(mongoDB);
    				DBCollection coll = db.getCollection(mongoCollection);
    				if (!mongoQuery.isEmpty()) {
    					Object query = JSON.parse(mongoQuery);
    					cursor = coll.find((DBObject)query);
    				} else {
    					cursor = coll.find();
    				}
    				cursor.skip(seen);
    			}
    			if (cRecords % 100 == 0) {
    				//System.out.println("Read " + cValidRecords + " compatible from " + cRecords + " / " + totalRecords + " records.");
    			}
    		} 

    	} else if (message instanceof ReadMore) { 
    		if (cursor.hasNext()) { 
    			boolean gotOne = false;
    			while (!gotOne) { 
    				try { 
    					readData();
    					gotOne = true;
    				} catch (SocketException se) {
    					System.out.println("Recovering...");
    					MongoClient mongoClient = new MongoClient(mongoHost);
    					DB db = mongoClient.getDB(mongoDB);
    					DBCollection coll = db.getCollection(mongoCollection);
    					if (!mongoQuery.isEmpty()) {
    						Object query = JSON.parse(mongoQuery);
    						cursor = coll.find((DBObject)query);
    					} else {
    						cursor = coll.find();
    					}
    					cursor.skip(seen);
    				}
    			}
    		}
    	}
    	if (cursor != null && !cursor.hasNext()) { 
    		cursor.close(); 
    		getContext().stop(getSelf());
    		Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getName(),invoc,start,System.currentTimeMillis());
    	} 

    	invoc++;
    }

    private void readData() throws SocketException {
        if ( cursor == null || !cursor.hasNext()) return;

        DBObject dbo = cursor.next();
        cRecords++;

        SpecimenRecord out = new SpecimenRecord();
        for (String key : dbo.keySet()) {
            String fieldName = key;
            if (fieldName.startsWith("_")) fieldName = fieldName.substring(1);
            if (fieldName.contains(":")) fieldName = fieldName.replace(":","");

            Object o = dbo.get(key);
            out.put(fieldName,o.toString());
        }

        Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(out,this.getClass().getSimpleName(),invoc);

        ++cValidRecords;
        //System.err.println("read#"+out.get("oaiid").toString() + "#" + System.currentTimeMillis());
        //System.out.println("record:" + out.prettyPrint());
        if(cValidRecords % this.reportSize == 0) { 
        	System.out.println("cValidRecords = " + cValidRecords);
        }
        listener.tell(t,getSelf());
    }

    @Override
    public void postStop() {
        System.out.println("Read " + cValidRecords + " records");
        System.out.println("Stopped MongoDB Reader");
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
        super.postStop();
    }
        
}


