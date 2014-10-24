package akka.fp;

import akka.actor.*;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;
import fp.services.INewScientificNameValidationService;
import fp.util.*;

import java.util.*;

public class NewScientificNameValidator extends UntypedActor {

    private final ActorRef listener;
    private final ActorRef workerRouter;
    private final String service;
    private final boolean useCache;
    private final boolean insertLSID;


    public NewScientificNameValidator(final String service, final boolean useCache, final boolean insertLSID,  final ActorRef listener ) {
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

    public NewScientificNameValidator(final String service, final boolean useCache, final boolean insertLSID, final int instances, final ActorRef listener ) {
        this.listener = listener;
        this.service = service;
        this.useCache = useCache;
        this.insertLSID = insertLSID;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new ScientificNameValidatorInvocation(service, useCache, insertLSID, listener);
            }
        }).withRouter(new SmallestMailboxRouter(instances)), "workerRouter");
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
        System.out.println("Stopped ScinRefValidator");
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

        private INewScientificNameValidationService scientificNameService;

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
                    scientificNameLabel = "scientificName";
                    //throw new CurrationException(" failed since the ScientificName label of the SpecimenRecordType is not set.");
                }

                authorLabel = specimenRecordTypeConf.getLabel("ScientificNameAuthorship");
                if(authorLabel == null){
                    authorLabel = "scientificNameAuthorship";
                    //throw new CurrationException("failed since the ScientificNameAuthorship label of the SpecimenRecordType is not set.");
                }

                if(insertLSID){
                    LSIDLabel = specimenRecordTypeConf.getLabel("IdentificationTaxon");
                    if(LSIDLabel == null){
                        LSIDLabel = "IdentificationTaxon";
                        //throw new CurrationException(" failed since the IdentificationTaxon label of the SpecimenRecordType is not set.");
                    }
                }

                scientificNameService = (INewScientificNameValidationService)Class.forName(serviceClassQN).newInstance();
            //} catch (CurrationException e) {
            //    e.printStackTrace();
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
                    //System.err.println("scinamestart#"+inputSpecimenRecord.get("oaiid").toString() + "#" + System.currentTimeMillis());
                    //System.out.println("inputSpecimenRecord = " + inputSpecimenRecord.toString());

                    String scientificName = inputSpecimenRecord.get(scientificNameLabel);
                    //System.out.println("scientificName = " + scientificName);
                    if(scientificName == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY, scientificNameLabel + " is missing in the incoming SpecimenRecord", "ScientificNameValidator");
                        constructOutput(new SpecimenRecord(inputSpecimenRecord),curationComment);
                        return;
                    }

                    String author = inputSpecimenRecord.get(authorLabel);
                    //System.out.println("author = " + author);
                    /*if(author == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,authorLabel+" is missing in the incoming SpecimenRecord","ScientificNameValidator");
                        constructOutput(new SpecimenRecord(inputSpecimenRecord),curationComment);
                        return;
                    }  */

                    String genus = inputSpecimenRecord.get("genus");
                    String subgenus = inputSpecimenRecord.get("subgenus");
                    String specificEpithet = inputSpecimenRecord.get("specificEpithet");
                    String verbatimTaxonRank = inputSpecimenRecord.get("verbatimTaxonRank");
                    String infraspecificEpithet = inputSpecimenRecord.get("infraspecificEpithet");
                    String taxonRank = inputSpecimenRecord.get("taxonRank");
                    String kingdom = inputSpecimenRecord.get("kingdom");
                    String phylum = inputSpecimenRecord.get("phylum");
                    String tclass = inputSpecimenRecord.get("tclass");
                    String order = inputSpecimenRecord.get("order");
                    String family = inputSpecimenRecord.get("family");


                    /*
                    System.out.println("taxonRank = " + taxonRank);
                    System.out.println("infraspecificEpithet = " + infraspecificEpithet);
                    System.out.println("verbatimTaxonRank = " + verbatimTaxonRank);
                    System.out.println("specificEpithet = " + specificEpithet);
                    System.out.println("subgenus = " + subgenus);
                    System.out.println("genus = " + genus);
                     */
                    scientificNameService.validateScientificName( scientificName, author, genus, subgenus,specificEpithet, verbatimTaxonRank, infraspecificEpithet, taxonRank, kingdom, phylum, tclass, order, family);

                    CurationStatus curationStatus = scientificNameService.getCurationStatus();
                    if(curationStatus == CurationComment.CURATED || curationStatus == CurationComment.Filled_in){
                        inputSpecimenRecord.put("scientificName", scientificNameService.getCorrectedScientificName());
                        inputSpecimenRecord.put("scientificNameAuthorship", scientificNameService.getCorrectedAuthor());
                    }

                    //output
                    CurationCommentType curationComment = CurationComment.construct(curationStatus,scientificNameService.getComment(),scientificNameService.getServiceName());
                    constructOutput(inputSpecimenRecord, curationComment);
                    /*for (List l : scientificNameService.getLog()) {
                        Prov.log().printf("service\t%s\t%d\t%s\t%d\t%d\t%s\t%s\n", this.getClass().getSimpleName(), invoc, l.get(0), l.get(1), l.get(2), l.get(3), curationStatus.toString());
                    }     */
                }
                Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
            }
        }

        private SpecimenRecord constructCleanedSpecimenRecord(SpecimenRecord inputSpecimenRecord, String scientificName,String author) {
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

                } else {
                    valueMap.put(label, value);
                }
            }
            return new SpecimenRecord(valueMap);
        }

        private void constructOutput(SpecimenRecord result, CurationCommentType comment) {
            if(comment!=null){
                result.put("scinComment", comment.getDetails());
                result.put("scinStatus", comment.getStatus());
                result.put("scinSource", comment.getSource());
            }
            //System.err.println("scinameend#"+result.get("oaiid").toString() + "#" + System.currentTimeMillis());
            listener.tell(new TokenWithProv<SpecimenRecord>(result,getClass().getSimpleName(),invoc),getContext().parent());
        }

        public String getName() {
            return "ScientificNameValidator";
        }
    }
}
