package akka.fp;


import akka.Token;
import akka.TokenWithProv;
import akka.actor.*;
import fp.services.IFloweringTimeValidationService;
import fp.util.*;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;


import java.util.*;

/*
 * assume the flowering time information in the ReproductiveCondition is organized as: ..Flower: Jan;Feb,....
 */
public class FloweringTimeValidator extends UntypedActor {

    private final ActorRef listener;
    private final ActorRef workerRouter;
    private final String service;
    private final boolean useCache;

    public FloweringTimeValidator(final String service, final boolean useCache, final boolean insertLSID, final ActorRef listener) {
        this.listener = listener;
        this.service = service;
        this.useCache = useCache;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new FloweringTimeValidatorInvocation(service, useCache, listener);
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

    public class FloweringTimeValidatorInvocation extends UntypedActor {

        private final String serviceClassQN;
        private final IFloweringTimeValidationService floweringTimeValidationService;
        private int invoc;
        private final Random rand;

        public FloweringTimeValidatorInvocation(String service, boolean useCache, ActorRef listener) {
            this.rand = new Random();
            //initialize required label
            SpecimenRecordTypeConf speicmenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

            //try {
                scientificNameLabel = speicmenRecordTypeConf.getLabel("ScientificName");
                if(scientificNameLabel == null){
                    scientificNameLabel = "scientificName";
                    //throw new CurrationException(getName()+" failed since the ScientificName label of the SpecimenRecordType is not set.");
                }

                ReproductiveConditionLabel = speicmenRecordTypeConf.getLabel("ReproductiveCondition");
                if(ReproductiveConditionLabel == null){
                    ReproductiveConditionLabel = "reproductiveCondition";
                    //throw new CurrationException(getName()+" failed since the ReproductiveCondition label of the SpecimenRecordType is not set.");
                }
            //} catch (CurrationException e) {
            //e.printStackTrace();
            //}

            //resolve service
            serviceClassQN = service;
            Object o = null;
            try {
                o = Class.forName(serviceClassQN).newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            floweringTimeValidationService = (IFloweringTimeValidationService)o;

            // cache?
        }

        public String getName() {
            return "FloweringTimeValidator";
        }

        @Override
        public void onReceive(Object o) throws Exception {
            long start = System.currentTimeMillis();
            invoc = rand.nextInt();
            if (o instanceof TokenWithProv) {
                Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                                        ((TokenWithProv) o).getActorCreated(),
                                        ((TokenWithProv) o).getInvocCreated(),
                                        this.getClass().getSimpleName(),
                                        invoc,
                                        ((TokenWithProv) o).getTimeCreated(),
                                        System.currentTimeMillis());
            }

            if (o instanceof Token) {
                if (((Token) o).getData() instanceof SpecimenRecord) {
                    SpecimenRecord inputSpecimenRecord = (SpecimenRecord)((Token) o).getData();

                    String scientificName = inputSpecimenRecord.get(scientificNameLabel);
                    if(scientificName == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY, scientificNameLabel + " is missing in the incoming SpecimenRecord", getName());
                        constructOutput(inputSpecimenRecord,curationComment);
                        return;
                    }

                    String reproductiveCondtion = inputSpecimenRecord.get(ReproductiveConditionLabel);
                    if(reproductiveCondtion == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,ReproductiveConditionLabel+" is missing in the incoming SpecimenRecord",getName());
                        constructOutput(inputSpecimenRecord, curationComment);
                        return;
                    }

                    Vector<String> floweringMonthVector = parseReproductiveCondition(reproductiveCondtion);

                    //invoke the service
                    floweringTimeValidationService.validateFloweringTime(scientificName,floweringMonthVector);

                    //construct the reproductive condition and construct curation comment
                    SpecimenRecord cleanedSpecimenRecord = null;
                    CurationCommentType curationComment = null;
                    CurationStatus curationStatus = floweringTimeValidationService.getCurationStatus();
                    if (curationStatus == CurationComment.CURATED){
                        curationComment = CurationComment.construct(CurationComment.CURATED,floweringTimeValidationService.getComment(),floweringTimeValidationService.getServiceName());
                        Vector<String> correctedFloweringtime = floweringTimeValidationService.getCorrectedFloweringTime();
                        String expReproductiveCondition = constructReproductiveCondition(reproductiveCondtion,getFlowertingTimeStr(correctedFloweringtime));
                        cleanedSpecimenRecord = constructCleanedSpecimenRecord(inputSpecimenRecord,expReproductiveCondition);
                    } else if (curationStatus == CurationComment.UNABLE_DETERMINE_VALIDITY || curationStatus == CurationComment.UNABLE_CURATED){
                        curationComment = CurationComment.construct(curationStatus,floweringTimeValidationService.getComment(),floweringTimeValidationService.getServiceName());
                        cleanedSpecimenRecord =  new SpecimenRecord(inputSpecimenRecord);
                    } else if (curationStatus == CurationComment.CORRECT){
                        curationComment = CurationComment.construct(CurationComment.CORRECT,floweringTimeValidationService.getComment(),floweringTimeValidationService.getServiceName());
                        cleanedSpecimenRecord =  new SpecimenRecord(inputSpecimenRecord);
                    }

                    //output
                    constructOutput(cleanedSpecimenRecord, curationComment);
                }
                Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
            }
        }
	
        //assume the flowering time information in the ReproductiveCondition is organized as: ..Flower: Jan;Feb,....
        private Vector<String> parseReproductiveCondition(String reproductiveCondtion){
            String floweringTime = "";
            int startIdx = reproductiveCondtion.indexOf("Flower:");
            int endIdx = -1;
            if(startIdx != -1){
                startIdx = reproductiveCondtion.indexOf(":", startIdx);
                endIdx = reproductiveCondtion.indexOf(",", startIdx);
                if(endIdx == -1){
                    endIdx = reproductiveCondtion.length();
                    floweringTime = reproductiveCondtion.substring(startIdx+1).trim();
                }else{
                    floweringTime = reproductiveCondtion.substring(startIdx+1,endIdx).trim();
                }
                return getMonthVector(floweringTime);
            }else{
                return null;
            }
        }

        private String constructReproductiveCondition(String reproductiveCondtion, String floweringTimeStr){
            int startIdx = reproductiveCondtion.indexOf("Flower:");
            if(startIdx == -1){
                //no flowering time before
                return reproductiveCondtion+",Flower:"+floweringTimeStr;
            }else{
                //need to replace the wrong flowering time
                int endIdx = reproductiveCondtion.indexOf(",", startIdx);
                if(endIdx == -1){
                    return reproductiveCondtion.substring(0, startIdx)+"Flower:"+floweringTimeStr+reproductiveCondtion.substring(reproductiveCondtion.length());
                }else{
                    return reproductiveCondtion.substring(0, startIdx)+"Flower:"+floweringTimeStr+reproductiveCondtion.substring(endIdx);
                }
            }
        }

        private Vector<String> getMonthVector(String flowerTimeStr){
            Vector<String> monthVector = new Vector<String>();
            String [] monthArray = flowerTimeStr.split(";");
            for(int i=0;i<monthArray.length;i++){
                monthVector.add(monthArray[i].trim());
            }
            return monthVector;
        }

        private String getFlowertingTimeStr(Vector<String> FTVector){
            String flowerTimeStr = "";
            for(int i=0;i<FTVector.size();i++){
                flowerTimeStr = flowerTimeStr + ";"+FTVector.get(i);
            }
            return flowerTimeStr.replaceFirst(";", "");
        }

        private SpecimenRecord constructCleanedSpecimenRecord(SpecimenRecord inputSpecimenRecord, String reproductiveCondition) {
            LinkedHashMap<String,String> valueMap = new LinkedHashMap<String, String>();
            Set<String> labelSet = inputSpecimenRecord.keySet();
            Iterator<String> iter = labelSet.iterator();
            while(iter.hasNext()){
                String label = iter.next();
                String value = inputSpecimenRecord.get(label);
                if(label.equals(ReproductiveConditionLabel)){
                    valueMap.put(label, reproductiveCondition);
                }else{
                    valueMap.put(label, value);
                }
            }

            return new SpecimenRecord(valueMap);
        }

        private void constructOutput(SpecimenRecord result, CurationCommentType comment) {
            if(comment!=null){
                result.put("flwtComment", comment.toString());
                result.put("flwtStatus", comment.getStatus());
            }
            listener.tell(new TokenWithProv<SpecimenRecord>(result,getClass().getSimpleName(),invoc), getContext().parent());
        }

        private String scientificNameLabel;
        private String ReproductiveConditionLabel;
    }
}
