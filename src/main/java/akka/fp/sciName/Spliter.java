package akka.fp.sciName;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import fp.util.SpecimenRecord;

import java.util.HashMap;
import java.util.List;

import org.filteredpush.akka.data.Token;

/**
 * Created by tianhong on 2/9/15.
 */
public class Spliter extends UntypedActor {

    final HashMap<ActorRef, List<String>> listeners;

    public Spliter(final HashMap<ActorRef, List<String>> listeners){
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

            for(ActorRef item : listeners.keySet()){
                SpecimenRecord sub = new SpecimenRecord();
                 for(String label : listeners.get(item)){
                     sub.put(label, record.get(label));
                 }
                item.tell(sub, getSelf());
            }
        }
    }


}
