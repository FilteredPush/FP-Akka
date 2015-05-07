package akka.fp.sciName;

import akka.actor.ActorRef;
import akka.fp.Token;
import fp.services.INewScientificNameValidationService;
import fp.util.CurationComment;
import fp.util.SpecimenRecord;

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
                scientificNameService = (INewScientificNameValidationService)Class.forName("fp.services.COLService").newInstance();
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
