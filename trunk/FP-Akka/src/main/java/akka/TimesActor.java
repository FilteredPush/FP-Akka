package akka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 04.09.2013
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */
public class TimesActor extends WfActor {
    int constant;

    public TimesActor(int i, List<String> listeners) {
        super(listeners);
        this.constant = i;
    }

    public TimesActor(int i) {
        super();
        this.constant = i;
    }

    @Override
    public void fire(Object message) {
        long x = (Long)message * this.constant;
        System.out.println(x + " = "+message+" * " + this.constant);
        if (x <= 1000) {
            broadcast(x);
        }
    }
}
