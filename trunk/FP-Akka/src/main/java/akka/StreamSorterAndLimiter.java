package akka;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 05.09.2013
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public class StreamSorterAndLimiter extends WfActor {

    private int numStreams;
    private Map<String,Long> map = new HashMap<String, Long>();
    private Queue<Long> q = new PriorityQueue<Long>();


    public StreamSorterAndLimiter(int numStreams) {
        super();
        this.numStreams = numStreams;
    }

    @Override
    public void fire(Object message) {
        if (message instanceof Long) {
            Long x = (Long) message;
            String p = getPort();
            System.out.println(getName() + " Sorting " +x + " from "+ p);
            map.put(p,x);
            q.offer(x);
            if (map.keySet().size() >= numStreams)
                checkAndSend();
        }
    }

    private void checkAndSend() {
        Long min = Long.MAX_VALUE;
        for (Long i : map.values()) {
            if (i < min) min = i;
        }
        while (q.size() > 0 && q.peek() <= min) {
            System.out.println("Sending "+q.peek());
            broadcast(q.poll());
        }
    }
}
