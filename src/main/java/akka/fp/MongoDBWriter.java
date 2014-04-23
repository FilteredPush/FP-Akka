package akka.fp;

import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import fp.util.CurationCommentType;
import fp.util.SpecimenRecord;

import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:06
* To change this template use File | Settings | File Templates.
*/
public class MongoDBWriter extends UntypedActor {
    int cRecords = 0;
    int invoc = 0;
    //private final OutputStreamWriter ost;

    public MongoDBWriter(String mongodbHost, String mongodbDB, String mongodbCollection, String resultId, String fileEncoding) {
        //System.out.println("Accessing MongoDB ...");
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

            if (!_db.collectionExists(mongodbCollection)) {
                _collection = _db.createCollection(mongodbCollection + resultId, new BasicDBObject());
            } else {
                _collection = _db.getCollection(mongodbCollection + resultId);
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
        //ost = o;
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
        }
        if (message instanceof Token) {
            if (((Token) message).getData() instanceof SpecimenRecord) {
                SpecimenRecord rec = (SpecimenRecord)((Token) message).getData();
                StringBuffer b = new StringBuffer();
                b.append("\"");
                b.append(rec.get("id"));
                b.append("\"");
                if (rec.get("geoRefStatus") != null) {
                    b.append("\tGEORefValidator=");
                    b.append(rec.get("geoRefStatus"));
                }
                if (rec.get("scinStatus") != null) {
                    b.append("\tScientificNameValidator=");
                    b.append(rec.get("scinStatus"));
                }
                if (rec.get("flwtStatus") != null) {
                    b.append("\tFloweringTimeValidator=");
                    b.append(rec.get("flwtStatus"));
                }
                b.append("\t[");
                boolean first = true;
                for (String s : rec.keySet()) {
                    if (!(s.equals("geoRefStatus") ||
                            s.equals("scinStatus") ||
                            s.equals("flwtStatus") ||
                            s.equals("geoRefComment") ||
                            s.equals("scinComment") ||
                            s.equals("flwtComment") )) {
                        if (!first) {
                            b.append(" , ");
                        } else {
                            first = false;
                        }
                        b.append("\"");
                        b.append(s);
                        b.append("\" : \"");
                        b.append(rec.get(s));
                        b.append("\"");
                    }
                }
                b.append("]\n");

            }

            Object data = convertObject(((Token) message).getData());
            _collection.insert(((BasicDBObject)data));

            if (++cRecords % 100 == 0) {
                //System.out.println("Wrote " + cRecords + " records.");
            }
        } else if (message instanceof Broadcast) {
            getSelf().tell(((Broadcast) message).message(), getSender());
        } else {
            unhandled(message);
        }
        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
        invoc++;
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

    private MongoClient _mongoClient;
    private DB _db;
    private DBCollection _collection;
}
