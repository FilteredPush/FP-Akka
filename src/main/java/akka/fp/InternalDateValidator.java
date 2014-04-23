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

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import fp.services.IInternalDateValidationService;
import fp.util.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

/**
 * clustering on Collectors
 * then clustering on Time
 * then in each cluster, find out the outlier according to the lat/log (calculate distance between each two point)
 * 
 * @author fancy
 *
 */
public class InternalDateValidator extends UntypedActor {
    private final ActorRef listener;

    //final ActorRef workerRouter;
    //todo: use router for multiple instance

    public InternalDateValidator(String singleServiceClassQN, ActorRef listener) {
        this.listener = listener;
        this.rand = new Random();

        try {
            //initialize required label
            SpecimenRecordTypeConf speicmenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

            collectorLabel = speicmenRecordTypeConf.getLabel("RecordedBy");
            if(collectorLabel == null){
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

            eventDateLabel = speicmenRecordTypeConf.getLabel("EventDate");
            if(eventDateLabel == null){
                throw new CurrationException(getName()+" failed since the eventDate label of the SpecimenRecordType is not set.");
            }

            startDayOfYearLabel = speicmenRecordTypeConf.getLabel("StartDayOfYear");
            if(startDayOfYearLabel == null){
                throw new CurrationException(getName()+" failed since the startDayOfYearLabel label of the SpecimenRecordType is not set.");
            }

            verbatimEventDateLabel = speicmenRecordTypeConf.getLabel("VerbatimEventDate");
            if(verbatimEventDateLabel == null){
                throw new CurrationException(getName()+" failed since the verbatimEventDate label of the SpecimenRecordType is not set.");
            }

            modifiedLabel = speicmenRecordTypeConf.getLabel("Modified");
            if(modifiedLabel == null){
                throw new CurrationException(getName()+" failed since the modified label of the SpecimenRecordType is not set.");
            }

            //resolve service
            this.singleServiceClassQN = singleServiceClassQN;
            singleDateValidationService = (IInternalDateValidationService)Class.forName(this.singleServiceClassQN).newInstance();

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
        invoc = rand.nextInt();
        //for date validation for each record
        if (message instanceof TokenWithProv) {
        //if(dataLabelStr.equals(getCurrentToken().getLabel().toString())){

            SpecimenRecord inputSpecimenRecord = (SpecimenRecord)((Token) message).getData();

            //System.out.println("inputSpecimenRecord = " + inputSpecimenRecord.toString());

            String eventDate = inputSpecimenRecord.get(eventDateLabel);
            String collector = inputSpecimenRecord.get(collectorLabel);
            String year = inputSpecimenRecord.get(yearCollectedLabel);
            String month = inputSpecimenRecord.get(monthCollectedLabel);
            String day = inputSpecimenRecord.get(dayCollectedLabel);
            String startDayOfYear = inputSpecimenRecord.get(startDayOfYearLabel);
            String verbatimEventDate = inputSpecimenRecord.get(verbatimEventDateLabel);
            String modified = inputSpecimenRecord.get(modifiedLabel);


            singleDateValidationService.validateDate(eventDate, verbatimEventDate, startDayOfYear, year, month,  day, modified, collector);

            CurationCommentType curationComment = null;
            CurationStatus curationStatus = singleDateValidationService.getCurationStatus();
            if(curationStatus == CurationComment.CURATED){
                curationComment = CurationComment.construct(CurationComment.CURATED,singleDateValidationService.getComment(),singleDateValidationService.getServiceName());
                //replace the old value if curated
                inputSpecimenRecord.put("decimalLatitude", String.valueOf(singleDateValidationService.getCorrectedDate()));
            }else if(curationStatus == CurationComment.UNABLE_CURATED){
                curationComment = CurationComment.construct(CurationComment.UNABLE_CURATED,singleDateValidationService.getComment(),singleDateValidationService.getServiceName());
            }else if(curationStatus == CurationComment.UNABLE_DETERMINE_VALIDITY){
                curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,singleDateValidationService.getComment(),singleDateValidationService.getServiceName());
            }
            //output
            constructOutput(inputSpecimenRecord, curationComment);
            /*
            for (List l : singleDateValidationService.getLog()) {
                Prov.log().printf("service\t%s\t%d\t%s\t%d\t%d\t%s\t%s\n", this.getClass().getSimpleName(), invoc, (String)l.get(0), (Long)l.get(1), (Long)l.get(2),l.get(3),curationStatus.toString());
            }

        } else if (message instanceof Terminated) {
            //if (((Terminated) message).getActor().equals(workerRouter))
                this.getContext().stop(getSelf());
                */
        } else {
            unhandled(message);
        }
    }

    @Override
    public void postStop() {
        //System.out.println("Stopped ScinRefValidator");
        getSelf().tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }

    private void constructOutput(SpecimenRecord result, CurationCommentType comment) {
        if(comment!=null){
            result.put("dateComment", comment.getDetails());
            result.put("dateStatus", comment.getStatus());
            result.put("dateSource", comment.getSource());
        }
        listener.tell(new TokenWithProv<SpecimenRecord>(result,getClass().getSimpleName(),invoc),getContext().parent());
    }

    private String singleServiceClassQN;

    private String collectorLabel;
    private String yearCollectedLabel;
    private String monthCollectedLabel;
    private String dayCollectedLabel;
    private String eventDateLabel;
    private String startDayOfYearLabel;
    private String verbatimEventDateLabel;
    private String modifiedLabel;

    //for invocation id
    private final Random rand;
    private int invoc;

    IInternalDateValidationService singleDateValidationService = null;
    private LinkedList<SpecimenRecord> inputObjList = new LinkedList<SpecimenRecord>();
    private LinkedHashMap<String,TreeSet<SpecimenRecord>> inputDataMap = new LinkedHashMap<String,TreeSet<SpecimenRecord>>();
}
