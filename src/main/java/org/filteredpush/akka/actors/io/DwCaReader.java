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
    
    private int cValidRecords = 0;  // number of records read.
    private int recordLimit = 0; // maximum records to read, zero or less for no limit.
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
	 * @param maxRecordsToRead limit on the number of records to read before stopping, zero or less for no limit.
	 */
    public DwCaReader(String archiveFilePath, ActorRef downstreamListener, int maxRecordsToRead) {
        listener = downstreamListener;
        recordLimit = maxRecordsToRead;
        if (archiveFilePath != null) { 
        	filePath = archiveFilePath;
        	File file =  new File(filePath);
        	if (!file.exists()) { 
        		// Error
        		logger.error(filePath + " not found.");
        	}
        	if (!file.canRead()) { 
        		// error
        		logger.error("Unable to read " + filePath);
        	}
        	if (file.isDirectory()) { 
        		// check if it is an unzipped dwc archive.
        		dwcArchive = openArchive(file);
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
        		dwcArchive = openArchive(outputDirectory);
        	}
        	if (dwcArchive!=null) { 
        		if (checkArchive()) {
        			// good to go
        		}
        	} else { 
				System.out.println("Problem opening archive.");
        	}
        }
        invoc = 0;
    }

    /**
     * Attempt to open a DarwinCore archive directory and return it as an Archive object.  
     * If an UnsupportedArchiveException is thrown, trys again harder by looking for an archive
     * directory inside the provided directory.
     * 
     * @param outputDirectory directory that should represent an unzipped DarwinCore archive.
     * @return an Archive object repsesenting the content of the directory or null if unable
     * to open an archive object.
     */
    protected Archive openArchive(File outputDirectory) { 
    	Archive result = null;
    	try {
    		result = ArchiveFactory.openArchive(outputDirectory);
    	} catch (UnsupportedArchiveException e) {
    		logger.error(e.getMessage());
    		File[] containedFiles = outputDirectory.listFiles();
    		boolean foundContained = false;
    		for (int i = 0; i<containedFiles.length; i++) { 
    			if (containedFiles[i].isDirectory()) {
    				try {
    					// Try harder, some pathological archives contain a extra level of subdirectory
    					result = ArchiveFactory.openArchive(containedFiles[i]);
    					foundContained = true;
    				} catch (Exception e1) { 
    					logger.error(e.getMessage());
    					System.out.println("Unable to open archive directory " + e.getMessage());
    					System.out.println("Unable to open directory contained within archive directory " + e1.getMessage());
    				}
    			}
    		}
    		if (!foundContained) { 
    			System.out.println("Unable to open archive directory " + e.getMessage());
    		}					
    	} catch (IOException e) {
    		logger.error(e.getMessage());
    		System.out.println("Unable to open archive directory " + e.getMessage());
    	}
    	return result;
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
			if (recordLimit>0 && initialLoad>recordLimit) { initialLoad=recordLimit; }
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
        
        // Check to see if we have reached the limit of number of records to read.
        boolean readToLimit = false;
        if (recordLimit>0 && cValidRecords>= recordLimit) {
        	readToLimit = true;
        }
		
		if ((iterator!=null && !iterator.hasNext()) || readToLimit) {
			// Reached last record in iterator, or have reached limit of records to read
			// Done.
			listener.tell(new Broadcast(PoisonPill.getInstance()),getSelf());
			getContext().stop(getSelf());
			//Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getName(),invoc,start,System.currentTimeMillis());
		}
		invoc++;		
		
	}

}
