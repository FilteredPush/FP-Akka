package akka.fp.sciName;

import akka.actor.ActorRef;
import fp.util.CurationComment;
import fp.util.CurationStatus;
import fp.util.SpecimenRecord;

import org.filteredpush.akka.data.Token;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.nameparser.NameParser;
import org.gbif.nameparser.UnparsableException;

import java.util.HashMap;

/**
 * Created by tianhong on 2/9/15.
 */

//todo not in use right now

public class RemoteService extends Component {

    //SpecimenRecord inputData = new SpecimenRecord();
    String validName;

    public RemoteService(final ActorRef listener){
        super(listener);
    }

    @Override
    public void onReceive(Object message){

        if (((Token) message).getData() instanceof SpecimenRecord) {
            SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
            //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());

            /*
            //if missing, let it run, handle the error in service
            String country = record.get("country");
            String stateProvince = record.get("stateProvince");
            String county = record.get("county");
            String locality = record.get("locality");
            */

            //todo: need to change to parsing a configuration
           // checkConsistencyToAtomicField(record.get("sciName"), record.get("genus"), record.get("subgenus"), record.get("specificEpithet"), record.get("verbatimTaxonRank"), record.get("taxonRank"), record.get("infraspecificEpithet"));
            SpecimenRecord result = new SpecimenRecord();
            result.put("sciName", validName);
            listener.tell(result, getSelf());
        }
    }

}
