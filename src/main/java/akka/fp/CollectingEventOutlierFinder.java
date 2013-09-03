/** Base class for coactors with one input and one output port.
 *
 * Copyright (c) 2008 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */

package akka.fp;

import akka.actor.*;
import fp.services.ICollectingEventIdentificationService;
import fp.util.*;
import akka.routing.Broadcast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * clustering on Collectors
 * then clustering on Time
 * then in each cluster, find out the outlier according to the lat/log (calculate distance between each two point)
 * 
 * @author fancy
 *
 */
public class CollectingEventOutlierFinder extends UntypedActor {
    private final ActorRef listener;

    //final ActorRef workerRouter;

    public CollectingEventOutlierFinder(String serviceClassQN, String dataLabelStr,
                                        String resultCollectionLabelStr, String normalDataCollectionLabelStr,
                                        String outlierCommentLabelStr, String outlierCollectionLabelStr,
                                        String outlierDataLabelStr, String normalDataLabelStr,
                                        String remoteComparatorDataLabelStr, String localComparatorDataLabelStr,
                                        boolean getRemoteEvidence, ActorRef listener) {
        this.listener = listener;
        this.dataLabelStr = dataLabelStr;
        this.resultCollectionLabelStr = resultCollectionLabelStr;
        this.normalDataCollectionLabelStr = normalDataCollectionLabelStr;
        this.outlierCommentLabelStr = outlierCommentLabelStr;
        this.outlierCollectionLabelStr = outlierCollectionLabelStr;
        this.outlierDataLabelStr = outlierDataLabelStr;
        this.normalDataLabelStr = normalDataLabelStr;
        this.localComparatorDataLabelStr = localComparatorDataLabelStr;
        this.getRemoteEvidence = getRemoteEvidence;
        this.remoteComparatorDataLabelStr = remoteComparatorDataLabelStr;

        try {
            //initialize required label
            SpecimenRecordTypeConf speicmenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

            CollectorLabel = speicmenRecordTypeConf.getLabel("RecordedBy");
            if(CollectorLabel == null){
                throw new CurrationException(getName()+" failed since the RecordedBy label of the SpecimenRecordType is not set.");
            }

            yearCollectedLabel = speicmenRecordTypeConf.getLabel("YearCollected");
            if(yearCollectedLabel == null){
                throw new CurrationException(getName()+" failed since the YearCollected label of the SpecimenRecordType is not set.");
            }

            monthCollectedLabel = speicmenRecordTypeConf.getLabel("MonthCollected");
            if(monthCollectedLabel == null){
                throw new CurrationException(getName()+" failed since the MonthCollected label of the SpecimenRecordType is not set.");
            }

            dayCollectedLabel = speicmenRecordTypeConf.getLabel("DayCollected");
            if(dayCollectedLabel == null){
                throw new CurrationException(getName()+" failed since the DayCollected label of the SpecimenRecordType is not set.");
            }

            latitudeLabel = speicmenRecordTypeConf.getLabel("DecimalLatitude");
            if(latitudeLabel == null){
                throw new CurrationException(getName()+" failed since the DecimalLatitude label of the SpecimenRecordType is not set.");
            }

            longitudeLabel = speicmenRecordTypeConf.getLabel("DecimalLongitude");
            if(longitudeLabel == null){
                throw new CurrationException(getName()+" failed since the DecimalLongitude label of the SpecimenRecordType is not set.");
            }

            //resolve service
            this.serviceClassQN = serviceClassQN;

            collectingEventoutlierIdentificationService = (ICollectingEventIdentificationService)Class.forName(this.serviceClassQN).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (CurrationException e) {
            e.printStackTrace();
        }

        // handleScopeStart
        inputObjList.clear();
        inputDataMap.clear();

        //workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
        //    @Override
        //    public Actor create() throws Exception {
        //        return new ScientificNameValidatorInvocation(service, useCache, insertLSID, listener);
        //    }
        //}).withRouter(new RoundRobinRouter(6)), "workerRouter");
        //getContext().watch(workerRouter);
    }

    public String getName() {
        return "CollectionEventOutlierFinder";
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SpecimenRecord) {
        //if(dataLabelStr.equals(getCurrentToken().getLabel().toString())){
            inputObjList.add((SpecimenRecord) message);
            addDataToken((SpecimenRecord) message);
        } else if (message instanceof Broadcast) {
            if (inputObjList.size() > 0 && inputDataMap.size() > 0) {
                //find out outlier by using the service
                collectingEventoutlierIdentificationService.identifyOutlier(inputDataMap, getRemoteEvidence);

                LinkedList<SpecimenRecord> noneOutlier = collectingEventoutlierIdentificationService.getNoneOutlier();
                LinkedHashMap<SpecimenRecord,LinkedList<SpecimenRecord>> outlierLocalComparatorMap = collectingEventoutlierIdentificationService.getOutlierLocalComparatorMap();
                LinkedHashMap<SpecimenRecord,LinkedList<SpecimenRecord>> outlierRemoteComparatorMap = collectingEventoutlierIdentificationService.getOutlierRemoteComparatorMap();
                String comment = collectingEventoutlierIdentificationService.getComment();
                CurationStatus curationStatus = collectingEventoutlierIdentificationService.getCurationStatus();

                //output
                if (curationStatus == CurationComment.UNABLE_DETERMINE_VALIDITY) {
                    HashMap<String,CurationCommentType> commentAn = new HashMap<String, CurationCommentType>();
                    commentAn.put(outlierCommentLabelStr, CurationComment.construct(curationStatus, comment, collectingEventoutlierIdentificationService.getServiceName()));

                    LinkedList<SpecimenRecord> topClusterCM = new LinkedList<SpecimenRecord>();
                    for (SpecimenRecord i : inputObjList) {
                        // TODO dependencies?
                        topClusterCM.add(i);
                    }
                    Collection<SpecimenRecord> c = new Collection<SpecimenRecord>(resultCollectionLabelStr,topClusterCM,commentAn);
                    listener.tell(c, getSender());
                } else if (curationStatus == CurationComment.CORRECT || curationStatus == CurationComment.UNABLE_CURATED){
                    LinkedList<SpecimenRecord> normal = new LinkedList<SpecimenRecord>(noneOutlier);
                    //for(int i=0;i<noneOutlier.size();i++){
                        //DependencySet cd = new DependencySet(collectionManager.getCoactor());
                        //cd.add(nontOutlier);
                    //}

                    LinkedList<Collection<SpecimenRecord>> top = new LinkedList<Collection<SpecimenRecord>>();
                    Collection<SpecimenRecord> normalCM = new Collection<SpecimenRecord>(normalDataLabelStr,normal);

                    if(curationStatus == CurationComment.UNABLE_CURATED){
                        for (SpecimenRecord outlier : outlierLocalComparatorMap.keySet()) {
                            LinkedList<SpecimenRecord> localComparatorList = outlierLocalComparatorMap.get(outlier);
                            LinkedList<SpecimenRecord> remoteComparatorList = null;
                            if(outlierRemoteComparatorMap.containsKey(outlier)){
                                remoteComparatorList = outlierRemoteComparatorMap.get(outlier);
                            }

                            HashMap<String,CurationCommentType> commentAn = new HashMap<String,CurationCommentType>();
                            commentAn.put(outlierCommentLabelStr, CurationComment.construct(curationStatus, comment, collectingEventoutlierIdentificationService.getServiceName()));

                            LinkedList<SpecimenRecord> rlOutlier = new LinkedList<SpecimenRecord>();
                            if (remoteComparatorList!=null){
                                for (SpecimenRecord r : remoteComparatorList){
                                    rlOutlier.add(r);
                                    //TODO label with remoteComparatorDataLabelStr
                                }
                            }
                            for (SpecimenRecord r: localComparatorList) {
                                //DependencySet cd = new DependencySet(collectionManager.getCoactor());
                                //cd.add(localComparator);
                                //outlierDp.add(localComparator);
                                rlOutlier.add(r);
                                //TODO: label with localComparatorDataLabelStr
                            }

                            //outlierDp.add(outlier);
                            rlOutlier.add(outlier);
                            // TODO: label with outlierDataLabelStr
                            Collection<SpecimenRecord> outlierCM = new Collection<SpecimenRecord>(outlierCollectionLabelStr, rlOutlier, commentAn);
                            top.add(outlierCM);
                        }
                    }
                    top.add(normalCM);
                    Collection<Collection<SpecimenRecord>> topClusterCM = new Collection<Collection<SpecimenRecord>>(resultCollectionLabelStr,top);
                    listener.tell(topClusterCM,getSelf());
                }
                inputObjList.clear();
                inputDataMap.clear();
            }

            //workerRouter.tell(new Broadcast(((Broadcast) message).message()), getSender());
            getSelf().tell(((Broadcast) message).message(), getSender());
        } else if (message instanceof Terminated) {
            //if (((Terminated) message).getActor().equals(workerRouter))
                this.getContext().stop(getSelf());
        } else {
            unhandled(message);
        }
    }

    @Override
    public void postStop() {
        //System.out.println("Stopped ScinRefValidator");
        getSelf().tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }
	
	private void addDataToken(SpecimenRecord dataToken){
        String collector = dataToken.get(CollectorLabel);
		TreeSet<SpecimenRecord> recordSet;
		if(inputDataMap.containsKey(collector)){
			recordSet = inputDataMap.get(collector);
		}else{
			recordSet = new TreeSet<SpecimenRecord>(new SpecimenDataTokenComparator());
			inputDataMap.put(collector, recordSet);
		}
		recordSet.add(dataToken);
	}	
	
	private class SpecimenDataTokenComparator implements Comparator<SpecimenRecord>{
		public int compare(SpecimenRecord o1, SpecimenRecord o2) {
			int year1 = Integer.parseInt(o1.get(yearCollectedLabel));
			int month1 = Integer.parseInt(o1.get(monthCollectedLabel));
			int day1 = Integer.parseInt(o1.get(dayCollectedLabel));
			long timestamp1 = getTimestamp(getFormatedDate(year1,month1,day1));

            int year2 = Integer.parseInt(o2.get(yearCollectedLabel));
            int month2 = Integer.parseInt(o2.get(monthCollectedLabel));
            int day2 = Integer.parseInt(o2.get(dayCollectedLabel));
            long timestamp2 = getTimestamp(getFormatedDate(year2,month2,day2));
			
			if(timestamp1>timestamp2){
				return 1;
			}else{
				//if they're equal, they're treated as less
				return -1;
			}
		}	
	}	

	private long getTimestamp(String dateStr){	
		//date is in format of mm-dd-yyyy
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		Date date;
		try {
			date = format.parse(dateStr);
			return date.getTime();
		} catch (ParseException e) {
			// shouldn't happen
			e.printStackTrace();
		}
		return 0;
	}	
	
	private String getFormatedDate(int year, int month, int day){
		//assume year is four digit
		String yearStr = String.valueOf(year);

		String monthStr = String.valueOf(month);		
		if(month<10){
			monthStr = "0"+monthStr;
		}

		String dayStr = String.valueOf(day);		
		if(day<10){
			dayStr = "0"+dayStr;
		}
		
		return monthStr+"-"+dayStr+"-"+yearStr;
	}	

	private String serviceClassQN;
    private String dataLabelStr = null; 
    private String resultCollectionLabelStr = null;
    private String normalDataCollectionLabelStr = null;
    private String outlierCommentLabelStr = null;
    private String outlierCollectionLabelStr = null;
    private String outlierDataLabelStr = null;
    private String normalDataLabelStr = null;
    private String localComparatorDataLabelStr = null;
    private boolean getRemoteEvidence = false;
    private String remoteComparatorDataLabelStr = null;    
    
    private String CollectorLabel;
    private String yearCollectedLabel;
    private String monthCollectedLabel;
    private String dayCollectedLabel;
    private String latitudeLabel;
    private String longitudeLabel;
    
    ICollectingEventIdentificationService collectingEventoutlierIdentificationService = null;
    private LinkedList<SpecimenRecord> inputObjList = new LinkedList<SpecimenRecord>();
    private LinkedHashMap<String,TreeSet<SpecimenRecord>> inputDataMap = new LinkedHashMap<String,TreeSet<SpecimenRecord>>();

    private static final long serialVersionUID = 1L;
}
