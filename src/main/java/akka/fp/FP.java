/**
 * Copyright (C) 2009-2012 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.fp;


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

    @Option(name="-h",usage="MongoDB host")
    private String host = "fp3.acis.ufl.edu";

    @Option(name="-db",usage="MongoDB database")
    private String mdb = "db";

    @Option(name="-icol",usage="input MongoDB collection")
    private String inCol = "Occurrence";

    @Option(name="-ocol",usage="output MongoDB collection")
    private String outCol = "AkkaTest";

    @Option(name="-q",usage="query for MongoDB")
    private String query = "{year:\"1957\"}";

    @Option(name="-e",usage="encoding for output file")
    private String enc = "UTF-8";

    @Option(name="-o",usage="output records to file")
    private String out = "/Users/cobalt/X31out.txt";

    public void setup(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);
        try {
            parser.parseArgument(args);
            //if( arguments.isEmpty() )
            //    throw new CmdLineException(parser,"No argument is given");
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java FP [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }
        System.out.println("Query: "+ query);
        Prov.init("testProv.log");
    }

    public void calculate() {
        this.calculate(host, mdb, inCol, outCol, query, 200.0);
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
           return new GEORefValidator("fp.services.GeoLocate2",true,certainty,scinValidator);
          }
        }), "geoValidator");

        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBReader(host,db,collectionIn,query,geoValidator);
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

