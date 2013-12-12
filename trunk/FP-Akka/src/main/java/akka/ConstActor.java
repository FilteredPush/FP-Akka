package akka;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.fp.Prov;
import akka.routing.Broadcast;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 04.09.2013
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */
public class ConstActor extends UntypedActor {
    private final ActorRef listener;
    int invoc;
    long constant;

    public ConstActor(int i, ActorRef listener) {
        this.listener = listener;
        this.constant = i;
        invoc = 0;
    }

    public void onReceive(Object message) {
        long start = System.currentTimeMillis();
        if (message instanceof Trigger) {
            listener.tell(new Token<Long>(constant),getSelf());
            //listener.tell(new Broadcast(PoisonPill.getInstance()),getSelf());
            getContext().stop(getSelf());
        } else if (message instanceof Broadcast) {
            getSelf().tell(((Broadcast) message).message(), getSender());
        } else {
            unhandled(message);
        }
        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getSimpleName(),invoc,start,System.currentTimeMillis());
        invoc++;
    }

    @Override
    public void postStop() {
        //getContext().system().shutdown();
        super.postStop();
    }

}
