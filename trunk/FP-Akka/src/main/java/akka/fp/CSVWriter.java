package akka.fp;

import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import fp.util.SpecimenRecord;

import java.io.FileWriter;
import java.io.IOException;

/**
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:06
* To change this template use File | Settings | File Templates.
*/
public class CSVWriter extends UntypedActor {
    int cRecords = 0;
    int invoc = 0;
    private String _filePath = "/home/tianhong/test/data/out1000.csv";
    FileWriter writer;
    Boolean header = true;


    public CSVWriter(String filePath) {
        if (filePath != null) this._filePath = filePath;
        try {
            writer = new FileWriter(_filePath);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        invoc = 0;
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
                //System.out.print(b.toString());

            }
            try {
                convertObject(((Token) message).getData());
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

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
        System.out.println("Stopped Display");
        //System.out.println("Wrote " + cRecords + " records.");
        getContext().system().shutdown();
        super.postStop();
    }

    private void convertObject(Object o) throws IOException {
        //System.out.println("o.toString() = " + o.toString());
        if (header){
            for (String label  : ((SpecimenRecord)o).keySet()) {
                writer.append(label + ",");
            }
            writer.append("\n");
        }
        if (o instanceof SpecimenRecord) {
            for (String label  : ((SpecimenRecord)o).keySet()) {
                String t = ((SpecimenRecord)o).get(label);
                writer.append(t + ",");
            }
        } else {
            System.out.println(o.getClass().getName());
            writer.append(o.toString());
        }
        writer.append("\n");
        writer.flush();
    }
}
