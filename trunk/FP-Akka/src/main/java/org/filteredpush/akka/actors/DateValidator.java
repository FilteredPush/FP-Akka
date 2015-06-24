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

package org.filteredpush.akka.actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;

import org.filteredpush.kuration.interfaces.IExternalDateValidationService;
import org.filteredpush.kuration.interfaces.IInternalDateValidationService;
import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.CurationCommentType;
import org.filteredpush.kuration.util.CurationException;
import org.filteredpush.kuration.util.CurationStatus;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.filteredpush.kuration.util.SpecimenRecordTypeConf;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;

/**
 * clustering on Collectors
 * then clustering on Time
 * then in each cluster, find out the outlier according to the lat/log (calculate distance between each two point)
 * 
 * @author fancy
 *
 */
public class DateValidator extends UntypedActor {
    private final ActorRef listener;

    //final ActorRef workerRouter;

    public DateValidator(String serviceClassQN, String dataLabelStr,
                                String resultCollectionLabelStr, String normalDataCollectionLabelStr,
                                String outlierCommentLabelStr, String outlierCollectionLabelStr,
                                String outlierDataLabelStr, String normalDataLabelStr,
                                String remoteComparatorDataLabelStr, String localComparatorDataLabelStr,
                                boolean getRemoteEvidence, ActorRef listener) {
        this.listener = listener;
        this.rand = new Random();

        try {
            //initialize required label
            SpecimenRecordTypeConf speicmenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

            collectorLabel = speicmenRecordTypeConf.getLabel("RecordedBy");
            if(collectorLabel == null){
                throw new CurationException(getName()+" failed since the RecordedBy label of the SpecimenRecordType is not set.");
            }

            yearCollectedLabel = speicmenRecordTypeConf.getLabel("YearCollected");
            if(yearCollectedLabel == null){
                throw new CurationException(getName()+" failed since the YearCollected label of the SpecimenRecordType is not set.");
            }

            monthCollectedLabel = speicmenRecordTypeConf.getLabel("MonthCollected");
            if(monthCollectedLabel == null){
                throw new CurationException(getName()+" failed since the MonthCollected label of the SpecimenRecordType is not set.");
            }

            dayCollectedLabel = speicmenRecordTypeConf.getLabel("DayCollected");
            if(dayCollectedLabel == null){
                throw new CurationException(getName()+" failed since the DayCollected label of the SpecimenRecordType is not set.");
            }

            latitudeLabel = speicmenRecordTypeConf.getLabel("DecimalLatitude");
            if(latitudeLabel == null){
                throw new CurationException(getName()+" failed since the DecimalLatitude label of the SpecimenRecordType is not set.");
            }

            longitudeLabel = speicmenRecordTypeConf.getLabel("DecimalLongitude");
            if(longitudeLabel == null){
                throw new CurationException(getName()+" failed since the DecimalLongitude label of the SpecimenRecordType is not set.");
            }

            eventDateLabel = speicmenRecordTypeConf.getLabel("EventDate");
            if(longitudeLabel == null){
                throw new CurationException(getName()+" failed since the eventDate label of the SpecimenRecordType is not set.");
            }

            startDayOfYearLabel = speicmenRecordTypeConf.getLabel("startDayOfYear");
            if(startDayOfYearLabel == null){
                throw new CurationException(getName()+" failed since the startDayOfYearLabel label of the SpecimenRecordType is not set.");
            }

            verbatimEventDateLabel = speicmenRecordTypeConf.getLabel("startDayOfYear");
            if(verbatimEventDateLabel == null){
                throw new CurationException(getName()+" failed since the verbatimEventDate label of the SpecimenRecordType is not set.");
            }

            modifiedLabel = speicmenRecordTypeConf.getLabel("startDayOfYear");
            if(modifiedLabel == null){
                throw new CurationException(getName()+" failed since the modified label of the SpecimenRecordType is not set.");
            }

            //resolve service
            this.singleServiceClassQN = singleServiceClassQN;
            this.outlierServiceClassQN = outlierServiceClassQN;

            //collectingEventoutlierIdentificationService = (ICollectingEventIdentificationService)Class.forName(this.serviceClassQN).newInstance();
            internalSingleDateValidationService = (IInternalDateValidationService)Class.forName(this.singleServiceClassQN).newInstance();
            externalSingleDateValidationService = (IExternalDateValidationService)Class.forName(this.outlierServiceClassQN).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (CurationException e) {
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
        invoc = rand.nextInt();
        //for date validation for each record
        if (message instanceof SpecimenRecord) {
        //if(dataLabelStr.equals(getCurrentToken().getLabel().toString())){

            SpecimenRecord inputSpecimenRecord = (SpecimenRecord)((Token) message).getData();

            //System.out.println("inputSpecimenRecord = " + inputSpecimenRecord.toString());

            String eventDate = inputSpecimenRecord.get(eventDateLabel);
            if(eventDate == null){
                CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY, "EventDate is missing in the incoming SpecimenRecord","");
                constructOutput(new SpecimenRecord(inputSpecimenRecord),curationComment);
                return;
            }
            String collector = inputSpecimenRecord.get(collectorLabel);
            String year = inputSpecimenRecord.get(yearCollectedLabel);
            String month = inputSpecimenRecord.get(monthCollectedLabel);
            String day = inputSpecimenRecord.get(dayCollectedLabel);
            String startDayOfYear = inputSpecimenRecord.get(startDayOfYearLabel);
            String longitude = inputSpecimenRecord.get(longitudeLabel);
            String latitude = inputSpecimenRecord.get(latitudeLabel);
            String verbatimEventDate = inputSpecimenRecord.get(verbatimEventDateLabel);
            String modified = inputSpecimenRecord.get(modifiedLabel);


            internalSingleDateValidationService.validateDate(eventDate, verbatimEventDate, startDayOfYear, month, year, day, modified, collector);


            CurationStatus curationStatus = internalSingleDateValidationService.getCurationStatus();
            if(curationStatus == CurationComment.CURATED || curationStatus == CurationComment.FILLED_IN){
                inputSpecimenRecord.put("eventDate", internalSingleDateValidationService.getCorrectedDate());
            }

            //todo: unfinished case selection and form output
            CurationCommentType curationComment = CurationComment.construct(curationStatus,internalSingleDateValidationService.getComment(),internalSingleDateValidationService.getServiceName());
            constructOutput(inputSpecimenRecord, curationComment);

            //todo: no external for now
        }else if (message instanceof Broadcast){
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
        System.out.println("Stopped DateValidator");
        getSelf().tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }

    private void constructOutput(SpecimenRecord result, CurationCommentType comment) {
        if (comment != null) {
            result.put("dateComment",comment.getDetails());
            result.put("dateStatus",comment.getStatus());
            result.put("dateSource",comment.getSource());
        } else {
            result.put("dateStatus",CurationComment.CORRECT.toString());
            result.put("dateComment","None");
            result.put("dateSource",comment.getSource());
        }

        listener.tell(new TokenWithProv<SpecimenRecord>(result,getClass().getSimpleName(),invoc),getContext().parent());
    }

	private String outlierServiceClassQN;
    private String singleServiceClassQN;
    
    private String collectorLabel;
    private String yearCollectedLabel;
    private String monthCollectedLabel;
    private String dayCollectedLabel;
    private String latitudeLabel;
    private String longitudeLabel;
    private String eventDateLabel;
    private String startDayOfYearLabel;
    private String verbatimEventDateLabel;
    private String modifiedLabel;

    //for invocation id
    private final Random rand;
    private int invoc;

    IExternalDateValidationService externalSingleDateValidationService = null;
    IInternalDateValidationService internalSingleDateValidationService = null;
    private LinkedList<SpecimenRecord> inputObjList = new LinkedList<SpecimenRecord>();
    private LinkedHashMap<String,TreeSet<SpecimenRecord>> inputDataMap = new LinkedHashMap<String,TreeSet<SpecimenRecord>>();

    private static final long serialVersionUID = 1L;
}
