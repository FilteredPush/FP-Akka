/**
 * Copyright (C) 2009-2012 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.fp;


import java.io.File;

import akka.actor.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class FP {

    public static void main(String[] args) {
        FP fp = new FP();
        fp.setup(args);
        fp.calculate();
    }

    @Option(name="-q",usage="query for MongoDB")
    private String query = "{year:\"1957\"}";

    @Option(name="-e",usage="encoding for output file")
    private String enc = "UTF-8";

    @Option(name="-o",usage="output records to file")
    //private String out = "/Users/cobalt/X31out.txt";
    //private String out = "/home/tianhong/X31out.txt";
    private String outputFilename = "testoutput.txt";

    @Option(name="-i",usage="Input records from CSV file")
    private String inputFilename = "test.csv";
    
    public void setup(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);
        try {
            parser.parseArgument(args);
            //if( arguments.isEmpty() )
            //    throw new CmdLineException(parser,"No argument is given");
            File inputFile = new File(inputFilename);
            if (!inputFile.canRead()) { 
                throw new CmdLineException(parser,"Can't read Input File " + inputFilename );
            }
            File outputFile = new File(outputFilename);
            if (outputFile.exists()) { 
                throw new CmdLineException(parser,"Output File Exists " + outputFilename );
            }
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java FP [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }
        Prov.init("testProv.log");
    }

    public void calculate() {
        this.calculate("fp3.acis.ufl.edu", "db", "Occurrence", "AkkaTest", query, 200.0);
    }

    public void calculate(
            final String host,
            final String db,
            final String collectionIn,
            final String collectionOut,
            final String query,
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
                  */

        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVWriter(outputFilename);
            }
        }), "MongoDBWriter");

        final ActorRef scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ScientificNameValidator("fp.services.IPNIService",true,true,writer);
            }
        }), "scinValidator");


        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVReader(inputFilename, scinValidator);
            }
        }), "reader");
        /*

        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBReader(host,db,collectionIn,query,scinValidator);
            }
        }), "reader"); */

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

