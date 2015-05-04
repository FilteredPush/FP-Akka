package akka.fp.sciName;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.fp.Token;
import fp.util.CurationComment;
import fp.util.CurationStatus;
import fp.util.SpecimenRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * Created by tianhong on 2/9/15.
 */
public class NameReconciliation extends UntypedActor {

    //SpecimenRecord inputData = new SpecimenRecord();
    String validName;
    final ActorRef listener;

    public NameReconciliation(final ActorRef listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Object message) {

        if (((Token) message).getData() instanceof SpecimenRecord) {
            SpecimenRecord record = (SpecimenRecord) ((Token) message).getData();
            //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());

            checkMisspelling(record.get("sciName"));
            SpecimenRecord result = new SpecimenRecord();
            result.put("sciName", validName);
            listener.tell(result, getSelf());
        }
    }
    public HashMap checkMisspelling (String name){

        CurationStatus curationStatus = null;
        String comment = "";
        String resultName = null;
        HashMap<String, String> resultMap = new HashMap <String, String>();

        StringBuilder result = new StringBuilder();
        URL url;
        try {
            name = name.replace(" ", "+");
            url = new URL("http://resolver.globalnames.org/name_resolvers.json?names=" + name +"&resolve_once=true");
            //System.out.println(url);
            URLConnection connection = url.openConnection();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null) {
                //System.out.println("line = " + line);
                result.append(line);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        JSONParser parser = new JSONParser();
        JSONObject last = new JSONObject();
        try {
            //System.out.println("result = " + result.toString());
            JSONObject object = (JSONObject)parser.parse(result.toString());
            //System.out.println("object = " + object.toString());

            JSONArray jdata = (JSONArray)object.get("data");
            JSONObject jone = (JSONObject)jdata.get(0);
            JSONArray jresults = (JSONArray)jone.get("results");
            //if there is no possible correction, return now
            if (jresults == null){
                //comment = comment + " | the name is misspelled and cannot be corrected.";
                comment = comment + " | the provided name cannot be found in Global Name Resolver";
                resultMap.put("scientificName", null);
                resultMap.put("curationStatus", CurationComment.UNABLE_CURATED.toString());
                resultMap.put("comment", comment);
                return resultMap;
            }
            last = (JSONObject)jresults.get(0);
            //System.out.println("last = " + last.toString());

        } catch (ParseException e) {
            comment = comment + " | cannot get result from global name resolver due to error";
            resultMap.put("scientificName", null);
            resultMap.put("curationStatus", CurationComment.UNABLE_DETERMINE_VALIDITY.toString());
            resultMap.put("comment", comment);
            return resultMap;
        }
        double score = Double.parseDouble(getValFromKey(last,"score"));
        int type = Integer.parseInt(getValFromKey(last,"match_type"));
        String resolvedName = getValFromKey(last,"name_string");
    	/*
    	if (resolvedName != "" ){
    		if (includeAuthor == false)
    		System.out.println("Changed to current name: " + resolvedName +"**");
		} else {
			resolvedName = getValFromKey(last,"name_string");
		}
		*/
        //System.out.println(score);
        //System.out.println(type);
        //System.out.println(resolvedName);

        //if not exact match, print out reminder
        if (type > 2){
            if (score > 0.9){
                //System.out.println("The provided name: \"" + name + "\" is misspelled, changed to \"" + resolvedName + "\".");
                comment = comment + " | The provided name: " + name + " is misspelled, changed to " + resolvedName;
                curationStatus = CurationComment.CURATED;
            }
            else {
                //System.out.println("The provided name: \"" + name + "\" has spelling issue, changed to \"" + resolvedName + "\" for now.");
                //System.out.println("The provided name: \"" + name + "\" has spelling issue and it cannot be curated");
                curationStatus = CurationComment.UNABLE_CURATED;
                comment = comment + " | The provided name: " + name + " cannot be found in Global Name Resolver";
                resolvedName = null;
            }
        }else{
            comment = comment + " | The provided name: " + name + " is valid after checking misspelling";
            curationStatus = CurationComment.CORRECT;
        }

        validName = resolvedName;
        resultMap.put("scientificName", resolvedName);
        resultMap.put("curationStatus", curationStatus.toString());
        resultMap.put("comment", comment);
        return resultMap;
    }

    public static String getValFromKey(JSONObject json, String key) {
        if (json==null || json.get(key)==null) {
            return "";
        } else {
            return json.get(key).toString();
        }
    }
}
