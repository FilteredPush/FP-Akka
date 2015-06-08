package org.filteredpush.akka.actors;

import java.util.Random;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.filteredpush.akka.actors.io.CSVReader;
import org.filteredpush.akka.data.ReadMore;
import org.filteredpush.akka.data.SetUpstreamListener;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;
import org.filteredpush.kuration.util.SpecimenRecord;

/**
 * This actor can serve to throttle flow through a series of actors, where an
 * actor that has a high throughput rate can be followed by one or more actors
 * with low throughput rates, causing mailboxes to overflow or causing memory
 * consumption issues.  The PullRequestor can be placed downstream of the slow
 * throughput actors, and can make requests to the high throughput rate actor to
 * send data down the chain at a rate controlled by the throughput of the slow 
 * rate actors.  The PullRequestor will throttle the throughput to the rate of 
 * the slowest actor between the upstream listener and the PullRequestor.
 * 
 * To use, connect a pull requestor to the last of a chain of slow throughput
 * actors in a workflow, then send it a SetUpstreamListener message pointing
 * at the upstream listener. 
 * 
 * pullRequestor.tell(new SetUpstreamListener(), upstreamListeningActor);
 * 
 * on each message the PullRequestor receives which is a token, it will send
 * a ReadMore message to the upstream listener. 
 * 
 * @author mole
 *
 */
public class PullRequestor extends UntypedActor {
	
	private static final Log logger = LogFactory.getLog(PullRequestor.class);

	private ActorRef upstreamListener;
    private final ActorRef listener;
    private final ActorRef workerRouter;

    private int reportSize = 1000;
    private int msgCount = 0;
    
    /**
     * Set up an akka workflow actor that makes requests upstream to load more data.
     * defaults to 6 parallel instances
     * 
     * @param listener downstream actor that will consume output from this actor 
     */
    public PullRequestor(final ActorRef listener ) {
    	this(6, listener);
    }

    /**
     * Set up an akka workflow actor that wraps general scientific name validation classes.,
     * 
     * @param instances number of parallel instances 
     * @param listener downstream actor that will consume output from this actor 
     */
    public PullRequestor(final int instances, final ActorRef listener ) {
        this.upstreamListener = null;
        this.listener = listener;
        workerRouter = this.getContext().actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new PullRequestorInvocation(listener);
            }
        }).withRouter(new SmallestMailboxRouter(instances)), "workerRouter");
        getContext().watch(workerRouter);
    }

    public void onReceive(Object message) {
    	if (message instanceof SetUpstreamListener) { 
    		this.setUpstreamListener(getSender());
    	} else if (message instanceof Token) {
            if (!getSender().equals(getSelf())) {
                workerRouter.tell(message, getSelf());
            } else {
                listener.tell(message, getSelf());
            }
        } else if (message instanceof TokenWithProv) {
            listener.tell(message, getSelf());
        } else if (message instanceof SpecimenRecord) {
            listener.tell(message, getSelf());
        } else if (message instanceof Broadcast) {
            workerRouter.tell(new Broadcast(((Broadcast) message).message()), getSender());
        } else if (message instanceof Terminated) {
            //System.out.println("SciName termianted");
            if (((Terminated) message).getActor().equals(workerRouter))
                this.getContext().stop(getSelf());
        } else {
            unhandled(message);
        }

    }

    @Override
    public void postStop() {
        System.out.println("Stopped PullRequestor");
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }

    public class PullRequestorInvocation extends UntypedActor {

        private String serviceClassPath = null;

        private final Random rand;
        private int invoc;
        private final ActorRef listener;

        public PullRequestorInvocation(ActorRef listener) {
            this.listener = listener;
            this.rand = new Random();
        }

        public void onReceive(Object message) {
            //System.out.println("ScinRefWorker message: "+ message+toString());
        	
        	logger.debug(message.toString());
        	
            long start = System.currentTimeMillis();
            invoc = rand.nextInt();

            if (message instanceof Token) {
            	// Tell the upstream listener to load more data.
                if (upstreamListener!=null) { 
                	upstreamListener.tell(new ReadMore(), getSelf());
                }
                // Pass the message on to the downstream listener.
                listener.tell(message,getContext().parent());
            }
            
            msgCount++;
            if(msgCount % reportSize == 0) { 
                 System.out.println("Messages to PullRequestor: " + msgCount);
            }
        }

        public String getName() {
            return "PullRequestor";
        }
    }

	/**
	 * @return the upstreamListener
	 */
	public ActorRef getUpstreamListener() {
		return upstreamListener;
	}

	/**
	 * @param upstreamListener the upstreamListener to set
	 */
	public void setUpstreamListener(ActorRef upstreamListener) {
		this.upstreamListener = upstreamListener;
		System.out.println(this.getClass().getSimpleName() + " will make pull requests to " + upstreamListener.path());
	}
}
