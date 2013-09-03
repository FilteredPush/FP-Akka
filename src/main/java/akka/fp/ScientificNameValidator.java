package akka.fp;

import akka.actor.*;
import fp.services.IScientificNameValidationService;
import fp.util.*;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;

public class ScientificNameValidator extends UntypedActor {

    private final ActorRef listener;
    private final ActorRef workerRouter;
    private final String service;
    private final boolean useCache;
    private final boolean insertLSID;

    public ScientificNameValidator(final String service, final boolean useCache, final boolean insertLSID, final ActorRef listener) {

        this.listener = listener;
        this.service = service;
        this.useCache = useCache;
        this.insertLSID = insertLSID;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new ScientificNameValidatorInvocation(service, useCache, insertLSID, listener);
            }
        }).withRouter(new SmallestMailboxRouter(6)), "workerRouter");
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
            if (((Terminated) message).getActor().equals(workerRouter))
                this.getContext().stop(getSelf());
        } else {
            unhandled(message);
        }
    }

    @Override
    public void postStop() {
        //System.out.println("Stopped ScinRefValidator");
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }

    public class ScientificNameValidatorInvocation extends UntypedActor {

        private String serviceClassPath = null;
        private String serviceClassQN;
        private boolean insertLSID;
        private boolean hasDataCache;

        private String scientificNameLabel;
        private String authorLabel;
        private String LSIDLabel;

        private IScientificNameValidationService scientificNameService;

        private final Random rand;
        private int invoc;
        private final ActorRef listener;

        public ScientificNameValidatorInvocation(String service, boolean useCache, boolean insertLSID, ActorRef listener) {
            this.serviceClassQN = service;
            this.hasDataCache = useCache;
            this.insertLSID = insertLSID;
            this.listener = listener;
            this.rand = new Random();

            //initialize required label
            SpecimenRecordTypeConf specimenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

            try {
                scientificNameLabel = specimenRecordTypeConf.getLabel("ScientificName");
                if(scientificNameLabel == null){
                    throw new CurrationException(" failed since the ScientificName label of the SpecimenRecordType is not set.");
                }

                authorLabel = specimenRecordTypeConf.getLabel("ScientificNameAuthorship");
                if(authorLabel == null){
                    throw new CurrationException("failed since the ScientificNameAuthorship label of the SpecimenRecordType is not set.");
                }

                if(insertLSID){
                    LSIDLabel = specimenRecordTypeConf.getLabel("IdentificationTaxon");
                    if(LSIDLabel == null){
                        throw new CurrationException(" failed since the IdentificationTaxon label of the SpecimenRecordType is not set.");
                    }
                }

                scientificNameService = (IScientificNameValidationService)Class.forName(serviceClassQN).newInstance();
                scientificNameService.setUseCache(hasDataCache);
            } catch (CurrationException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public void onReceive(Object message) {
            //System.out.println("ScinRefWorker message: "+ message+toString());
            long start = System.currentTimeMillis();
            invoc = rand.nextInt();
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
                    SpecimenRecord inputSpecimenRecord = (SpecimenRecord)((Token) message).getData();

                    String scientificName = inputSpecimenRecord.get(scientificNameLabel);
                    if(scientificName == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY, scientificNameLabel + " is missing in the incoming SpecimenRecord", "ScientificNameValidator");
                        constructOutput(new SpecimenRecord(inputSpecimenRecord),curationComment);
                        return;
                    }

                    String author = inputSpecimenRecord.get(authorLabel);
                    if(author == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,authorLabel+" is missing in the incoming SpecimenRecord","ScientificNameValidator");
                        constructOutput(new SpecimenRecord(inputSpecimenRecord),curationComment);
                        return;
                    }

                    scientificNameService.validateScientificName(scientificName, author);

                    SpecimenRecord cleanedSpecimenRecord = new SpecimenRecord(inputSpecimenRecord);
                    CurationCommentType curationComment = null;
                    String addLSIDComment = "";
                    if(insertLSID){
                        addLSIDComment = "Add LSID from service "+scientificNameService.getServiceName()+".";
                    }
                    CurationStatus curationStatus = scientificNameService.getCurationStatus();
                    if(curationStatus == CurationComment.CORRECT && insertLSID){
                        cleanedSpecimenRecord = constructCleanedSpecimenRecord(inputSpecimenRecord,scientificName,author,scientificNameService.getLSID());
                        curationComment = CurationComment.construct(CurationComment.CURATED,addLSIDComment,getName());
                    } else if (curationStatus == CurationComment.CURATED){
                        if (insertLSID) {
                            cleanedSpecimenRecord = constructCleanedSpecimenRecord(inputSpecimenRecord,scientificNameService.getCorrectedScientificName(),scientificNameService.getCorrectedAuthor(),scientificNameService.getLSID());
                        } else {
                            cleanedSpecimenRecord = constructCleanedSpecimenRecord(inputSpecimenRecord,scientificNameService.getCorrectedScientificName(),scientificNameService.getCorrectedAuthor(),null);
                        }
                        curationComment = CurationComment.construct(CurationComment.CURATED,scientificNameService.getComment()+addLSIDComment,getName());
                    } else if (curationStatus == CurationComment.UNABLE_CURATED){
                        curationComment = CurationComment.construct(CurationComment.UNABLE_CURATED,scientificNameService.getComment(),getName());
                    }else if(curationStatus == CurationComment.UNABLE_DETERMINE_VALIDITY){
                        curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,scientificNameService.getComment(),getName());
                    }
                    //output
                    constructOutput(cleanedSpecimenRecord, curationComment);
                }
                Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
            }
        }

        private SpecimenRecord constructCleanedSpecimenRecord(SpecimenRecord inputSpecimenRecord, String scientificName,String author, String lsid) {
            LinkedHashMap<String,String> valueMap = new LinkedHashMap<String, String>();
            Set<String> labelSet = inputSpecimenRecord.keySet();
            Iterator<String> iter = labelSet.iterator();
            while(iter.hasNext()){
                String label = iter.next();
                String value = inputSpecimenRecord.get(label);

                if (label.equals(scientificNameLabel)){
                    valueMap.put(label, scientificName);
                } else if (label.equals(authorLabel)){
                    valueMap.put(label, author);
                    if(lsid == null){
                        valueMap.put(LSIDLabel, "");
                    }else{
                        valueMap.put(LSIDLabel, lsid);
                    }
                } else {
                    valueMap.put(label, value);
                }
            }
            return new SpecimenRecord(valueMap);
        }

        private void constructOutput(SpecimenRecord result, CurationCommentType comment) {
            if(comment!=null){
                result.put("scinComment", comment.toString());
                result.put("scinStatus", comment.getStatus());
            }
            listener.tell(new TokenWithProv<SpecimenRecord>(result,getClass().getSimpleName(),invoc),getContext().parent());
        }

        public String getName() {
            return "ScientificNameValidator";
        }
    }
}
