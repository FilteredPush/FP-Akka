package akka.fp.sciName;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.filteredpush.kuration.util.SpecimenRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.filteredpush.akka.data.Token;

/**
 * Created by tianhong on 2/9/15.
 */
public abstract class Selector extends UntypedActor {

    final HashMap<String, ActorRef> listeners;

    public Selector( HashMap<String, ActorRef> listeners){
        this.listeners = listeners;
    }

    @Override
    public void onReceive(Object message){

        if (((Token) message).getData() instanceof SpecimenRecord) {
            SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
            //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());

            /*
            //if missing, let it run, handle the error in service
            String country = record.get("country");
            String stateProvince = record.get("stateProvince");
            String county = record.get("county");
            String locality = record.get("locality");
            */

            ActorRef listener = selectListener(listeners);
            listener.tell(record, getSelf());
        }
    }

    public abstract ActorRef selectListener(HashMap<String, ActorRef> listeners);
}
