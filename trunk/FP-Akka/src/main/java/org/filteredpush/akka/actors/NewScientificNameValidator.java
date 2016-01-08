package org.filteredpush.akka.actors;

import akka.actor.*;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;

import org.filteredpush.kuration.interfaces.INewScientificNameValidationService;
import org.filteredpush.kuration.services.sciname.*;
import org.filteredpush.kuration.util.*;

import java.io.IOException;
import java.util.*;

import org.filteredpush.akka.data.ReadMore;
import org.filteredpush.akka.data.SetUpstreamListener;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.nameparser.NameParser;
import org.gbif.nameparser.UnparsableException;

public class NewScientificNameValidator extends UntypedActor {

	private ActorRef upstreamListener;
    private final ActorRef listener;
    private final ActorRef workerRouter;
    private final boolean useCache;
    private final boolean insertLSID;

    
    /**
     * Set up an akka workflow actor that wraps general scientific name validation classes, 
     * defaults to 6 parallel instances
     * 
     * @param useCache tell the validator to use a cache file
     * @param insertGUID add any GUID returned by the validator to the output
     * @param authorityName switch to select the scientific name validator class to invoke.
     * @param listener downstream actor that will consume output from this actor 
     */
    public NewScientificNameValidator(final boolean useCache, final boolean insertGUID, final String authorityName, final boolean taxonomicMode,  final ActorRef listener ) {
    	this(useCache, insertGUID, 6, authorityName, taxonomicMode, listener);
    }

    /**
     * Set up an akka workflow actor that wraps general scientific name validation classes.,
     * 
     * @param useCache tell the validator to use a cache file
     * @param insertGUID add any GUID returned by the validator to the output
     * @param instances number of parallel instances 
     * @param authorityName switch to select the scientific name validator class to invoke.
     * @param taxonomicMode 
     * @param listener downstream actor that will consume output from this actor 
     */
    public NewScientificNameValidator(final boolean useCache, final boolean insertGUID, final int instances, final String authorityName, final boolean taxonomicMode, final ActorRef listener ) {
        System.out.println("NewScientificNameValidator authority: "+ authorityName);
        System.out.println("NewScientificNameValidator taxonomicMode: "+ taxonomicMode);
        System.out.println("NewScientificNameValidator insertGUID: "+ insertGUID);
        this.upstreamListener = null;
        this.listener = listener;
        this.useCache = useCache;
        this.insertLSID = insertGUID;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new ScientificNameValidatorInvocation(useCache, insertGUID, authorityName, taxonomicMode, listener);
            }
        }).withRouter(new SmallestMailboxRouter(instances)), "workerRouter");
        getContext().watch(workerRouter);
    }

    public void onReceive(Object message) {
        //System.out.println("ScinRef message: "+ message.toString());
        //System.out.println("ScinRef message: "+ mFessage.getClass().getName());
    	if (message instanceof SetUpstreamListener) { 
    		this.setUpstreamListener(getSender());
    	} else if (message instanceof Token) {
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
            //System.out.println("SciName terminated");
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
        private boolean insertGUID;
        private boolean hasDataCache;

        private String scientificNameLabel;
        private String authorLabel;
        private String LSIDLabel;

        private INewScientificNameValidationService scientificNameService;

        private final Random rand;
        private int invoc;
        private final ActorRef listener;

        public ScientificNameValidatorInvocation(boolean useCache, boolean insertGUID, String authorityName, boolean taxonomicMode, ActorRef listener) {
            this.hasDataCache = useCache;
            this.insertGUID = insertGUID;
            this.listener = listener;
            this.rand = new Random();

            //initialize required label
            SpecimenRecordTypeConf specimenRecordTypeConf = SpecimenRecordTypeConf.getInstance();


            try {
                scientificNameLabel = specimenRecordTypeConf.getLabel("ScientificName");
                if(scientificNameLabel == null){
                    scientificNameLabel = SpecimenRecord.dwc_scientificName;
                    //throw new CurrationException(" failed since the ScientificName label of the SpecimenRecordType is not set.");
                }

                authorLabel = specimenRecordTypeConf.getLabel("ScientificNameAuthorship");
                if(authorLabel == null){
                    authorLabel = SpecimenRecord.dwc_scientificNameAuthorship;
                    //throw new CurrationException("failed since the ScientificNameAuthorship label of the SpecimenRecordType is not set.");
                }

                if(insertGUID){
                    LSIDLabel = specimenRecordTypeConf.getLabel("TaxonID");
                    if(LSIDLabel == null){
                        LSIDLabel = "IdentificationTaxon";
                        //throw new CurrationException(" failed since the IdentificationTaxon label of the SpecimenRecordType is not set.");
                    }
                }

                 //scientificNameService = (INewScientificNameValidationService)Class.forName(serviceClassQN).newInstance();
                //use the authority argument to select which service to use
                switch(authorityName.toUpperCase()) { 
                case "IF": 
                case "INDEXFUNGORUM": 
                	scientificNameService = new IndexFungorumService();
                	break;
                case "WORMS": 
                	scientificNameService = new WoRMSService();
                	break;
                case "COL": 
                	scientificNameService = new COLService();
                	break;
                case "IPNI": 
                	scientificNameService = new IPNIService();
                	break;
                case "GBIF": 
                default: 
                	if (!authorityName.toUpperCase().equals("GBIF")) { 
                	    System.err.println("Unrecognized service (" + authorityName + ") or service not specified, using GBIF.");
                	}
                	scientificNameService = new GBIFService();
                }                
                
                //set validation mode
                if(!taxonomicMode) scientificNameService.setValidationMode(INewScientificNameValidationService.MODE_NOMENCLATURAL);
                else scientificNameService.setValidationMode(INewScientificNameValidationService.MODE_TAXONOMIC);

            //} catch (CurrationException e) {
            //    e.printStackTrace();
            } catch (IOException e) {
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
                                        System.currentTimeMillis());      */
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
                    // TODO: Need an actor to extract scientific name authorships from scientific name when authorship is not provided separately
                    if(author == null){
             		   NameParser parser = new NameParser();
            		   try {
            			   ParsedName parse = parser.parse(scientificName);
            			   author = parse.getAuthorship();
            		   } catch (UnparsableException e) {
            		   }
                    }

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
                    String genericEpithet = "";


                    /*
                    System.out.println("taxonRank = " + taxonRank);
                    System.out.println("infraspecificEpithet = " + infraspecificEpithet);
                    System.out.println("verbatimTaxonRank = " + verbatimTaxonRank);
                    System.out.println("specificEpithet = " + specificEpithet);
                    System.out.println("subgenus = " + subgenus);
                    System.out.println("genus = " + genus);
                     */
                    try { 
                        scientificNameService.validateScientificName( scientificName, author, genus, subgenus,specificEpithet, verbatimTaxonRank, infraspecificEpithet, taxonRank, kingdom, phylum, tclass, order, family, genericEpithet);
                    } catch (Exception e) { 
                    	// If we don't catch a exception that will stop this actor here, we can starve the
                    	// the workflow by blocking the upstream reader that is waiting for completion.
                        if (upstreamListener!=null) { 
                	        upstreamListener.tell(new ReadMore(), getSelf());
                        }
                        // TODO: Should we throw the exception again, or keep running? 
                    }

                    CurationStatus curationStatus = scientificNameService.getCurationStatus();
                    
                    if (curationStatus == CurationComment.CORRECT) {
                    	// If a blank has been filled in from atomic fields and is asserted as correct, put it inot the output.
                    	if (inputSpecimenRecord.get(SpecimenRecord.dwc_scientificName)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_scientificName).trim().length()==0) { 
                    		String originalSciName = "";
                            String newSciName = scientificNameService.getCorrectedScientificName();	
                            inputSpecimenRecord.put(SpecimenRecord.Original_SciName_Label, originalSciName);
                            inputSpecimenRecord.put(SpecimenRecord.dwc_scientificName, newSciName);
                    	}
                    }
                    
                    if(curationStatus == CurationComment.CURATED || curationStatus == CurationComment.FILLED_IN){
                        //put in original value first
                        String originalSciName =  inputSpecimenRecord.get(SpecimenRecord.dwc_scientificName);
                        String originalAuthor = inputSpecimenRecord.get(SpecimenRecord.dwc_scientificNameAuthorship);
                        String newSciName = scientificNameService.getCorrectedScientificName();
                        String newAuthor = scientificNameService.getCorrectedAuthor();

                        if(originalSciName != null && originalSciName.length() != 0 &&  !originalSciName.equals(newSciName)){
                            inputSpecimenRecord.put(SpecimenRecord.Original_SciName_Label, originalSciName);
                            inputSpecimenRecord.put(SpecimenRecord.dwc_scientificName, newSciName);
                        } else if (originalSciName==null || originalSciName.trim().length()==0) { 
                            inputSpecimenRecord.put(SpecimenRecord.Original_SciName_Label, "");
                            inputSpecimenRecord.put(SpecimenRecord.dwc_scientificName, newSciName);
                        }
                        if(originalAuthor != null && !originalAuthor.equals(newAuthor)){
                            inputSpecimenRecord.put(SpecimenRecord.Original_Authorship_Label, originalAuthor);
                            inputSpecimenRecord.put(SpecimenRecord.dwc_scientificNameAuthorship, newAuthor);
                        }
                    }
                    // add a GUID one was returned
                    if(!scientificNameService.getGUID().equals("")) {
                    	// TODO: We should be able to handle scientificNameID and acceptedNameUsageID
                    	inputSpecimenRecord.put("taxonID", scientificNameService.getGUID());
                    	
                    }
                    
                    StringBuffer higherFillInComment = new StringBuffer(); 
                    // if higher taxonomy is absent, fill it in 
                    if (inputSpecimenRecord.get(SpecimenRecord.dwc_kingdom)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_kingdom).trim().length()==0) { 
                    	if (scientificNameService.getCorrectedKingdom()!=null && scientificNameService.getCorrectedKingdom().trim().length() >0) { 
                    	   inputSpecimenRecord.put(SpecimenRecord.dwc_kingdom, scientificNameService.getCorrectedKingdom());
                    	   higherFillInComment.append(" | Filled In Kingdom ");
                    	}
                    }
                    if (inputSpecimenRecord.get(SpecimenRecord.dwc_phylum)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_phylum).trim().length()==0) { 
                    	if (scientificNameService.getCorrectedPhylum()!=null && scientificNameService.getCorrectedPhylum().trim().length() >0) { 
                    	    inputSpecimenRecord.put(SpecimenRecord.dwc_phylum, scientificNameService.getCorrectedPhylum()); 
                    	    higherFillInComment.append(" | Filled In Phylum ");
                    	}
                    }
                    if (inputSpecimenRecord.get(SpecimenRecord.dwc_class)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_class).trim().length()==0) { 
                    	if (scientificNameService.getCorrectedClass()!=null && scientificNameService.getCorrectedClass().trim().length() >0) { 
                    	    inputSpecimenRecord.put(SpecimenRecord.dwc_class, scientificNameService.getCorrectedClass()); 
                    	    higherFillInComment.append(" | Filled In Class ");
                    	}
                    }
                    if (inputSpecimenRecord.get(SpecimenRecord.dwc_order)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_order).trim().length()==0) { 
                    	if (scientificNameService.getCorrectedOrder()!=null && scientificNameService.getCorrectedOrder().trim().length() >0) { 
                    	    inputSpecimenRecord.put(SpecimenRecord.dwc_order, scientificNameService.getCorrectedOrder()); 
                    	    higherFillInComment.append(" | Filled In Order ");
                    	}
                    }
                    if (inputSpecimenRecord.get(SpecimenRecord.dwc_family)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_family).trim().length()==0) { 
                    	if (scientificNameService.getCorrectedFamily()!=null && scientificNameService.getCorrectedFamily().trim().length() >0) { 
                    	    inputSpecimenRecord.put(SpecimenRecord.dwc_family, scientificNameService.getCorrectedFamily()); 
                    	    higherFillInComment.append(" | Filled In Family ");
                    	}
                    }
                    
                    //output
                    CurationCommentType curationComment = CurationComment.construct(curationStatus,scientificNameService.getComment().concat(higherFillInComment.toString()),scientificNameService.getServiceName());
                    constructOutput(inputSpecimenRecord, curationComment);
                    /*for (List l : scientificNameService.getLog()) {
                        Prov.log().printf("service\t%s\t%d\t%s\t%d\t%d\t%s\t%s\n", this.getClass().getSimpleName(), invoc, l.get(0), l.get(1), l.get(2), l.get(3), curationStatus.toString());
                    }     */
                }
                //Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
                
                if (upstreamListener!=null) { 
                	upstreamListener.tell(new ReadMore(), getSelf());
                }
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
                result.put(SpecimenRecord.SciName_Comment_Label, comment.getDetails());
                result.put(SpecimenRecord.SciName_Status_Label, comment.getStatus());
                result.put(SpecimenRecord.SciName_Source_Label, comment.getSource());
            }
            //System.err.println("scinameend#"+result.get("oaiid").toString() + "#" + System.currentTimeMillis());
            listener.tell(new TokenWithProv<SpecimenRecord>(result,getClass().getSimpleName(),invoc),getContext().parent());
        }

        public String getName() {
            return "ScientificNameValidator";
        }
    }

	/**
	 * @return the upstreamListener
	 */
	public ActorRef getUpstreamListener() {
		return upstreamListener;
	}

	/**
	 * @param upstreamListener the upstreamListener to set
	 */
	public void setUpstreamListener(ActorRef upstreamListener) {
		this.upstreamListener = upstreamListener;
		if (upstreamListener!=null) { 
		     System.out.println(this.getClass().getSimpleName() + " will make pull requests to " + upstreamListener.getClass().getCanonicalName());
		}
	}
}
