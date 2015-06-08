package org.filteredpush.akka.actors.sciName;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.CurationCommentType;
import org.filteredpush.kuration.util.CurationStatus;
import org.filteredpush.kuration.util.SpecimenRecord;

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
        if(!record.containsKey(SpecimenRecord.SciName_Status_Label)){
            record.put(SpecimenRecord.SciName_Status_Label, status);
            record.put(SpecimenRecord.SciName_Comment_Label, curationComment.getDetails());
            record.put(SpecimenRecord.SciName_Source_Label, curationComment.getSource());
        }else{

            // overwrite previous status if the current one is worse
            if(getRank(status) > getRank(record.get(SpecimenRecord.SciName_Status_Label))) record.put(SpecimenRecord.SciName_Status_Label, status);

            record.put(SpecimenRecord.SciName_Comment_Label, record.get(SpecimenRecord.SciName_Comment_Label) + " | " + curationComment.getDetails());
            record.put(SpecimenRecord.SciName_Source_Label, record.get(SpecimenRecord.SciName_Source_Label) + " | " + curationComment.getSource());
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

}
