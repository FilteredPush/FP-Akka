package akka.fp.sciName;

import akka.actor.*;
import akka.fp.Token;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;
import com.sun.accessibility.internal.resources.accessibility_zh_TW;
import fp.util.SpecimenRecord;

import java.util.HashMap;

/**
 * Created by tianhong on 2/10/15.
 */


public class SciNameWorkflow extends UntypedActor {

    private final ActorRef listener;
    private final ActorRef workerRouter;


    public SciNameWorkflow(final String serviceSwitch, final boolean writeToFile, final ActorRef listener) {
        this.listener = listener;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new SciNameWorkflowInvocation(serviceSwitch, writeToFile, listener);
            }
        }).withRouter(new SmallestMailboxRouter(6)), "workerRouter");
        getContext().watch(workerRouter);
    }


    public void onReceive(Object message) {
        //System.out.println("ScinRef message: "+ message.toString());
        //System.out.println("ScinRef message: "+ message.getClass().getName());
        if (message instanceof Token) {
            if (!getSender().equals(getSelf())) {
                workerRouter.tell(message, getSelf());
            } else {
                listener.tell(message, getSelf());
            }
        } else if (message instanceof SpecimenRecord) {
            SpecimenRecord result = (SpecimenRecord) message;
            listener.tell(result, getSelf());
            //if (done && --num == 0) {
            //    //TODO: watch children!
            //    getContext().stop(getSelf());
            //}
            // Stops this actor and all its supervised children
            //getContext().stop(getSelf());
            //} else if (message instanceof Done) {
            //    done = true;
            //    if (num == 0) {
            //        //TODO: watch children!
            //        getContext().stop(getSelf());
            //    }
        } else if (message instanceof Broadcast) {
            workerRouter.tell(new Broadcast(((Broadcast) message).message()), getSender());
        } else if (message instanceof Terminated) {
            //System.out.println("SciName termianted");
            if (((Terminated) message).getActor().equals(workerRouter))
                this.getContext().stop(getSelf());
        } else {
            unhandled(message);
        }

        /*
        //pass through the original token
        if (message instanceof TokenWithProv) {
            if (((TokenWithProv) message).getActorCreated().equals("MongoDBReader")) {
                listener.tell(message, getSelf());
            }
        }
        */
    }

    @Override
    public void postStop() {
        System.out.println("Stopped SciNameValidator");
        workerRouter.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }


    public class SciNameWorkflowInvocation extends UntypedActor{

        ActorRef starter;
        ActorRef listener;



        public void onReceive(Object message) {

            if (message instanceof Token && ((Token) message).getData() instanceof SpecimenRecord) {
                SpecimenRecord result = (SpecimenRecord)((Token) message).getData();
                starter.tell(result, getSelf());
            }
        }

        HashMap<Integer, SpecimenRecord> dataStore = new HashMap<Integer, SpecimenRecord>();
        public SciNameWorkflowInvocation (final String serviceSwitch, final boolean writeToFile, final ActorRef listener){

            ActorSystem system = ActorSystem.create("SciNameWorkflow");
            this.listener = listener;

            /*

            final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new SciNameWriter(writeToFile, listener);
                }
            }), "SciNameWriter");


            final ActorRef nomenclatural = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new NomenclaturalService(writer);
                }
            }), "NomenclaturalService");

            final ActorRef taxonomic = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new TaxonomicService(writer);
                }
            }), "TaxonomicService");

            final HashMap<String, ActorRef> availableServices = new HashMap<String, ActorRef>();
            availableServices.put("-t", taxonomic);
            availableServices.put("-n", nomenclatural);

            final ActorRef serviceCall = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new ServiceSelector(serviceSwitch, availableServices);    //(2,null)
                }
            }), "serviceCall");

            */

            final ActorRef nr = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new NameReconciliation(listener);
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

            starter = cni;


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

        @Override
        public void postStop() {
            System.out.println("Stopped SciNameValidator");
            starter.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
        }
    }
}