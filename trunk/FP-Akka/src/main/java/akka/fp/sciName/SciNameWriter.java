package akka.fp.sciName;

import org.filteredpush.akka.data.Token;

import akka.actor.ActorRef;
import fp.util.SpecimenRecord;

/**
 * Created by tianhong on 2/9/15.
 */

//todo not in use

public class SciNameWriter extends Component {

    //SpecimenRecord inputData = new SpecimenRecord();
    String validName;
    boolean writeToFile;

    public SciNameWriter(boolean writeToFile, final ActorRef listener){
        super(listener);
        this.writeToFile = writeToFile;

    }

    @Override
    public void onReceive(Object message){

        if (((Token) message).getData() instanceof SpecimenRecord) {
            SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
            //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());

            if(!writeToFile){
                listener.tell(record, getSelf());
            }
            else{

            }
        }
    }

}
