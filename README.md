# FP-Akka

FP-Akka is a biodiversity data cleaning tool produced by 
the FilteredPush and Kurator projects with support from the US
National Science Foundation.  It is able to run one of a small
set of predefined data quality control workflows on flat 
DarwinCore data held in MongoDB, DarwinCore archive files with
a dwc:Occurrence core, or csv files containing DarwinCore data.

[![DOI](https://zenodo.org/badge/76992913.svg)](https://zenodo.org/badge/latestdoi/76992913)

# Documentation

See: http://wiki.datakurator.org/wiki/FP-Akka_User_Documentation

# Building
To build FP-Akka from source, use maven.

    mvn install 

The buisness logic internals of the workflow actors can be 
found in the FP-KurationServices package.  You will need to
build FP-KurationServices to build FP-Akka.

https://github.com/FilteredPush/FP-KurationServices

# Running 
To run FP-Akka, use java.

    java -jar FP-Akka-{version}-workflowstarter.jar -w dwca -i dwca-file.zip -o output.json 

JSON output can be converted to human readable spreadsheets with 
the FP-postprocessor.jar tool.

Available Workflows are: DwCa, CSV, MONGO (specify workflow to run with -w).

The DwCa (-w DwCa) workflow checks an occurrence file and reports 
(in JSON) on quality aspects of the scientific name, event date, 
georeference, and basis of record elements of the data.

Options for the DwCa workflow (-w DwCa) are:

    -a VAL : Authority to check scientific names against (IPNI, IF, WoRMS, COL, GBIF, GlobalNames), default GBIF.
    -i VAL : Input occurrence.txt (tab delimited occurrence core from a DwC archive) file.
    -l N   : Limit on the number of records to read before stopping.
    -o VAL : output JSON file
    -t     : Run scientific name validator in taxonomic mode (look up name in current use).

Options for the CSV workflow (-w CSV) are:

    -a VAL : Authority to check scientific names against (IPNI, IF, WoRMS, COL, GBIF, GlobalNames), default GBIF.
    -i VAL : Input CSV file
    -l N   : Limit on the number of records to read before stopping.
    -o VAL : output file (.json unless -s is specified, in which case .csv)
    -s     : Only check scientific names with SciNameValidator (outputs will be .csv, not .json)
    -t     : SciNameValidator taxonomicMode Mode (look up name in current use).

The CSV workflow will perform one of two different operations.  
It, like the DwCA workflow, can operate on flat DarwinCore and check 
quality aspects of the scientific name, event date, and georeference, 
reporting the results in JSON. 
Alternatively (with -s), the CSV workflow can take a flat csv file of 
dwc:scientificName and dwc:scientificNameAuthorship and report on 
matches on those names found in specified authorities in a CSV output.  
This operation is intended for quality control of taxonomic authority
files against external authorities.

Options for the MongoDB workflow (-w Mongo) are: 

    -a VAL  : Authority to check scientific names against (IPNI, IF, WoRMS, COL, GBIF, GlobalNames), default GBIF.
    -ci VAL : Input Collection in mongo to query for records to process.
    -co VAL : Output Collection in mongo into which to write results.
    -d VAL  : db
    -h VAL  : MongoDB Host
    -l N    : Limit on the number of records to read before stopping.
    -q VAL  : Query on Mongo collection to select records to process, e.g. {institutionCode:\"NMSU\"} 
    -t      : Run scientific name validator in taxonomic mode (look up name in current use).

The Mongo workflow performs the same checks as the DwCa workflow, 
except it queries a collection in a MongoDB datastore for the 
input DarwinCore (in JSON), and writes the output into a mongo 
collection.  It is intended for use as an analytical capability in
a FilteredPush node.

There is also an experimental jettyStarter workflow that can be run
(e.g. from eclipse) to run as a service able to load data from the 
iDigBio API.  See the org.filteredpush.akka.workflows.jettyStarter 
class.
