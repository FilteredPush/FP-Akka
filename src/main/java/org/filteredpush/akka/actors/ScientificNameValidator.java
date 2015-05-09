package org.filteredpush.akka.actors;

import akka.actor.*;
import edu.harvard.mcz.nametools.AuthorNameComparator;
import edu.harvard.mcz.nametools.ICNafpAuthorNameComparator;
import edu.harvard.mcz.nametools.NameComparison;
import edu.harvard.mcz.nametools.NameUsage;
import fp.services.IScientificNameValidationService;
import fp.util.*;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;

import java.util.*;

import org.filteredpush.akka.data.Prov;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;

public class ScientificNameValidator extends UntypedActor {

    private final ActorRef listener;
    private final ActorRef workerRouter;
    private final String service;
    private final boolean useCache;
    private final boolean insertLSID;

    /**
     * Replaced by NewScientificNameValidator. 
     * 
     * @param service
     * @param useCache
     * @param insertGUID
     * @param listener
     */
    @Deprecated 
    public ScientificNameValidator(final String service, final boolean useCache, final boolean insertGUID, final ActorRef listener) {

        this.listener = listener;
        this.service = service;
        this.useCache = useCache;
        this.insertLSID = insertGUID;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new ScientificNameValidatorInvocation(service, useCache, insertGUID, listener);
            }
        }).withRouter(new SmallestMailboxRouter(6)), "workerRouter");
        getContext().watch(workerRouter);
    }

    /**
     * Replaced by NewScientificNameValidator
     * 
     * @param service
     * @param useCache
     * @param insertGUID
     * @param listener
     * @param instances
     */
    @Deprecated 
    public ScientificNameValidator(final String service, final boolean useCache, final boolean insertGUID, final ActorRef listener, final int instances) {
        this.listener = listener;
        this.service = service;
        this.useCache = useCache;
        this.insertLSID = insertGUID;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new ScientificNameValidatorInvocation(service, useCache, insertGUID, listener);
            }
        }).withRouter(new SmallestMailboxRouter(instances)), "workerRouter");
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
        //System.out.println("Stopped ScinRefValidator");
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }

    public class ScientificNameValidatorInvocation extends UntypedActor {

        private String serviceClassPath = null;
        private String serviceClassQN;
        private boolean insertGUID;
        private boolean hasDataCache;

        private String scientificNameLabel;
        private String authorLabel;
        private String GUIDLabel;

        private IScientificNameValidationService scientificNameService;

        private final Random rand;
        private int invoc;
        private final ActorRef listener;

        public ScientificNameValidatorInvocation(String service, boolean useCache, boolean insertGUID, ActorRef listener) {
            this.serviceClassQN = service;
            this.hasDataCache = useCache;
            this.insertGUID = insertGUID;
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

                if(insertGUID){
                    GUIDLabel = specimenRecordTypeConf.getLabel("IdentificationTaxon");
                    if(GUIDLabel == null){
                        GUIDLabel = "IdentificationTaxon";
                        //throw new CurrationException(" failed since the IdentificationTaxon label of the SpecimenRecordType is not set.");
                    }
                }
                scientificNameService = (IScientificNameValidationService)Class.forName(serviceClassQN).newInstance();
                scientificNameService.setUseCache(hasDataCache);
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
                /*Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                                        ((TokenWithProv) message).getActorCreated(),
                                        ((TokenWithProv) message).getInvocCreated(),
                                        this.getClass().getSimpleName(),
                                        invoc,
                                        ((TokenWithProv) message).getTimeCreated(),
                                        System.currentTimeMillis());   */
            }

            if (message instanceof Token) {
                if (((Token) message).getData() instanceof SpecimenRecord) {
                    SpecimenRecord inputSpecimenRecord = (SpecimenRecord)((Token) message).getData();

                    //System.out.println("inputSpecimenRecord = " + inputSpecimenRecord.toString());

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
                    CurationStatus curationStatus = scientificNameService.getCurationStatus();
                    
                    AuthorNameComparator authorNameComparator = scientificNameService.getAuthorNameComparator(author,"");
                    
                    NameUsage nameUsage = new NameUsage();
					nameUsage.setAuthorComparator(authorNameComparator);
					nameUsage.setGuid(scientificNameService.getLSID());
				    nameUsage.setScientificName(scientificNameService.getCorrectedScientificName());
				    nameUsage.setAuthorship(scientificNameService.getCorrectedAuthor());
				    nameUsage.setOriginalAuthorship(author);
				    nameUsage.setOriginalScientificName(scientificName);
				    double nameSimilarity = ICNafpAuthorNameComparator.stringSimilarity(scientificName, nameUsage.getScientificName());
				    NameComparison comparison = authorNameComparator.compare(author, nameUsage.getAuthorship()); 
				    double authorSimilarity = comparison.getSimilarity();
				    String match = comparison.getMatchType();
				    if (authorSimilarity==1d && nameSimilarity==1d) { 
				    	nameUsage.setMatchDescription(NameComparison.MATCH_EXACT);
				    	curationStatus = CurationComment.CORRECT;
				    } else { 
				    	nameUsage.setMatchDescription(match);
				    }
				    nameUsage.setAuthorshipStringEditDistance(authorSimilarity);     
				    
				    String authorshipSimilarity = "Authorship: " +  nameUsage.getMatchDescription() + " Similarity: " + Double.toString(nameSimilarity);

                    SpecimenRecord cleanedSpecimenRecord = new SpecimenRecord(inputSpecimenRecord);
                    CurationCommentType curationComment = null;
                    String addGUIDComment = "";
                    if(insertGUID){
                        addGUIDComment = "Add GUID from service "+scientificNameService.getServiceName()+".";
                    }
                    if(curationStatus == CurationComment.CORRECT && insertGUID){
                        cleanedSpecimenRecord = constructCleanedSpecimenRecord(inputSpecimenRecord,scientificName,author,scientificNameService.getLSID());
                        curationComment = CurationComment.construct(CurationComment.CURATED,addGUIDComment,getName());
                    } else if (curationStatus == CurationComment.CURATED){
                        if (insertGUID) {
                            cleanedSpecimenRecord = constructCleanedSpecimenRecord(inputSpecimenRecord,scientificNameService.getCorrectedScientificName(),scientificNameService.getCorrectedAuthor(),scientificNameService.getLSID());
                        } else {
                            cleanedSpecimenRecord = constructCleanedSpecimenRecord(inputSpecimenRecord,scientificNameService.getCorrectedScientificName(),scientificNameService.getCorrectedAuthor(),null);
                        }
                        curationComment = CurationComment.construct(CurationComment.CURATED,scientificNameService.getComment()+addGUIDComment,getName() + authorshipSimilarity);
                    } else if (curationStatus == CurationComment.UNABLE_CURATED){
                        curationComment = CurationComment.construct(CurationComment.UNABLE_CURATED,scientificNameService.getComment() + authorshipSimilarity,getName());
                    } else if (curationStatus == CurationComment.UNABLE_DETERMINE_VALIDITY){
                        curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,scientificNameService.getComment(),getName());
                    }
                    //output
                    constructOutput(cleanedSpecimenRecord, curationComment);
                    for (List l : scientificNameService.getLog()) {
                        Prov.log().printf("service\t%s\t%d\t%s\t%d\t%d\t%s\t%s\n", this.getClass().getSimpleName(), invoc, l.get(0), l.get(1), l.get(2), l.get(3), curationStatus.toString());
                    }
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
                        valueMap.put(GUIDLabel, "");
                    }else{
                        valueMap.put(GUIDLabel, lsid);
                    }
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
            listener.tell(new TokenWithProv<SpecimenRecord>(result,getClass().getSimpleName(),invoc),getContext().parent());
        }

        public String getName() {
            return "ScientificNameValidator";
        }
    }
}
