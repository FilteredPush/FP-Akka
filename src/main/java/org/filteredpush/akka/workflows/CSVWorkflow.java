/**
 * Copyright (C) 2009-2012 Typesafe Inc. <http://www.typesafe.com>
 */

package org.filteredpush.akka.workflows;


import akka.actor.*;

import org.filteredpush.akka.actors.GEORefValidator;
import org.filteredpush.akka.actors.InternalDateValidator;
import org.filteredpush.akka.actors.io.CSVReader;
import org.filteredpush.akka.actors.io.MongoSummaryWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;

public class CSVWorkflow {

    public static void main(String[] args) {
        CSVWorkflow fp = new CSVWorkflow();
        if (fp.setup(args)) { 
           fp.calculate();
        }
    }


    /*@Option(name="-e",usage="encoding for output file")
    private String enc = "UTF-8";          */

    @Option(name="-o",usage="output JSON file")
    //private String out = "/Users/cobalt/X31out.txt";
    //private String outputFilename = "/home/tianhong/data/akka/2011Demo_out.csv";
    //private String outputFilename = "/home/tianhong/data/akka/test.json";
    private String outputFilename = "/home/thsong/data/scan_data/test.json";

    @Option(name="-i",usage="Input CSV file")
    private String inputFilename = "/home/thsong/data/scan_data/tt.txt";
    
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
            if(args==null || args.length==0)
                throw new CmdLineException(parser,"No arguments were provided, you must specify -i and -o.");
            parser.parseArgument(args);
            File inputFile = new File(inputFilename);
            if (!inputFile.canRead()) { 
                throw new CmdLineException(parser,"Can't read Input File " + inputFilename );
            }
            File outputFile = new File(outputFilename);
            if (outputFile.exists()) { 
                throw new CmdLineException(parser,"Output File Exists " + outputFilename );
            }
            setupOK = true;
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            //System.err.println("java FP [options...] arguments...");
            parser.printUsage(System.err);
            //System.err.println();
        }
        //Prov.init("testProv.log");
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
        /*
        // create the result listener, which will print the result and shutdown the system
        //final ActorRef display = system.actorOf(new Props(TextDisplay.class), "display");
        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBWriter(host,db,collectionOut,"",out,enc);
            }
        }), "MongoDBWriter");


        final ActorRef flwtValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new FloweringTimeValidator("fp.services.FNAFloweringTimeService",true,true,writer);
            }
        }), "flwtValidator");


        final ActorRef scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ScientificNameValidator("fp.services.IPNIService",true,true,flwtValidator);
            }
        }), "scinValidator");


        final ActorRef geoValidator = system.actorOf(new Props(new UntypedActorFactory() {
          public UntypedActor create() {
           return new GEORefValidator("fp.services.GeoLocate2",true,certainty,flwtValidator);
          }
        }), "geoValidator");

        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBReader(host,db,collectionIn,query,geoValidator);
            }
        }), "reader");

         final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVReader(inputFilename, scinValidator);
            }
        }), "reader");


        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVWriter(outputFilename);
            }
        }), "writer");


        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBWriter(host,db,collectionOut,"", enc);
            }
        }), "MongoDBWriter");

        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVWriter(outputFilename);
            }
        }), "writer");    */

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
                return new SciNameWorkflow("-t",false,dateValidator);
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
