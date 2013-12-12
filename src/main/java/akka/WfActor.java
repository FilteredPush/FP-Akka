package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.fp.Prov;
import akka.routing.Broadcast;

import java.util.LinkedList;
import java.util.List;

public abstract class WfActor extends UntypedActor {

    final List<String> listeners;
    int invoc = 0;
    private String port;

    public WfActor() {
        this.listeners = new LinkedList<String>();
    }

    public WfActor(List<String> listeners) {
        this.listeners = listeners;
    }

    String getName() {
        return getSelf().toString();
    }

    protected String getPort() {
        return this.port;
    }

    public void addListener(String listener) {
        listeners.add(listener);
    }

    public void onReceive(Object message) {
        long start = System.currentTimeMillis();
        if (message instanceof TokenWithProv) {
            Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                    ((TokenWithProv) message).getActorCreated(),
                    ((TokenWithProv) message).getInvocCreated(),
                    getName(),
                    invoc,
                    ((TokenWithProv) message).getTimeCreated(),
                    System.currentTimeMillis());
        }
        if (message instanceof Token) {
            this.port = getSender().toString();
            fire(((Token) message).getData());
        } else if (message instanceof Broadcast) {
            this.getSelf().tell(((Broadcast) message).message(), this.getSender());
        } else {
            this.unhandled(message);
        }
        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", getName(), invoc, start, System.currentTimeMillis());
        invoc++;
    }

    public abstract void fire(Object message);

    protected void broadcast(Object message) {
        for (String listener : listeners) {
            ActorRef a = getContext().system().actorFor("/user/"+listener);
            a.tell(new TokenWithProv(message, getName(), invoc), this.getSelf());
        }
    }

    @Override
    public void postStop() {
        this.getContext().system().shutdown();
        super.postStop();
    }
}