package akka.fp.sciName;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.fp.Token;
import fp.util.SpecimenRecord;

/**
 * Created by tianhong on 2/9/15.
 */
public class Merger extends UntypedActor {

    SpecimenRecord inputData = new SpecimenRecord();
    final ActorRef listener;
    final int numInputPorts;

    int portCounter = 0;  //for counting all the input ports are arrived

    public Merger(int numPorts, final ActorRef listener){
        this.numInputPorts = numPorts;
        this.listener = listener;
    }

    @Override
    public void onReceive(Object message){

        if (((Token) message).getData() instanceof SpecimenRecord) {
            SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
            //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());

            inputData.putAll(record);

            /*
            //if missing, let it run, handle the error in service
            String country = record.get("country");
            String stateProvince = record.get("stateProvince");
            String county = record.get("county");
            String locality = record.get("locality");
            */


            if(portCounter == numInputPorts) listener.tell(inputData, getSelf());
        }
    }


}
