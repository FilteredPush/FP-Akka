package fp.services.sciName;

import fp.util.SpecimenRecord;

/**
 * Created by tianhong on 2/9/15.
 */
public abstract class Component{

    SpecimenRecord dataRecord;

    public void setInput(SpecimenRecord input){
         dataRecord = input;
    }

    abstract public void run();

    public SpecimenRecord getOutput(){
        return dataRecord;
    }


}
