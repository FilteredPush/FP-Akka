package akka.fp.sciName;


import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by thsong on 5/5/15.
 */
public class ServiceSelector extends Selector {
    String serviceSwitch;

    public ServiceSelector(String serviceSwitch, HashMap<String, ActorRef> listeners){
        super(listeners);

        this.serviceSwitch = serviceSwitch;
    }

    @Override
    public ActorRef selectListener(HashMap<String, ActorRef> listeners) {

        if(serviceSwitch.equals("-t")) return listeners.get(serviceSwitch);
        else if(serviceSwitch.equals("-n")) return listeners.get(serviceSwitch);
        else System.out.println("invalid service switch: " + serviceSwitch);
        return null;
    }
}
