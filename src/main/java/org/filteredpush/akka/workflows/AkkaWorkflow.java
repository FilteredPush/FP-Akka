package org.filteredpush.akka.workflows;

/**
 * Created by thsong on 5/15/15.
 */
public interface AkkaWorkflow {

    public boolean setup(String[] args);

    public void calculate();
}
