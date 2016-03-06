package org.filteredpush.akka.actors;

import akka.actor.*;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;

import org.filteredpush.kuration.interfaces.IInternalDateValidationService;
import org.filteredpush.kuration.interfaces.IStringValidationService;
import org.filteredpush.kuration.services.BasisOfRecordValidationService;
import org.filteredpush.kuration.util.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;

/**
 * clustering on Collectors
 * then clustering on Time
 * then in each cluster, find out the outlier according to the lat/log (calculate distance between each two point)
 * 
 * @author fancy
 *
 */
public class BasisOfRecordValidator extends UntypedActor {
    private final ActorRef listener;
    private final ActorRef workerRouter;
    //todo: use router for multiple instance

    public BasisOfRecordValidator(final String service, final ActorRef listener) {

        this.listener = listener;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new BasisOfRecordValidatorInvocation(service,  listener);
            }
        }).withRouter(new SmallestMailboxRouter(6)), "workerRouter");
        getContext().watch(workerRouter);
    }

    public BasisOfRecordValidator(final String service, int numIns, final ActorRef listener) {

        this.listener = listener;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new BasisOfRecordValidatorInvocation(service,  listener);
            }
        }).withRouter(new SmallestMailboxRouter(numIns)), "workerRouter");
        getContext().watch(workerRouter);
    }

    public void onReceive(Object message) {
        //System.out.println("ScinRef message: "+ message+toString());
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
            //    //TODO: watch children!        numIns
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
    }

    @Override
    public void postStop() {
        System.out.println("Stopped BasisOfRecordValidator");
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }

    public class BasisOfRecordValidatorInvocation extends UntypedActor {
        private final ActorRef listener;
        private String singleServiceClassQN;

        private String basisOfRecordLabel;

        //for invocation id
        private final Random rand;
        private int invoc;

        IStringValidationService basisOfRecordValidationService = null;
        private LinkedList<SpecimenRecord> inputObjList = new LinkedList<SpecimenRecord>();
        private LinkedHashMap<String, TreeSet<SpecimenRecord>> inputDataMap = new LinkedHashMap<String, TreeSet<SpecimenRecord>>();        
        
        public BasisOfRecordValidatorInvocation(String singleServiceClassQN, ActorRef listener) {
            this.listener = listener;
            this.rand = new Random();

            try {
                //initialize required label
                SpecimenRecordTypeConf speicmenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

                basisOfRecordLabel = speicmenRecordTypeConf.getLabel("BasisOfRecord");
                if (basisOfRecordLabel == null) {
                    throw new CurationException(getName() + " failed since the basisOfRecord label of the SpecimenRecordType is not set.");
                }

                //resolve service
                basisOfRecordValidationService = (IStringValidationService) new BasisOfRecordValidationService();

            } catch (CurationException e) {
                e.printStackTrace();
            }

            // handleScopeStart
            inputObjList.clear();
            inputDataMap.clear();

            //workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            //    @Override
            //    public Actor create() throws Exception {
            //        return new ScientificNameValidatorInvocation(service, useCache, insertLSID, listener);
            //    }
            //}).withRouter(new RoundRobinRouter(6)), "workerRouter");
            //getContext().watch(workerRouter);
        }

        public String getName() {
            return "BasisOfRecordValidator";
        }

        @Override
        public void onReceive(Object message) throws Exception {
            invoc = rand.nextInt();

            //for date validation for each record
            if (message instanceof TokenWithProv) {
                //if(dataLabelStr.equals(getCurrentToken().getLabel().toString())){

                SpecimenRecord inputSpecimenRecord = (SpecimenRecord) ((Token) message).getData();

                //System.err.println("datestart#"+inputSpecimenRecord.get("oaiid").toString() + "#" + System.currentTimeMillis());

                String basisOfRecord = inputSpecimenRecord.get(basisOfRecordLabel);
                //System.err.println("servicestart#"+ inputSpecimenRecord.get("oaiid").toString() + "#" + ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()/1000);
                basisOfRecordValidationService.validateString(basisOfRecord);
                //System.err.println("servicesend#"+ inputSpecimenRecord.get("oaiid").toString() + "#" + ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()/1000);

                CurationStatus curationStatus = basisOfRecordValidationService.getCurationStatus();

                //System.out.println("curationStatus = " + curationStatus);
                //System.out.println("basisOfRecordValidationService.getComment() = " + basisOfRecordValidationService.getComment());
                //System.out.println("basisOfRecordValidationService.getServiceName() = " + basisOfRecordValidationService.getServiceName());

                if (curationStatus == CurationComment.CURATED || curationStatus == CurationComment.FILLED_IN) {
                    //replace the old value if curated
                    //inputSpecimenRecord.put("eventDate", String.valueOf(basisOfRecordValidationService.getCorrectedDate()));
                    String originalBasis = inputSpecimenRecord.get(SpecimenRecord.dwc_basisOfRecord);
                    String newBasis = basisOfRecordValidationService.getCorrectedValue();
                    if(originalBasis != null && originalBasis.length() != 0 &&  !originalBasis.equals(newBasis)){
                        inputSpecimenRecord.put(SpecimenRecord.Original_BasisOfRecord_Label, originalBasis);
                        inputSpecimenRecord.put(SpecimenRecord.dwc_basisOfRecord, newBasis);
                    }
                }

                CurationCommentType curationComment = CurationComment.construct(curationStatus, basisOfRecordValidationService.getComment(), basisOfRecordValidationService.getServiceName());
                constructOutput(inputSpecimenRecord, curationComment);
                /*
                for (List l : basisOfRecordValidationService.getLog()) {
                    Prov.log().printf("service\t%s\t%d\t%s\t%d\t%d\t%s\t%s\n", this.getClass().getSimpleName(), invoc, (String)l.get(0), (Long)l.get(1), (Long)l.get(2),l.get(3),curationStatus.toString());
                }

            } else if (message instanceof Terminated) {
                //if (((Terminated) message).getActor().equals(workerRouter))
                    this.getContext().stop(getSelf());
                    */
            } else {
                unhandled(message);
            }
        }

        private void constructOutput(SpecimenRecord result, CurationCommentType comment) {
            if (comment != null) {
                result.put(SpecimenRecord.borRef_Comment_Label, comment.getDetails());
                result.put(SpecimenRecord.borRef_Status_Label, comment.getStatus());
                result.put(SpecimenRecord.borRef_Source_Label, comment.getSource());
            } else {
                result.put(SpecimenRecord.borRef_Comment_Label, "None");
                result.put(SpecimenRecord.borRef_Status_Label, CurationComment.CORRECT.toString());
                result.put(SpecimenRecord.borRef_Source_Label, comment.getSource());
            }
            //System.err.println("dateend#"+result.get("oaiid").toString() + "#" + System.currentTimeMillis());
            listener.tell(new TokenWithProv<SpecimenRecord>(result, getClass().getName(), invoc), getContext().parent());
        }


    }
}