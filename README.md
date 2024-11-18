
B2Bi Decoder Export
---------------------

![](https://github.com/EricJCross/B2BiDecoder/blob/b5bda5e21347ca92666c86d1602b014da63f7124/Documentation/Initial%20Screen.jpg)


Objectives
---------------------

The tool, B2BiDecoder.jar, is designed to take an IBM Sterling Integrator export file ( .xml or .jar ), and extract resources contained within it.

The following resources can be found within an export file:

- Business Processes
- Maps
- XSLTs
- XML Schemas
- User Identity Keys - noting that this exports the private key, but cannot not provide the passwords
- Known Host Keys
- Authorized User Keys



When SI creates export data, the included resources are 64-bit encoded.  This tool decodes it.  

Depending on the procedure used, Exports may contain the Default version only, or all versions of the resource.  This tool will export all versions if selected, otherwise only the default version.




Getting Started
---------------------

1. The B2BiDecoder is an executable jar file.  With java installed, a user can double click the JAR file to begin.
2. Click 'Load Export File' to select the Sterling Integrator export file.  
   1. Both .xml and .jar exports are supported.
   2. When a file is selected, the tool will pre-read and display the number of resources found.
3. By default, this tool will export data to the same location the export file is in.  If you wish to change this, select 'Select Destination'.
4. By default, this tool will only export the default versions.  If the export contains all versions, and you wish you extract them, select the 'Extract All Versions' checkbox.  Older versions ( not the default ) will be saved into a subdirectory named "_OLD".  Files are named with the version #.  ex:  HelloWorld_v1.bpml , HelloWorld_v2.bpml.  This will work for the following resources:
   1. Business Processes - files are named as .bpml
   2. Maps - both the compiled and uncompiled maps are saved
   3. XSLTs - files are named according to the original filename loaded in Sterling Integrator
   4. XML Schemas - files are named according to the original filename loaded in Sterling Integrator
5. Click 'Decode Extracts' to extract the resources.
6. If you wish, click 'Export Log' to save a copy of the log.  This is the text displayed in the tool





Change History
---------------------

2019 - Nick Turdo

- Initial version 

2024 - (https://github.com/EricJCross)

- Added introductory directions
- Renamed the title and buttons 
- Renamed the output directories
- Files output in the same location as the export file

- Added support for jar files
- When data is loaded, the number of resources will be displayed
- Added error checking.  The tool will not fail if it encounters a resource with invalid or missing data.
  - The error is recorded to the screen
  - Processing continues through all found resources
  - Any errors are displayed at the end of the export.
- Added support for All Versions of resources.  


###### Screenshots


The export file is loaded

![](https://github.com/EricJCross/B2BiDecoder/blob/b5bda5e21347ca92666c86d1602b014da63f7124/Documentation/Export%20File%20Loaded.jpg)


Extraction - while dealing with errors

![](https://github.com/EricJCross/B2BiDecoder/blob/b5bda5e21347ca92666c86d1602b014da63f7124/Documentation/Extraction%20-%20with%20error.jpg)


Extraction of all versions - while dealing with errors

![](https://github.com/EricJCross/B2BiDecoder/blob/b5bda5e21347ca92666c86d1602b014da63f7124/Documentation/Extraction%20all%20-%20with%20error.jpg)



Built With
---------------------
* Java 8
* Intellij


Authors
---------------------
* **Nick Turdo** - initial build
* **Eric Cross** - (https://github.com/EricJCross) - updates



