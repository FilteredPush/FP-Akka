package org.filteredpush.akka.actors.sciName;

import org.filteredpush.akka.data.Token;

import akka.actor.ActorRef;
import org.filteredpush.kuration.interfaces.INewScientificNameValidationService;
import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.SpecimenRecord;

/**
 * Created by tianhong on 2/9/15.
 */
public class TaxonomicService extends Component {

    //SpecimenRecord inputData = new SpecimenRecord();
    String validName;

    public TaxonomicService(final ActorRef listener){
        super(listener);
    }

    @Override
    public void onReceive(Object message){

        if (((Token) message).getData() instanceof SpecimenRecord) {
            SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
            //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());

            String sciName = record.get("scientificName");
            String author = record.get("scientificNameAuthorship");

            

            INewScientificNameValidationService scientificNameService = null;
            try {
            	//TODO: Select a service 
            	//TODO: Use WoRMS, IF, GBIF in taxonomic mode
                scientificNameService = (INewScientificNameValidationService)Class.forName("org.filteredpush.kuration.services.test.COLService").newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            scientificNameService.validateScientificName(sciName, author);


            curationComment = CurationComment.construct(scientificNameService.getCurationStatus(), scientificNameService.getComment(), scientificNameService.getServiceName());
            listener.tell( constructOutput(record), getSelf());
        }
    }

}
