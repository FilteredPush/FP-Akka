/**
 * DwCaWorkflow.java
 * 
 * Copyright (C) 2015 President and Fellows of Harvard College
 */
package org.filteredpush.akka.workflows;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

import org.filteredpush.akka.actors.GEORefValidator;
import org.filteredpush.akka.actors.InternalDateValidator;
import org.filteredpush.akka.actors.NewScientificNameValidator;
import org.filteredpush.akka.actors.io.CSVReader;
import org.filteredpush.akka.actors.io.MongoSummaryWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * Workflow to take a tab delimited occurrence.txt file and run it through FP 
 * data QC actors for scientific name, collecting event date, and georeference validation, 
 * and to output the result as a JSON file for post processing.
 * 
 * @author mole
 *
 */
public class DwCaWorkflow {

    public static void main(String[] args) {
        DwCaWorkflow fp = new DwCaWorkflow();
        if (fp.setup(args)) { 
           fp.calculate();
        }
    }


    /*@Option(name="-e",usage="encoding for output file")
    private String enc = "UTF-8";          */

    @Option(name="-o",usage="output JSON file")
    private String outputFilename = "output.json";

    @Option(name="-i",usage="Input occurrence.txt (tab delimited occurrence core from a DwC archive) file.")
    private String inputFilename = "occurrence.txt";
    
    @Option(name="-a",usage="Authority to check scientific names against (IPNI, IF, WoRMS, COL, GBIF, GlobalNames), default IPNI.")
    private String service = "ipni";
    
    private String serviceClass = "org.filteredpush.kuration.services.IPNIService";
    
    /**
     * Setup conditions to run the workflow.
     * @param args command line arguments
     * @return true if setup was successful, false otherwise.
     */
    public boolean setup(String[] args) {
        boolean setupOK = false;
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);
        try {
            parser.parseArgument(args);
            File inputFile = new File(inputFilename);
            if (!inputFile.canRead()) { 
                throw new CmdLineException(parser,"Can't read Input File " + inputFilename );
            }
            File outputFile = new File(outputFilename);
            if (outputFile.exists()) { 
                throw new CmdLineException(parser,"Output File Exists " + outputFilename );
            }
            
            switch(service.toUpperCase()) { 
            case "IF": 
            	serviceClass="org.filteredpush.kuration.services.IFService";
            	break;
            case "WORMS": 
            	serviceClass="org.filteredpush.kuration.services.WoRMSService";
            	break;
            case "COL": 
            	serviceClass="org.filteredpush.kuration.services.COLService";
            	break;
            case "GBIF": 
            	serviceClass="org.filteredpush.kuration.services.GBIFService";
            	break;
            case "IPNI": 
            default: 
            	serviceClass="org.filteredpush.kuration.services.IPNIService";
            }
            
            setupOK = true;
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
        return setupOK;
    }

    public void calculate() {
        this.calculate( "db", "Occurrence", 200.0);
    }

    public void calculate(
            final String fileIn,
            final String fileOut,
            //final String query,
            final Double certainty) {

        long starttime = System.currentTimeMillis();

        // Create an Akka system
        ActorSystem system = ActorSystem.create("FpSystem");
        
        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoSummaryWriter(outputFilename);
            }
        }), "JsonWriter");


        final ActorRef geoValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new GEORefValidator("org.filteredpush.kuration.services.GeoLocate3",false,certainty,writer);
            }
        }), "geoValidator");


        final ActorRef dateValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new InternalDateValidator("org.filteredpush.kuration.services.InternalDateValidationService", geoValidator);
            }
        }), "dateValidator");

        
        final ActorRef scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
            	// TODO: Need to see if this sort of picking inside create() will work 
            	// to allow choice between CSV or MongoDB input from command line parameters 
            	// letting DwCaWorkflow and MongoWorkflow be collapsed into a single workflow.
            	if (service.toUpperCase().equals("GLOBALNAMES")) { 
                    return new SciNameWorkflow("-t",false,dateValidator);
            	} else { 
                    return new NewScientificNameValidator(serviceClass,true,true,dateValidator);
            	}
            }
        }), "scinValidator");
        

        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVReader(inputFilename, scinValidator);
            }
        }), "reader");


        // start the calculation
        reader.tell(new Curate());
        system.awaitTermination();
        long stoptime = System.currentTimeMillis();
        //System.out.printf("\nTime: %f s\n",(stoptime-starttime)/1000.0);
        System.err.printf("%d",stoptime-starttime);
    }

    static class Curate {
    }
}

