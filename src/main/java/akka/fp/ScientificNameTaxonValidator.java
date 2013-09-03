package akka.fp;

import akka.actor.UntypedActor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Scientific Name Validator that validates a scientific name in a taxon record.
 * 
 * @author Lei Dou
 * @author Paul J. Morris
 *
 */
public class ScientificNameTaxonValidator extends UntypedActor {

    /*
	public ScientificNameTaxonValidator(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        
        super(container, name);

        serviceClassPathParam = Parameters.stringParameter(this, "ServiceClassPath", "");
        serviceClassQNParam = Parameters.stringParameter(this, "ServiceClassQN", "");
        hasDataCacheParam = Parameters.booleanParameter(this, "hasDataCache", false);
    	dataCacheFileParam = Parameters.stringParameter(this, "dataCacheFile", "");
    	insertLSIDParam = Parameters.booleanParameter(this, "InsertLSID", false);
        
        createSignatureElementParameter("TaxonRecord", "TaxonRecordType", true);

        createSignatureElementParameter("CleanedTaxonRecord", "TaxonRecordType", false);
        createSignatureElementParameter("Comment", "CurationCommentType?", false);
    }	

	public void initialize() throws IllegalActionException {
		super.initialize();
		
		//initialize InsertLSID
		BooleanToken insertLSIDToken = (BooleanToken)insertLSIDParam.getToken();
		insertLSID = insertLSIDToken.booleanValue();

		//initialize required label
		TaxonRecordTypeConf taxonRecordTypeConf = TaxonRecordTypeConf.INSTANCE;		
		
		scientificNameLabel = taxonRecordTypeConf.getLabel("ScientificName");
		if(scientificNameLabel == null){
			throw new IllegalActionException(getName()+" failed since the ScientificName label of the TaxonRecordType is not set.  The taxon record type xml definition must include a label 'ScientificName'."); 
		}
		
		authorLabel = taxonRecordTypeConf.getLabel("ScientificNameAuthorship");
		if(authorLabel == null){
			throw new IllegalActionException(getName()+" failed since the ScientificNameAuthorship label of the TaxonRecordType is not set.  The taxon record type xml definition must include a label 'ScientificNameAuthorship'.");
		}
		
		if(insertLSID){
			LSIDLabel = taxonRecordTypeConf.getLabel("IdentificationTaxon");
			// TODO: Is the following test redundant on purpose or an error?   Looks like it should be IdentificationTaxon
			if(authorLabel == null){
				throw new IllegalActionException(getName()+" failed since the ScientificNameAuthorship label of the TaxonRecordType is not set.");
			}			
		}
		
		//resolve service
		StringToken serviceClassPathToken =  (StringToken)serviceClassPathParam.getToken();
		if(serviceClassPathToken == null ||
				serviceClassPathToken.stringValue()==null ||
				serviceClassPathToken.stringValue().trim().equals("")){
			 throw new IllegalActionException(getName()+" failed since the ServiceClassPath parameter is not set.  Expect value such as property(\"KURATION_SPECIMENCURATION_DIR\")+\"function\"");
		}
		serviceClassPath = serviceClassPathToken.stringValue().trim();
		
		StringToken serviceClassQNToken =  (StringToken)serviceClassQNParam.getToken();
		if(serviceClassQNToken == null ||
				serviceClassQNToken.stringValue()==null ||
				serviceClassQNToken.stringValue().trim().equals("")){
			 throw new IllegalActionException(getName()+" failed since the ServiceClassPathQN parameter is not set.  Expect value such as: org.kepler.actor.SpecimenQC.service.IPNIService");
		}
		serviceClassQN = serviceClassQNToken.stringValue().trim();		
		
		scientificNameService = (IScientificNameValidationService)ClassResolver.resolve(serviceClassPath, serviceClassQN, "org.kepler.actor.SpecimenQC.IScientificNameValidationService");
		
		//import from cached file if there's a cache
		hasDataCache = ((BooleanToken)hasDataCacheParam.getToken()).booleanValue();		
		if(hasDataCache){
			Token dataCacheFileToken = dataCacheFileParam.getToken();
			if(dataCacheFileToken == null || 
				((StringToken)dataCacheFileToken).stringValue()==null || 
				((StringToken)dataCacheFileToken).stringValue().trim().equals("")){
				throw new IllegalActionException(getName()+" failed since the dataCacheFile is not set where the hasDataCache is true.");
			}
			
			String dataCacheFileStr = ((StringToken)dataCacheFileToken).stringValue().trim();			
			scientificNameService.setCacheFile(dataCacheFileStr);
		}
	}	
	 */
    @Override
    public void onReceive(Object o) throws Exception {
        //TODO
    }
    /*
	@Override
	public DataBindingValueMap fireActor(DataBindingValueMap inputDataMap) throws IllegalActionException{
		TaxonRecordType inputSpecimenRecord = (TaxonRecordType)inputDataMap.get("TaxonRecord");
		
		Token scientificNameToken = inputSpecimenRecord.get(scientificNameLabel);
		if(scientificNameToken == null){
			CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,scientificNameLabel+" is missing in the incoming SpecimenRecord",getName());
			return constructOutput(inputSpecimenRecord,curationComment);	
		}
		String scientificName = ((StringToken)scientificNameToken).stringValue().trim();		
		
		Token authorToken = inputSpecimenRecord.get(authorLabel);
		if(authorToken == null){
			CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,authorLabel+" is missing in the incoming SpecimenRecord",getName());
			return constructOutput(inputSpecimenRecord,curationComment);	
		}		
		String author = ((StringToken)authorToken).stringValue().trim();

		// Work out the rank of the provided name.
		String rank = "";
		if (scientificName.equals(((StringToken)inputSpecimenRecord.get("Family")).stringValue().trim())) { 
			rank = "f";
		}
		if (scientificName.equals(((StringToken)inputSpecimenRecord.get("Genus")).stringValue().trim())) { 
			rank = "g";
		}
		
		String kingdom = "";
		if (inputSpecimenRecord.get("Kingdom")!=null) { 
		    kingdom = ((StringToken)inputSpecimenRecord.get("Kingdom")).stringValue().trim();
		}
		String phylum = "";
		if (inputSpecimenRecord.get("Phylum")!=null) { 
			phylum = ((StringToken)inputSpecimenRecord.get("Phylum")).stringValue().trim();
		}
		String tclass = "";
		if (inputSpecimenRecord.get("Class")!=null) { 
			tclass = ((StringToken)inputSpecimenRecord.get("Class")).stringValue().trim();
		}
		
		System.out.println("Before Validate " + scientificName);
		scientificNameService.validateScientificName(scientificName, author, rank, kingdom, phylum, tclass);
		System.out.println("After Validate " + scientificName);
		
		TaxonRecordType cleanedSpecimenRecord = inputSpecimenRecord;
		CurationCommentType curationComment = null;
		String addLSIDComment = "";
		if(insertLSID){
			addLSIDComment = "Add LSID from service "+scientificNameService.getServiceName()+".";
		}
		CurationStatus curationStatus = scientificNameService.getCurationStatus();		
		System.out.println("CurationStatus after Validation is: " + curationStatus.toString());
		if(curationStatus == CurationComment.CORRECT){
			cleanedSpecimenRecord = constructCleanedTaxonRecord(inputSpecimenRecord, (IScientificNameValidationService)scientificNameService);
			curationComment = CurationComment.construct(CurationComment.CURATED,"Got name from Service. " + addLSIDComment,getName());
		}else if(curationStatus == CurationComment.CURATED){
			if(insertLSID){
				cleanedSpecimenRecord = constructCleanedTaxonRecord(inputSpecimenRecord,(IScientificNameValidationService)scientificNameService);
			}else{
				cleanedSpecimenRecord = constructCleanedTaxonRecord(inputSpecimenRecord,(IScientificNameValidationService)scientificNameService);
			}
			curationComment = CurationComment.construct(CurationComment.CURATED,scientificNameService.getComment()+addLSIDComment,getName());
		}else if(curationStatus == CurationComment.UNABLE_CURATED){
			curationComment = CurationComment.construct(CurationComment.UNABLE_CURATED,scientificNameService.getComment(),getName());
		}else if(curationStatus == CurationComment.UNABLE_DETERMINE_VALIDITY){
			curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,scientificNameService.getComment(),getName());
		}

		//output 
		return constructOutput(cleanedSpecimenRecord, curationComment);
	}
	
	public void wrapup() throws IllegalActionException {
		super.wrapup();
		if(hasDataCache){
			scientificNameService.flushCacheFile();
		}		
	}	
	
	private TaxonRecordType constructCleanedTaxonRecord(TaxonRecordType inputTaxonRecord, IScientificNameValidationService scientificNameService) throws IllegalActionException{
		
	    String scientificName = scientificNameService.getCorrectedScientificName();
	    String author = scientificNameService.getCorrectedAuthor();
	    String lsid = scientificNameService.getLSID();
	    String curationStatus = "";
	       if (scientificNameService.getCurationStatus()!=null) { 
	           curationStatus = scientificNameService.getCurationStatus().toString();
	       }
		LinkedHashMap<String,Token> valueMap = new LinkedHashMap<String, Token>();
		Set<String> labelSet = inputTaxonRecord.labelSet();
		Iterator<String> iter = labelSet.iterator();
		while(iter.hasNext()){
			String label = iter.next();
			Token value = inputTaxonRecord.get(label);
			
			if(label.equals(scientificNameLabel)){
				valueMap.put(label, new StringToken(scientificName));
			}else if(label.equals(authorLabel)){
				valueMap.put(label, new StringToken(author));
			}else if (label.equals("ScientificNameGUID")) { 
				valueMap.put(label, new StringToken(lsid));
			}else if (label.equals("CurationStatus")) { 
				valueMap.put(label, new StringToken(curationStatus));
			}else if (label.equals("Kingdom")) { 
				System.out.println("Kingdom was " + value.toString() + " is " + scientificNameService.getFoundKingdom());
				if (value.toString().equals("\"\"")) { 
				   valueMap.put(label, new StringToken(scientificNameService.getFoundKingdom()));
				} else { 
				    valueMap.put(label, value);
				}
			}else if (label.equals("Phylum")) { 
				if (value.toString().equals("\"\"")) { 
				   valueMap.put(label, new StringToken(scientificNameService.getFoundPhylum()));
				} else { 
				    valueMap.put(label, value);
				}
			}else if (label.equals("Class")) { 
				if (value.toString().equals("\"\"")) { 
				   valueMap.put(label, new StringToken(scientificNameService.getFoundClass()));
				} else { 
				    valueMap.put(label, value);
				}
			}else if (label.equals("Order")) { 
				if (value.toString().equals("\"\"")) { 
				   valueMap.put(label, new StringToken(scientificNameService.getFoundOrder()));
				} else { 
				    valueMap.put(label, value);
				}
			}else if (label.equals("Family")) { 
				if (value.toString().equals("\"\"")) { 
				   valueMap.put(label, new StringToken(scientificNameService.getFoundFamily()));
				} else { 
				    valueMap.put(label, value);
				}
			} else { 
				valueMap.put(label, value);
			}	
			if (valueMap.get(label)==null) { 
				valueMap.remove(label);
				valueMap.put(label, new StringToken(""));
			}
            System.out.println("Validator valueMap:" + label + "," + valueMap.get(label));
		}
		if (!valueMap.containsKey("ScientificNameGUID") && valueMap.size()>0) { 
			if (lsid!=null && lsid.length()>0) {
				System.out.println("Adding guid to results: " + lsid);
				valueMap.put("ScientificNameGUID", new StringToken(lsid));
			}
		}
		if (!valueMap.containsKey("CurationStatus") && valueMap.size()>0) { 
			if (curationStatus!=null && curationStatus.length()>0) {
				System.out.println("Adding curationStatus to results: " + curationStatus);
				valueMap.put("CurationStatus", new StringToken(curationStatus));
			}
		}

		try {
			return new TaxonRecordType(valueMap);
		} catch (ParseException e) {
			throw new IllegalActionException(getName()+" failed to construct CleanedTaxonRecord for "+e.getMessage());
		}
	}
	
	private DataBindingValueMap constructOutput(TaxonRecordType result, CurationCommentType comment) throws IllegalActionException{	
		DataBindingValueMap outputData = new DataBindingValueMap();	
		outputData.put("CleanedTaxonRecord",result);
		
		if(comment!=null){
			outputData.put("Comment",comment);
		}
		
		outputData.setDependOnlyOnCurrentFiring();
		
		return outputData;
	}

	Parameter serviceClassPathParam;
	Parameter serviceClassQNParam;
	Parameter hasDataCacheParam;
	Parameter dataCacheFileParam;
	Parameter insertLSIDParam;
	
	private String serviceClassPath;
	private String serviceClassQN;
	private boolean insertLSID;
	private boolean hasDataCache;
	
	private String scientificNameLabel;	
	private String authorLabel;
	private String LSIDLabel; 
	
	private IScientificNameValidationService scientificNameService;
		
	private static final long serialVersionUID = 1L;
	*/
}
