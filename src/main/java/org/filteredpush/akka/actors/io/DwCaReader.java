/** 
 * DwCaReader.java 
 * 
 * Copyright 2015 President and Fellows of Harvard College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.filteredpush.akka.actors.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.filteredpush.akka.data.Curate;
import org.filteredpush.akka.data.ReadMore;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.gbif.dwc.record.StarRecord;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.UnsupportedArchiveException;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;

/**
 * Actor to read occurrence data from DarwinCore Archive files.
 * 
 * @author mole
 *
 */
public class DwCaReader extends UntypedActor {
	
	private static final Log logger = LogFactory.getLog(DwCaReader.class);
	
	private final ActorRef listener;

    private String filePath = "dwca.zip";
    
    private int cValidRecords = 0;
    String[] labelList;

    public Archive dwcArchive = null;
    public Reader inputReader = null;
    public String recordClass = null;
    public String[] headers = new String[]{};
    
    /**
     * Report reading records in this increment. 
     */
    private int reportSize = 1000;
    
    Iterator<StarRecord> iterator;
    Object debug;

    int invoc;
    long start;	
	
	/**
	 * Constructor, for reading data from a DarwinCore Archive file
	 * or directory.  Specifies location of archive to read and next actor
	 * downstream to which records from the archive should be passed.
	 * If the archiveFilePath is a zip file, it will be unzipped.
	 * 
	 * @param archiveFilePath
	 * @param downstreamListener
	 */
    public DwCaReader(String archiveFilePath, ActorRef downstreamListener) {
        listener = downstreamListener;
        if (archiveFilePath != null) { 
        	filePath = archiveFilePath;
        	File file =  new File(filePath);
        	if (!file.exists()) { 
        		// Error
        	}
        	if (!file.canRead()) { 
        		// error
        	}
        	if (file.isDirectory()) { 
        		// check if it is an unzipped dwc archive.
				try {
					dwcArchive = ArchiveFactory.openArchive(file);
				} catch (UnsupportedArchiveException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
        	}
        	if (file.isFile()) { 
        		// unzip it
        		File outputDirectory = new File(file.getName().replace(".", "_") + "_content");
        		if (!outputDirectory.exists()) {
        			outputDirectory.mkdir();
        			try {
        				byte[] buffer = new byte[1024];
						ZipInputStream inzip = new ZipInputStream(new FileInputStream(file));
						ZipEntry entry =  inzip.getNextEntry();
						while (entry!=null) { 
					    	   String fileName = entry.getName();
					           File expandedFile = new File(outputDirectory.getPath() + File.separator + fileName);
					            new File(expandedFile.getParent()).mkdirs();
					            FileOutputStream expandedfileOutputStream = new FileOutputStream(expandedFile);             
					            int len;
					            while ((len = inzip.read(buffer)) > 0) {
					       		    expandedfileOutputStream.write(buffer, 0, len);
					            }
					 
					            expandedfileOutputStream.close();   
					            entry = inzip.getNextEntry();							
						}
						inzip.closeEntry();
						inzip.close();
						System.out.println("Unzipped archive into " + outputDirectory.getPath());
					} catch (FileNotFoundException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} catch (IOException e) {
						logger.error(e.getMessage());
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		// look into the unzipped directory
    		    try {
					dwcArchive = ArchiveFactory.openArchive(outputDirectory);
				} catch (UnsupportedArchiveException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	if (dwcArchive!=null) { 
        		if (checkArchive()) {
        			// good to go
        		}
        	}
        }
        invoc = 0;
    }

    protected boolean checkArchive() {
    	boolean result = false;
    	if (dwcArchive==null) { 
    		return result;
    	}
	    if (dwcArchive.getCore() == null) {
		      System.out.println("Cannot locate the core datafile in " + dwcArchive.getLocation().getPath());
		      return result;
		}
		System.out.println("Core file found: " + dwcArchive.getCore().getLocations());
		System.out.println("Core row type: " + dwcArchive.getCore().getRowType());
		if (dwcArchive.getCore().getRowType().equals(DwcTerm.Occurrence) ) {
			
			// check expectations 
		    List<DwcTerm> expectedTerms = new ArrayList<DwcTerm>();
		    expectedTerms.add(DwcTerm.scientificName);
		    expectedTerms.add(DwcTerm.scientificNameAuthorship);
		    expectedTerms.add(DwcTerm.eventDate);
		    expectedTerms.add(DwcTerm.recordedBy);
		    expectedTerms.add(DwcTerm.decimalLatitude);
		    expectedTerms.add(DwcTerm.decimalLongitude);
		    expectedTerms.add(DwcTerm.locality);
		    expectedTerms.add(DwcTerm.basisOfRecord);
		    
		    for (DwcTerm term : expectedTerms) {
		      if (!dwcArchive.getCore().hasTerm(term)) {
		        System.out.println("Cannot find " + term + " in core of input dataset.");
		      }
		    } 		
		    
		    result = true;
		} else { 
			// currently can only process occurrence core
		}

        return result;
    }
    
    /**
	 * @return the reportSize (send count of number of records read
	 * to the console at this increment of number of records read)
	 */
	public int getReportSize() {
		return reportSize;
	}

	/**
	 * @param reportSize the reportSize to set
	 */
	public void setReportSize(int chunkSize) {
		this.reportSize = chunkSize;
	}    
    
	@Override
	public void onReceive(Object message) throws Exception {
		
		if (message instanceof Curate) { 
			// startup 
			start = System.currentTimeMillis();
			int initialLoad = 30;
		    iterator = dwcArchive.iterator();
			while (iterator.hasNext() && cValidRecords < initialLoad) {
				// read initial set of rows, pass downstream
				StarRecord dwcrecord = iterator.next();
				SpecimenRecord record = new SpecimenRecord(dwcrecord);
                Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(record,this.getClass().getSimpleName(),invoc);
				cValidRecords++;
                listener.tell(t,getSelf());
			}
            System.out.println("Read initial " + cValidRecords + " records.  Will report every " + this.reportSize + " records.") ;
				
			
		} else if (message instanceof ReadMore ) { 
			// send another record down the pipeline
			
			if (iterator.hasNext()) {
				StarRecord dwcrecord = iterator.next();
				SpecimenRecord record = new SpecimenRecord(dwcrecord);
                Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(record,this.getClass().getSimpleName(),invoc);
				cValidRecords++;
                listener.tell(t,getSelf());
			}
			
		}

        if(cValidRecords % reportSize == 0) { 
             System.out.println("Read " + reportSize + " records, total " + cValidRecords);
        }		
		
		if (iterator!=null && !iterator.hasNext()) { 
			listener.tell(new Broadcast(PoisonPill.getInstance()),getSelf());
			getContext().stop(getSelf());
			//Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getName(),invoc,start,System.currentTimeMillis());
		}
		invoc++;		
		
	}

}
