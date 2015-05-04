package akka.fp.sciName;

import akka.actor.*;
import fp.util.SpecimenRecord;

import java.util.HashMap;

/**
 * Created by tianhong on 2/10/15.
 */
public class SciNameWorkflow {

    ActorRef listener;

    HashMap<Integer, SpecimenRecord> dataStore = new HashMap<Integer, SpecimenRecord>();
    public SciNameWorkflow (SpecimenRecord input, ActorRef listener){

        ActorSystem system = ActorSystem.create("SciNameWorkflow");
        this.listener = listener;

        final ActorRef serviceCall = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new NameReconciliation(null);    //(2,null)
            }
        }), "serviceCall");

        final ActorRef nr = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new NameReconciliation(serviceCall);
            }
        }), "NameReconciliation");

        /*
        final ActorRef me1 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new Merger(2, nr);
            }
        }), "MongoDBWriter");
         */

        final ActorRef cni = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new checkNameInconsistency(nr);
            }
        }), "checkNameInconsistency");

        /*
        //construct the output ports of spliter1
        HashMap<ActorRef, List<String>> sp1Ports = new HashMap<ActorRef, List<String>>();
        List<String> cniInputs = new ArrayList<String>();
        cniInputs.add("scientificName");
        cniInputs.add("genus");
        cniInputs.add("subgenus");
        cniInputs.add("specificEpithet");
        cniInputs.add("verbatimTaxonRank");
        cniInputs.add("taxonRank");
        cniInputs.add("infraspecificEpithet");
        sp1Ports.put(cni, cniInputs);
        List<String> nrInputs = new ArrayList<String>();
        nrInputs.add("scientificNameAuthorship");
        sp1Ports.put(me1, nrInputs);
        Spliter sp1=new Spliter(sp1Ports);
        */
    }
}
