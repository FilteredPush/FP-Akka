package akka.fp;

import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import com.csvreader.CsvWriter;
import fp.util.SpecimenRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private String _filePath = "/home/tianhong/test/data/test.csv";
    CsvWriter csvOutput;
    Boolean headerWritten = false;
    List<String> headers = new ArrayList<String>();
    //todo: make it more flexible

    public CSVWriter(String filePath) {
        if (filePath != null) this._filePath = filePath;
        try {
            System.out.println("filePath = " + filePath);
            csvOutput = new CsvWriter(new FileWriter(_filePath, true), ',');
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
            Object o = ((Token) message).getData();

            try {
                if (!headerWritten) {
                    //write header first
                    //use headers list to keep track of the order
                    for (String label  : ((SpecimenRecord)o).keySet()) {

                        csvOutput.write(label);
                        headers.add(label);
                    }
                    csvOutput.endRecord();
                }

                //write the values
                for (String header : headers){
                    //System.out.println("asfeafadf" + ((SpecimenRecord)o).get(header));
                    csvOutput.write(((SpecimenRecord)o).get(header));
                }
                csvOutput.endRecord();

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
        csvOutput.close();
        getContext().system().shutdown();
        super.postStop();
    }

}
