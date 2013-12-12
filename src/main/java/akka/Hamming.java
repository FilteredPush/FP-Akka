package akka;

import akka.actor.*;
import akka.fp.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 04.09.2013
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class Hamming {

    public static void main(String[] args) {
        Hamming wf = new Hamming();
        wf.setup(args);
        wf.run();
    }

    @Option(name="-e",usage="encoding for output file")
    private String enc = "UTF-8";

    public void setup(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);
        try {
            parser.parseArgument(args);
            //if( arguments.isEmpty() )
            //    throw new CmdLineException(parser,"No argument is given");
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java Hamming [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }
        Prov.init("testProv.log");
    }

    public void run() {
        long starttime = System.currentTimeMillis();

        // Create an Akka system
        ActorSystem system = ActorSystem.create("HammingWf");

        final ActorRef txt1 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new TextDisplay();
            }
        }), "txt1");

        final ActorRef ssal2 = system.actorOf(new Props(new UntypedActorFactory() {
                    public UntypedActor create() {
                StreamSorterAndLimiter s = new StreamSorterAndLimiter(2);
                s.addListener("txt1");
                s.addListener("mul5");
                return s;
            }
        }), "ssal2");


        final ActorRef ssal1 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                StreamSorterAndLimiter s = new StreamSorterAndLimiter(2);
                s.addListener("ssal2");
                s.addListener("mul3");
                return s;
            }
        }), "ssal1");

        final ActorRef mul2 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                TimesActor ta2 = new TimesActor(2);
                ta2.addListener("mul2");
                ta2.addListener("ssal1");
                return ta2;
            }
        }), "mul2");

        final ActorRef mul3 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                TimesActor ta3 = new TimesActor(3);
                ta3.addListener("ssal1");
                return ta3;
            }
        }), "mul3");

        final ActorRef mul5 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                TimesActor ta5 = new TimesActor(5);
                ta5.addListener("ssal2");
                return ta5;
            }
        }), "mul5");

        final ActorRef const2 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ConstActor(1,mul2);
            }
        }), "const2");

        final ActorRef const3 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ConstActor(1,mul3);
            }
        }), "const3");

        final ActorRef const5 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ConstActor(1,mul5);
            }
        }), "const5");

        // start the calculation
        const2.tell(new Trigger(),system.lookupRoot());
        const3.tell(new Trigger(),system.lookupRoot());
        const5.tell(new Trigger(),system.lookupRoot());
        system.awaitTermination();
        long stoptime = System.currentTimeMillis();
        //System.out.printf("\nTime: %f s\n",(stoptime-starttime)/1000.0);
        System.err.printf("Runtime: %d ms",stoptime-starttime);
    }

    static class Curate {
    }
}
