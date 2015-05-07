package akka.fp.sciName;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import fp.util.CurationComment;
import fp.util.CurationCommentType;
import fp.util.CurationStatus;
import fp.util.SpecimenRecord;

/**
 * Created by tianhong on 2/9/15.
 */
public abstract class Component extends UntypedActor{

    SpecimenRecord dataRecord;
    CurationCommentType curationComment;
    ActorRef listener;
    String validAuthor;
    String validName;

    public Component (ActorRef listener){
        this.listener = listener;
    }

    public SpecimenRecord constructOutput(SpecimenRecord record){
        String status = curationComment.getStatus();
        //if it's the first component in the subworkflow
        if(!record.containsKey("scinStatus")){
            record.put("scinStatus", status);
            record.put("scinComment", curationComment.getDetails());
            record.put("scinSource", curationComment.getSource());
        }else{

            // overwrite previous status if the current one is worse
            if(getRank(status) > getRank(record.get("scinStatus"))) record.put("scinStatus", status);

            record.put("scinComment", record.get("scinComment") + " | " + curationComment.getDetails());
            record.put("scinSource", record.get("scinSource") + " | " + curationComment.getSource());
        }

        if(status.equals(CurationComment.CURATED.toString()) || status.equals(CurationComment.Filled_in.toString())){
            record.put("scientificName", validName);
            record.put("scientificNameAuthorship", validAuthor);
        }

        return record;
    }

    private int getRank(String status){
        if(status.equals(CurationComment.CORRECT.toString())) return 1;
        else if(status.equals(CurationComment.UNABLE_DETERMINE_VALIDITY.toString())) return 2;
        else if(status.equals(CurationComment.Filled_in.toString())) return 3;
        else if(status.equals(CurationComment.CURATED.toString())) return 4;
        else if(status.equals(CurationComment.UNABLE_CURATED.toString())) return 5;
        else{
            System.out.println("InValid status in SciName subworkflow component: " + status);
            return -1;
        }
    }

    //abstract public void run();

    //public SpecimenRecord constructOutput(String name, String author, String status, String comment, String source){

    //}

    public SpecimenRecord getOutput(){
        return dataRecord;
    }

    public void postStop() {
        System.out.println("Stopped SciNameValidator");
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }

}
