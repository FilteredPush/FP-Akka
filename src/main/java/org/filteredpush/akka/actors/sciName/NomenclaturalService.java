package org.filteredpush.akka.actors.sciName;

import org.filteredpush.akka.data.Token;

import akka.actor.ActorRef;

import org.filteredpush.kuration.interfaces.INewScientificNameValidationService;
//import org.filteredpush.kuration.services.sciname.GBIFService;
import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.SpecimenRecord;

/**
 * Created by tianhong on 2/9/15.
 */
public class NomenclaturalService extends Component {

    //SpecimenRecord inputData = new SpecimenRecord();
    String validName;

    public NomenclaturalService(final ActorRef listener){
        super(listener);
    }

    @Override
    public void onReceive(Object message) {

        if (((Token) message).getData() instanceof SpecimenRecord) {
            SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
            //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());

            String sciName = record.get("scientificName");
            String author = record.get("scientificNameAuthorship");

            String kingdom = record.get("kingdom");

            INewScientificNameValidationService scientificNameService = null;

            // TODO: Specify use of services in Nomenclatural Mode
            try {
                if(kingdom.toLowerCase().equals("plantae")){
                	// Note: IPNI only covers vascular plants, but no good service for non-vascular plants
                	// IPNI is only nomenclatural
                    scientificNameService = (INewScientificNameValidationService)Class.forName("org.filteredpush.kuration.services.sciname.IPNIService").newInstance();
                }else if(kingdom.toLowerCase().equals("animalia")){
                    scientificNameService = (INewScientificNameValidationService)Class.forName("org.filteredpush.kuration.services.sciname.ZooBankNomenclaturalAct").newInstance();
                }else if(kingdom.toLowerCase().equals("fungi")){
                	// IF can be nomenclatural or taxonomic
                    scientificNameService = (INewScientificNameValidationService)Class.forName("org.filteredpush.kuration.services.sciname.IndexFungorumService").newInstance();
                }else{
                    //todo need to handle other cases and default service
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            scientificNameService.validateScientificName(sciName, author);

            if(scientificNameService.getCorrectedScientificName() == null){
                scientificNameService.validateScientificName(sciName,author);
            }
            curationComment = CurationComment.construct(scientificNameService.getCurationStatus(), scientificNameService.getComment(), scientificNameService.getServiceName());
            constructOutput(record);

            listener.tell(record, getSelf());
        }
    }
}

