package akka.fp.sciName;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.fp.Token;
import fp.util.CurationComment;
import fp.util.CurationCommentType;
import fp.util.CurationStatus;
import fp.util.SpecimenRecord;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.nameparser.NameParser;
import org.gbif.nameparser.UnparsableException;
import akka.fp.sciName.Component;


import java.util.HashMap;

/**
 * Created by tianhong on 2/9/15.
 */
public class checkNameInconsistency extends Component {

    //SpecimenRecord inputData = new SpecimenRecord();

    private static int count=0;


    public checkNameInconsistency(final ActorRef listener){
        super(listener);
    }

    @Override
    public void onReceive(Object message){

        if (message instanceof SpecimenRecord) {
            //System.out.println("count = " + count++);
            //SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
            SpecimenRecord record = (SpecimenRecord) message;
            //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());

            /*
            //if missing, let it run, handle the error in service
            String country = record.get("country");
            String stateProvince = record.get("stateProvince");
            String county = record.get("county");
            String locality = record.get("locality");
            */

            //todo: need to change to parsing a configuration
            checkConsistencyToAtomicField(record.get("sciName"), record.get("genus"), record.get("subgenus"), record.get("specificEpithet"), record.get("verbatimTaxonRank"), record.get("taxonRank"), record.get("infraspecificEpithet"));

            listener.tell(constructOutput(record), getSelf());
        }
    }

    public HashMap<String, String> checkConsistencyToAtomicField(String scientificName, String genus, String subgenus, String specificEpithet, String verbatimTaxonRank, String taxonRank, String infraspecificEpithet){
        CurationStatus curationStatus = null;
        String comment = "";
        String name = "";
        HashMap<String, String> resultMap = new HashMap <String, String>();

        if (genus != null && !genus.equals("") && specificEpithet != null && !specificEpithet.equals("") && infraspecificEpithet != null && !infraspecificEpithet.equals("")){
            String constructName= genus + " ";

            if (subgenus != null){
                if(!subgenus.equals("")) constructName += subgenus + " ";
            }
            if (!specificEpithet.equals("")) constructName = constructName + specificEpithet + " ";

            if (verbatimTaxonRank != null) constructName = constructName + verbatimTaxonRank + " ";
            else if (taxonRank != null) constructName = constructName + taxonRank + " ";

            constructName = constructName + infraspecificEpithet;


            NameParser parser = new NameParser();
            ParsedName pn = null;
            ParsedName cn = null;
            //System.out.println("scientificName = " + scientificName);
            //System.out.println("constructName = " + constructName);
            try {

                //System.out.println("constructName111 = " + constructName.trim());
                //System.out.println("scientificName = " + scientificName.equals(constructName.trim()));
                pn = parser.parse(scientificName);
                cn = parser.parse(constructName.trim());
            } catch (UnparsableException e) {
                System.out.println("Parsing error: " + e);
            }

            /*   start

            if(cn != null && pn != null && !cn.getGenusOrAbove().equals(pn.getGenusOrAbove())){
                //add the following line in order to handle dwc:genus and dwc:subgenus
                //check against global name resolver to check whether this genus exist
                HashMap<String, String> result2 = SciNameServiceUtilCopy.checkMisspelling(pn.getGenusOrAbove());
                CurationStatus returnedStatus = new CurationStatus(result2.get("curationStatus"));
                if(returnedStatus.equals(CurationComment.CORRECT) || returnedStatus.equals(CurationComment.CURATED)){
                    pn.setGenusOrAbove(result2.get("scientificName"));
                    comment = comment + "| Genus in SciName is not consistent to atomic field, genus has been changed to dwc:Genus: \"" + genus + "\"";
                    //todo: need to handle overwritten status
                }else{
                    cn.setGenusOrAbove(pn.getGenusOrAbove());
                    comment += " | Genus in SciName is not consistent to atomic field, but dwc:Genus: \"" + genus + "\" cannot be found in Global Name Resolver";
                }
            }

            end */


            if(pn == null){
                //if(pn.equals(cn)){
                if(cn == null){
                    curationStatus = CurationComment.UNABLE_DETERMINE_VALIDITY;
                    comment = comment + "| cannot get a valid scientificName from the record";
                    name = null;
                    //name =  pn.canonicalName();
                    //validatedAuthor = pn.getAuthorship();
                } else{
                    //validatedAuthor = null;
                    curationStatus = CurationComment.CURATED;
                    comment = comment + "| scientificName is constructed from atomic fields";
                    name = null;
                }
            }else{
                if(pn.equals(cn)){
                    curationStatus = CurationComment.CORRECT;
                    comment = comment + "| scientificName is consistent with atomic fields";
                    name = pn.canonicalName();
                    //validatedAuthor = pn.getAuthorship();
                } else{

                    if(cn != null){
                        //validatedAuthor = null;

                        curationStatus = CurationComment.UNABLE_CURATED;
                        comment = comment + "| scientificName is inconsistent with atomic fields";
                        name = null;
                    }else{


                        curationStatus = CurationComment.UNABLE_DETERMINE_VALIDITY;
                        name =  scientificName;
                    }
                }
            }

        }else{
            curationStatus = CurationComment.UNABLE_DETERMINE_VALIDITY;
            comment = comment + "| can't construct sciName from atomic fields";
            name =  scientificName;
        }



        curationComment = CurationComment.construct(curationStatus,comment,null);

        /*
        resultMap.put("scientificName", name);
        resultMap.put("curationStatus", curationStatus.toString());
        resultMap.put("comment", comment);
        resultMap.put("source", null);
        return resultMap;
        */
        //return constructOutput(record, name, curationStatus.toString(), comment, null );
        return resultMap;
    }
}
