package org.COE;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.Dimension;

//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.jar.*;
import java.util.Enumeration;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;


import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.ScrollPaneConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;



    public class DecodeExtract extends JPanel implements ActionListener {
        private static final long serialVersionUID = 1L;
        private static final String newline = "\n";
        private static final String newtab = "\t";
        JButton openButton;
        JButton saveButton;
        JButton goButton;
        private JCheckBox extractAllVersionsCheckbox;
        JButton logButton;
        JTextArea log = new JTextArea(20, 60);
        JFileChooser fc_FileOnly;
        JFileChooser fc_Directory;
        File B2BiExportFile = null;
        String DIRECTORY4RESOURCES = "";
        Document DocB2BiExport;

        int Count_BPs = 0;
        int Count_Maps = 0;
        int Count_XSLTs = 0;
        int Count_XMLSchema = 0;
        int Count_Key_User_ID = 0;
        int Count_Key_Known_Host = 0;
        int Count_Key_Authorized = 0;

        private boolean Extract_All_Versions = false;

        private List<String> Errors = new ArrayList<>();

        /**
         * Description: Constructs a new DecodeExtract panel and initializes its components.
         * Parameters: none
         * Returns: none
         */
        public DecodeExtract() {
            super(new BorderLayout());
            this.log.setText( "Decode and Extract, by coenterprise\n\n" +
                              "Instructions:\n" +
                              "1. Click 'Load Export File' to select the IBM B2B Integrator export file.\n" +
                              "     .xml and .jar files are supported.\n" +
                              "     This will set the parent output directory to the same location.\n" +
                              "2. Click 'Select Destination' to select a new output directory.\n" +
                              "3. Click 'Decode Extracts' to start the decoding process.\n" +
                              "4. Optionally, check 'Extract All Versions' to extract all versions of the files.\n" +
                              "     The export file may not contain multiple versions of a resource\n" +
                              "     Not all resources have multiple versions within SI\n" +
                              "5. Click 'Export Log' to save the log output to a file.\n\n");

            this.log.setMargin(new Insets(5, 5, 5, 5));
            this.log.setEditable(false);
            JScrollPane logScrollPane = new JScrollPane(this.log);

            // Ensure the scroll pane's viewport listens to size changes
            logScrollPane.setPreferredSize(new Dimension(500, 300)); // Set initial size
            logScrollPane.setMinimumSize(new Dimension(10, 10));
            logScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);


            this.fc_FileOnly = new JFileChooser();
            this.fc_FileOnly.setFileSelectionMode(0);
            this.fc_Directory = new JFileChooser();
            this.fc_Directory.setFileSelectionMode(1);
            this.openButton = new JButton("Load Export File");
            this.openButton.addActionListener(this);
            this.saveButton = new JButton("Select Destination");
            this.saveButton.addActionListener(this);
            this.goButton = new JButton("Decode Extracts");
            this.goButton.addActionListener(this);

            this.extractAllVersionsCheckbox = new JCheckBox("Extract All Versions");
            this.extractAllVersionsCheckbox.setToolTipText("Select to extract all versions.");
            this.extractAllVersionsCheckbox.setSelected(false);  // Unselected by default
            this.extractAllVersionsCheckbox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    DecodeExtract.this.Extract_All_Versions = e.getStateChange() == ItemEvent.SELECTED;
                    // Log the current state for debugging
                    if ( DecodeExtract.this.Extract_All_Versions ) {
                        DecodeExtract.this.Write_Log( "All resource versions will be extracted, and placed in an \\_OLD subdirectory" );
                        DecodeExtract.this.Write_Log( "Old versions will be named with _<version number>.  ex:  HelloWorld_v1.bpml" );
                    }
                    else {
                        DecodeExtract.this.Write_Log( "Only the default resource versions will be extracted." );
                    }
                }
            });

            this.logButton = new JButton("Export Log");
            this.logButton.addActionListener(this);
            this.openButton.setToolTipText("Select the IBM B2B Integrator export file you wish to decode.");
            this.saveButton.setToolTipText("Select Destination for the exported files.");
            this.goButton.setToolTipText("Start the decode, export file and destination must be select prior to starting.");
            this.logButton.setToolTipText("Save the log textArea to file.");
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(this.openButton);
            buttonPanel.add(this.saveButton);
            buttonPanel.add(this.goButton);
            buttonPanel.add(this.extractAllVersionsCheckbox);  // Add checkbox to the panel

            buttonPanel.add(this.logButton);
            this.add(buttonPanel, "First");
            this.add(logScrollPane, "Center");
        }

        /**
         * Appends a message to the log text area and ensures it is displayed immediately.
         *
         * @param message the message to append to the log
         */
        private void Write_Log( String message ) {
            SwingUtilities.invokeLater(() -> {
                this.log.append( message + "\n" );
                this.log.setCaretPosition(this.log.getDocument().getLength());
            });
        }


        /**
         * Description: Handles action events for the buttons in the DecodeExtract panel.
         * Parameters: e - The ActionEvent triggered by the user's action.
         * Returns: none
         */
        public void actionPerformed(ActionEvent e) {
            int returnVal;
            if (e.getSource() == this.openButton) {
                this.Write_Log( "Loading..." );
                returnVal = this.fc_FileOnly.showOpenDialog(this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    this.B2BiExportFile = this.fc_FileOnly.getSelectedFile();
                    this.DIRECTORY4RESOURCES = this.B2BiExportFile.getParent();
                    this.Write_Log("Selected File: " + this.B2BiExportFile.getName());

                    try {
                        if (this.B2BiExportFile.getName().toLowerCase().endsWith(".xml") ) {
                            this.Write_Log("Opening XML Export File: " + this.B2BiExportFile.getName());
                            this.DocB2BiExport = this.ReadFile( this.B2BiExportFile );
                            this.LogInitialFigures();
                        } else if (this.B2BiExportFile.getName().toLowerCase().endsWith(".jar")) {
                            this.Write_Log("Opening JAR Export File: " + this.B2BiExportFile.getName());
                            this.extractResourceXMLFromJar( this.B2BiExportFile );
                            this.LogInitialFigures();
                        } else {
                            this.Write_Log("Unsupported file format: " + this.B2BiExportFile.getName());
                        }

                    } catch ( Exception ex) {
                        this.Write_Log("Error opening file: " + ex.getMessage() );
                        ex.printStackTrace();
                    }

                } else {
                    this.Write_Log( "Open command cancelled by user." );
                }

                this.log.setCaretPosition(this.log.getDocument().getLength());
            } else if (e.getSource() == this.saveButton) {
                returnVal = this.fc_Directory.showOpenDialog(this);
                if (returnVal == 0) {
                    this.DIRECTORY4RESOURCES = this.fc_Directory.getSelectedFile().getPath();
                    this.Write_Log( "Saving Destination Location: " + this.DIRECTORY4RESOURCES + "." );
                } else {
                    this.Write_Log("Destination command cancelled by user.");
                }

                this.log.setCaretPosition(this.log.getDocument().getLength());
            } else if (e.getSource() == this.goButton) {
                if (this.B2BiExportFile != null && this.DIRECTORY4RESOURCES != "") {
                    //this.log.append("Starting Decode\n");
                    this.Write_Log( "\nStarting Decode" );
                    //this.DocB2BiExport = this.ReadFile(this.B2BiExportFile);
                    if ( this.Count_BPs > 0 ) {
                        this.LoopBusinessProcesses();
                    }
                    if ( this.Count_Maps > 0 ) {
                        this.LoopMaps();
                    }
                    if ( this.Count_XSLTs > 0 ) {
                        this.LoopXSLT();
                    }
                    if ( this.Count_XMLSchema > 0 ) {
                        this.LoopXMLSchema();
                    }
                    if ( this.Count_Key_User_ID > 0 ) {
                        this.LoopUserIdentKey();
                    }
                    if ( this.Count_Key_Known_Host > 0 ) {
                        this.LoopKnownHostKey();
                    }
                    if ( this.Count_Key_Authorized > 0 ) {
                        this.LoopAuthorizedUserKey();
                    }
                    this.Write_Log( "Decode Complete" );
                    this.Display_Errors();
                } else {
                    this.Write_Log("Select Export File and Directory First..");
                }

                //this.log.setCaretPosition(this.log.getDocument().getLength());
            } else if (e.getSource() == this.logButton) {
                // Set default filename for saving the log
                String Filename_Log = this.DIRECTORY4RESOURCES + "\\" + "log.txt";
                File defaultFile = new File(Filename_Log);
                this.fc_FileOnly.setSelectedFile(defaultFile);

                // Show the save dialog
                returnVal = this.fc_FileOnly.showSaveDialog(this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    this.SaveLog(this.fc_FileOnly.getSelectedFile());
                } else {
                    this.Write_Log("Save command cancelled by user.");
                }

            }

        }

        /**
         * Description: Reads and parses the specified B2Bi export file into a Document object.
         * Parameters: B2BiExportFile - The file to be read and parsed.
         * Returns: Document - The parsed XML document.
         */
        public Document ReadFile(File B2BiExportFile) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(B2BiExportFile);
                doc.getDocumentElement().normalize();
                return doc;
            } catch (Exception var5) {
                var5.printStackTrace();
                return null;
            }
        }


        private void extractResourceXMLFromJar( File jarFile ) throws IOException {
            JarFile jar = new JarFile( jarFile );
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                this.Write_Log( "Entry: " + entry.getName() );
                if (entry.getName().startsWith( "SIResources/import/resources.xml") ) {
                    InputStream inputStream = jar.getInputStream(entry);
                    this.DocB2BiExport = this.readXMLFromInputStream( inputStream );
                    break;
                }
            }

            jar.close();
        }

        private Document readXMLFromInputStream( InputStream inputStream) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                return builder.parse(inputStream);
            } catch (ParserConfigurationException | SAXException | IOException e ) {
                this.Write_Log("Error parsing XML: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }


        public void LogInitialFigures() {
            try {
                this.Count_BPs = this.DocB2BiExport.getElementsByTagName("BPDEF").getLength();
                this.Count_Maps = this.DocB2BiExport.getElementsByTagName("MAP").getLength();
                this.Count_XSLTs = this.DocB2BiExport.getElementsByTagName("XSLT").getLength();
                this.Count_XMLSchema = this.DocB2BiExport.getElementsByTagName("SCHEMA").getLength();
                this.Count_Key_User_ID = this.DocB2BiExport.getElementsByTagName("SSH_HOST_USER_IDENTITY_KEY").getLength();
                this.Count_Key_Known_Host = this.DocB2BiExport.getElementsByTagName("SSH_KNOWN_HOST_KEY").getLength();
                this.Count_Key_Authorized = this.DocB2BiExport.getElementsByTagName("SSH_AUTHORIZED_USER_KEY").getLength();

                this.Write_Log("\nExtractable Data Found in Export:");
                this.Write_Log("\tBP Count: " + this.Count_BPs );
                this.Write_Log("\tMap Count: " + this.Count_Maps );
                this.Write_Log("\tXSLT Count: " + this.Count_XSLTs );
                this.Write_Log("\tXML Schema Count: " + this.Count_XMLSchema );
                this.Write_Log("\tUser Identity Key Count: " + this.Count_Key_User_ID );
                this.Write_Log("\tKnown Host Key Count: " + this.Count_Key_Known_Host );
                this.Write_Log("\tAuthorized User Key Count: " + this.Count_Key_Authorized );

            } catch (Exception e) {
                e.printStackTrace();
                //this.Write_Log("\tError in LogInitialFigures():\n" + e );
                if ( this.DocB2BiExport == null ) {
                    this.Write_Log( "\tError in LogInitialFigures():\nNo data loaded" );
                } else {
                    this.Write_Log( "\tError in LogInitialFigures():\n" + e );
                }
            }
        }

        /**
         * Description: Creates a directory for the specified object type in the resources directory.
         * Parameters: ObjectType - The type of object for which the directory is to be created.
         * Returns: none
         */
        public void makeDirectory(String ObjectType) {
            try {
                File destination = new File(this.DIRECTORY4RESOURCES + "\\" + ObjectType);
                if (!destination.exists()) {
                    this.Write_Log("\tCreating directory: " + ObjectType );
                    destination.mkdirs();
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.Write_Log("\tFailed to create directory: " + ObjectType + "\n" + "\t" + "\t" + e );
            }
        }

        /**
         * Description: Writes the provided content to a file within the specified object type directory.
         * Parameters: ObjectType - The type of object to which the file belongs.
         *             ResourceName - The name of the resource to be saved.
         *             bpcontent - The content to be written to the file.
         * Returns: none
         */
        public void WriteToFile(String ObjectType, String ResourceName, byte[] bpcontent) {
            try {
                this.makeDirectory(ObjectType);
                File destination = new File(this.DIRECTORY4RESOURCES + "\\" + ObjectType + "\\" + ResourceName);
                FileOutputStream writer = new FileOutputStream(destination);
                writer.write(bpcontent);
                writer.close();
                this.Write_Log("\tSaving: " + ObjectType + "\\" + ResourceName );

            } catch (Exception e) {
                e.printStackTrace();
                this.Write_Log("\tFailed to Save: " + ObjectType + "\\" + ResourceName + "\n" + "\t" + "\t" + e);
            }

        }

        /**
         * Description: Saves the current log content to the specified log file.
         * Parameters: LogFile - The file where the log content will be saved.
         * Returns: none
         */
        public void SaveLog(File LogFile) {
            try {
                FileWriter writer = new FileWriter(LogFile);
                this.log.write(writer);
                writer.close();
                this.Write_Log("Saved Log: " + LogFile.getName() );
            } catch (Exception e) {
                e.printStackTrace();
                this.Write_Log("\tFailed to Save Log: " + LogFile.getName() );
            }

        }

        /**
         * Description: Extracts and saves business processes from the export document.
         * Parameters: export - The XML document containing the exported data.
         * Returns: none
         */
        public void LoopBusinessProcesses() {
            NodeList resourceList = this.DocB2BiExport.getElementsByTagName("BPDEF");
            this.Write_Log("\nExporting Business Processes..." );

            String resourceName = "";
            int Count_Exported = 0;
            for(int resourceIter = 0; resourceIter < resourceList.getLength(); ++resourceIter) {
                try {
                    Node nResourceRoot = resourceList.item(resourceIter);
                    if (nResourceRoot.getNodeType() == 1) {
                        Element eElement = (Element)nResourceRoot;
                        resourceName = eElement.getElementsByTagName("ConfProcessName").item(0).getTextContent();
                        int Is_Default = eElement.getElementsByTagName("SIResourceDefaultVersion").getLength();

                        NodeList Nodes = eElement.getElementsByTagName("LangResource");

                        int Count_Resource = 0;
                        for ( int n = 0; n < Nodes.getLength(); n++ ) {
                            Node Node_Map = Nodes.item( n );
                            if ( Node_Map.getNodeType() == 1 ) {
                                Element eDocument = (Element) Node_Map;
                                String encodedResource = eDocument.getTextContent();
                                byte[] decodedResource = Base64.getDecoder().decode( encodedResource.replace( "SIB64ENCODE", "" ) );
                                if ( Is_Default > 0 ) {
                                    // Default version
                                    this.WriteToFile( "Business_Processes", resourceName + ".bpml", decodedResource );
                                }
                                else {
                                    String Version = eElement.getElementsByTagName("OBJECT_VERSION").item(0).getTextContent();
                                    this.WriteToFile( "Business_Processes\\_OLD", resourceName + "_v" + Version + ".bpml", decodedResource );
                                }
                                Count_Exported++;
                                Count_Resource++;
                            }
                        }
                        if ( Count_Resource == 0 ) {
                            this.Write_Log( "Export file does not contain data for: " + resourceName );
                            this.Errors.add( "Business Process: " + resourceName );
                        }

                    }
                } catch ( Exception e ) {
                    this.Write_Log("Error on Business Process: " + resourceName + " - " + e.getMessage() );
                    this.Errors.add( "Business Process: " + resourceName );
                    e.printStackTrace();
                }
            }
            this.Write_Log( "\t" + Count_Exported + " of " + this.Count_BPs + " exported" );
        }

        /**
         * Description: Extracts and saves maps from the export document.
         * Parameters: export - The XML document containing the exported data.
         * Returns: none
         */
        public void LoopMaps() {
            NodeList resourceList = this.DocB2BiExport.getElementsByTagName("MAP");
            this.Write_Log("\nExporting Maps... " );

            String resourceName = "";
            int Count_Exported = 0;
            for(int resourceIter = 0; resourceIter < resourceList.getLength(); ++resourceIter) {
                try {
                    Node nResourceRoot = resourceList.item(resourceIter);
                    if (nResourceRoot.getNodeType() == 1) {
                        Element eElement = (Element)nResourceRoot;
                        resourceName = eElement.getElementsByTagName("MAP_NAME").item(0).getTextContent();
                        int Is_Default = eElement.getElementsByTagName("SIResourceDefaultVersion").getLength();

                        // There are separate tags for the compiled and noncompiled maps
                        NodeList Nodes = eElement.getElementsByTagName("SIBinaryFile");

                        int Count_Resource = 0;
                        for ( int n = 0; n < Nodes.getLength(); n++ ) {
                            Node Node_Map = Nodes.item( n );
                            if ( Node_Map.getNodeType() == 1 ) {
                                Element eDocument = (Element) Node_Map;
                                String encodedResource = eDocument.getTextContent();
                                byte[] decodedResource = Base64.getDecoder().decode( encodedResource.replace( "SIB64ENCODE", "" ) );
                                if ( Is_Default > 0 ) {
                                    // Default version
                                    this.WriteToFile( "Maps", eDocument.getAttribute( "filename" ), decodedResource );
                                }
                                else {
                                    String Version = eElement.getElementsByTagName("MAP_VERSION").item(0).getTextContent();
                                    String Extract_Name =  eDocument.getAttribute( "filename" );
                                    int dotIndex = Extract_Name.lastIndexOf('.');
                                    String namePart = (dotIndex == -1) ? Extract_Name : Extract_Name.substring(0, dotIndex);
                                    String Extract_Name_Extension = (dotIndex == -1) ? "" : Extract_Name.substring(dotIndex);
                                    Extract_Name = namePart + "_v" + Version + Extract_Name_Extension;

                                    this.WriteToFile( "Maps\\_OLD", Extract_Name, decodedResource );
                                }
                                Count_Resource++;
                            }
                        }
                        if ( Count_Resource == 2 ) {
                            Count_Exported++;
                        }

                        if ( Count_Exported == 0 ) {
                            this.Write_Log( "Export file does not contain data for: " + resourceName );
                            this.Errors.add( "Map: " + resourceName );
                        }

                    }
                } catch ( Exception e ) {
                    this.Write_Log("Error on Map: " + resourceName + " - " + e.getMessage() );
                    this.Errors.add( "Map: " + resourceName );
                    e.printStackTrace();
                }
            }
            this.Write_Log( "\t" + Count_Exported + " of " + this.Count_Maps + " exported" );
        }


        /**
         * Description: Extracts and saves XSLT files from the export document.
         * Parameters: export - The XML document containing the exported data.
         * Returns: none
         */
        public void LoopXSLT() {
            NodeList resourceList = this.DocB2BiExport.getElementsByTagName("XSLT");
            this.Write_Log("\nExporting XSLTs..." );

            String resourceName = "";
            int Count_Exported = 0;
            for(int resourceIter = 0; resourceIter < resourceList.getLength(); ++resourceIter) {
                try {
                    Node nResourceRoot = resourceList.item(resourceIter);
                    if (nResourceRoot.getNodeType() == 1) {
                        Element eElement = (Element)nResourceRoot;
                        resourceName = eElement.getElementsByTagName("NAME").item(0).getTextContent();
                        int Is_Default = eElement.getElementsByTagName("SIResourceDefaultVersion").getLength();

                        NodeList Nodes = eElement.getElementsByTagName("SIBinaryFile");

                        for ( int n = 0; n < Nodes.getLength(); n++ ) {
                            Node Node_Map = Nodes.item( n );
                            if ( Node_Map.getNodeType() == 1 ) {
                                Element eDocument = (Element) Node_Map;
                                String encodedResource = eDocument.getTextContent();
                                byte[] decodedResource = Base64.getDecoder().decode( encodedResource.replace( "SIB64ENCODE", "" ) );
                                if ( Is_Default > 0 ) {
                                    // Default version
                                    this.WriteToFile( "XSLTs", eDocument.getAttribute( "filename" ) + ".xslt", decodedResource );
                                }
                                else {
                                    String Version = eElement.getElementsByTagName("VERSION").item(0).getTextContent();
                                    this.WriteToFile( "XSLTs\\_OLD", eDocument.getAttribute( "filename" ) + "_v" + Version + ".xslt", decodedResource );
                                }
                                Count_Exported++;
                            }
                        }

                        if ( Count_Exported == 0 ) {
                            this.Write_Log( "Export file does not contain data for: " + resourceName );
                            this.Errors.add( "XSLT: " + resourceName );
                        }

                   }
                } catch ( Exception e ) {
                    this.Write_Log("Error on XSLT: " + resourceName + " - " + e.getMessage() );
                    this.Errors.add( "XSLT: " + resourceName );
                    e.printStackTrace();
                }
            }
            this.Write_Log( "\t" + Count_Exported + " of " + this.Count_XSLTs + " exported" );
        }


        /**
         * Description: Extracts and saves XML Schemas from the export document.
         * Parameters: export - The XML document containing the exported data.
         * Returns: none
         */
        public void LoopXMLSchema() {
            NodeList resourceList = this.DocB2BiExport.getElementsByTagName("SCHEMA");
            this.Write_Log("\nExporting XML Schemas..." );

            String resourceName = "";
            int Count_Exported = 0;
            for(int resourceIter = 0; resourceIter < resourceList.getLength(); ++resourceIter) {
                try {
                    Node nResourceRoot = resourceList.item(resourceIter);
                    if (nResourceRoot.getNodeType() == 1) {
                        Element eElement = (Element)nResourceRoot;
                        resourceName = eElement.getElementsByTagName("NAME").item(0).getTextContent();
                        int Is_Default = eElement.getElementsByTagName("SIResourceDefaultVersion").getLength();

                        NodeList Nodes = eElement.getElementsByTagName("SIBinaryFile");

                        for ( int n = 0; n < Nodes.getLength(); n++ ) {
                            Node Node_Map = Nodes.item( n );
                            if ( Node_Map.getNodeType() == 1 ) {
                                Element eDocument = (Element) Node_Map;
                                String encodedResource = eDocument.getTextContent();
                                byte[] decodedResource = Base64.getDecoder().decode( encodedResource.replace( "SIB64ENCODE", "" ) );
                                if ( Is_Default > 0 ) {
                                    // Default version
                                    this.WriteToFile( "XML_Schemas", eDocument.getAttribute( "filename" ), decodedResource );
                                }
                                else {
                                    String Version = eElement.getElementsByTagName("VERSION").item(0).getTextContent();
                                    String Extract_Name =  eDocument.getAttribute( "filename" );
                                    int dotIndex = Extract_Name.lastIndexOf('.');
                                    String namePart = (dotIndex == -1) ? Extract_Name : Extract_Name.substring(0, dotIndex);
                                    String Extract_Name_Extension = (dotIndex == -1) ? "" : Extract_Name.substring(dotIndex);
                                    Extract_Name = namePart + "_v" + Version + Extract_Name_Extension;

                                    this.WriteToFile( "XML_Schemas\\_OLD", Extract_Name, decodedResource );
                                }
                                Count_Exported++;
                            }
                        }

                        if ( Count_Exported == 0 ) {
                            this.Write_Log( "Export file does not contain data for: " + resourceName );
                            this.Errors.add( "XML Schema: " + resourceName );
                        }

                    }
                } catch ( Exception e ) {
                    this.Write_Log("Error on XML Schema: " + resourceName + " - " + e.getMessage() );
                    this.Errors.add( "XML Schema: " + resourceName );
                    e.printStackTrace();
                }
            }
            this.Write_Log( "\t" + Count_Exported + " of " + this.Count_XMLSchema + " exported" );
        }



    /**
     * Description: Extracts and saves User Identity Keys from the export document.
     * Parameters: export - The XML document containing the exported data.
     * Returns: none
     */
    public void LoopUserIdentKey() {
        NodeList resourceList = this.DocB2BiExport.getElementsByTagName("SSH_HOST_USER_IDENTITY_KEY");
        this.Write_Log("\nExporting User Identity Keys..." );

        String resourceName = "";
        int Count_Exported = 0;
        for(int resourceIter = 0; resourceIter < resourceList.getLength(); ++resourceIter) {
            try {
                Node nResourceRoot = resourceList.item(resourceIter);
                if (nResourceRoot.getNodeType() == 1) {
                    Element eElement = (Element)nResourceRoot;
                    resourceName = eElement.getElementsByTagName("NAME").item(0).getTextContent();

                    NodeList Nodes = eElement.getElementsByTagName("RAW_STORE");

                    for ( int n = 0; n < Nodes.getLength(); n++ ) {
                        Node Node_Map = Nodes.item( n );
                        if ( Node_Map.getNodeType() == 1 ) {
                            Element eDocument = (Element) Node_Map;
                            String encodedResource = eDocument.getTextContent();
                            byte[] decodedResource = Base64.getDecoder().decode( encodedResource.replace( "SIB64ENCODE", "" ) );
                            this.WriteToFile( "Keys_User_Identity_Keys", resourceName + ".ppk", decodedResource );
                            Count_Exported++;
                        }
                    }

                    if ( Count_Exported == 0 ) {
                        this.Write_Log( "Export file does not contain data for: " + resourceName );
                        this.Errors.add( "User Identity Key: " + resourceName );
                    }

                }
            } catch ( Exception e ) {
                this.Write_Log("Error on User Identity Key: " + resourceName + " - " + e.getMessage() );
                this.Errors.add( "User Identity Key: " + resourceName );
                e.printStackTrace();
            }
        }
        this.Write_Log( "\t" + Count_Exported + " of " + this.Count_Key_User_ID + " exported" );
    }


    /**
     * Description: Extracts and saves Known Host Keys from the export document.
     * Parameters: export - The XML document containing the exported data.
     * Returns: none
     */
    public void LoopKnownHostKey() {
        NodeList resourceList = this.DocB2BiExport.getElementsByTagName("SSH_KNOWN_HOST_KEY");
        this.Write_Log("\nExporting Known Host Keys..." );

        String resourceName = "";
        int Count_Exported = 0;
        for(int resourceIter = 0; resourceIter < resourceList.getLength(); ++resourceIter) {
            try {
                Node nResourceRoot = resourceList.item(resourceIter);
                if (nResourceRoot.getNodeType() == 1) {
                    Element eElement = (Element)nResourceRoot;
                    resourceName = eElement.getElementsByTagName("NAME").item(0).getTextContent();

                    NodeList Nodes = eElement.getElementsByTagName("RAW_STORE");

                    for ( int n = 0; n < Nodes.getLength(); n++ ) {
                        Node Node_Map = Nodes.item( n );
                        if ( Node_Map.getNodeType() == 1 ) {
                            Element eDocument = (Element) Node_Map;
                            String encodedResource = eDocument.getTextContent();
                            byte[] decodedResource = Base64.getDecoder().decode( encodedResource.replace( "SIB64ENCODE", "" ) );
                            this.WriteToFile( "Keys_Known_Host_Keys", resourceName + ".pub", decodedResource );
                            Count_Exported++;
                        }
                    }

                    if ( Count_Exported == 0 ) {
                        this.Write_Log( "Export file does not contain data for: " + resourceName );
                        this.Errors.add( "Known Host Key: " + resourceName );
                    }

                }
            } catch ( Exception e ) {
                this.Write_Log("Error on Known Host Key: " + resourceName + " - " + e.getMessage() );
                this.Errors.add( "Known Host Key: " + resourceName );
                e.printStackTrace();
            }
        }
        this.Write_Log( "\t" + Count_Exported + " of " + this.Count_XMLSchema + " exported" );
    }

    /**
     * Description: Extracts and saves Authorized User Keys from the export document.
     * Parameters: export - The XML document containing the exported data.
     * Returns: none
     */
    public void LoopAuthorizedUserKey() {
        NodeList resourceList = this.DocB2BiExport.getElementsByTagName("SSH_AUTHORIZED_USER_KEY");
        this.Write_Log("\nExporting Authorized User Keys..." );

        String resourceName = "";
        int Count_Exported = 0;
        for(int resourceIter = 0; resourceIter < resourceList.getLength(); ++resourceIter) {
            try {
                Node nResourceRoot = resourceList.item(resourceIter);
                if (nResourceRoot.getNodeType() == 1) {
                    Element eElement = (Element)nResourceRoot;
                    resourceName = eElement.getElementsByTagName("NAME").item(0).getTextContent();

                    NodeList Nodes = eElement.getElementsByTagName("RAW_STORE");

                    for ( int n = 0; n < Nodes.getLength(); n++ ) {
                        Node Node_Map = Nodes.item( n );
                        if ( Node_Map.getNodeType() == 1 ) {
                            Element eDocument = (Element) Node_Map;
                            String encodedResource = eDocument.getTextContent();
                            byte[] decodedResource = Base64.getDecoder().decode( encodedResource.replace( "SIB64ENCODE", "" ) );
                            this.WriteToFile( "Keys_Authorized_User_Keys", resourceName + ".pub", decodedResource );
                            Count_Exported++;
                        }
                    }

                    if ( Count_Exported == 0 ) {
                        this.Write_Log( "Export file does not contain data for: " + resourceName );
                        this.Errors.add( "Authorized User Key: " + resourceName );
                    }

                }
            } catch ( Exception e ) {
                this.Write_Log("Error on Authorized User Key: " + resourceName + " - " + e.getMessage() );
                this.Errors.add( "Authorized User Key: " + resourceName );
                e.printStackTrace();
            }
        }
        this.Write_Log( "\t" + Count_Exported + " of " + this.Count_XMLSchema + " exported" );
    }



        /**
         * Description: Displays all accumulated errors from resource processing.
         * Parameters: none
         * Returns: none
         */
        public void Display_Errors() {
            if ( !this.Errors.isEmpty() ) {
                this.Write_Log("\n\n**********************************************");
                this.Write_Log("Errors encountered during resource processing:");
                this.Write_Log( "Check the log for information." );
                for ( String Error : this.Errors ) {
                    this.Write_Log("\t" + Error );
                }
                this.Errors.clear();
            }
        }




    /**
     * Description: Creates an ImageIcon from the specified path.
     * Parameters: path - The path to the image file.
     * Returns: ImageIcon - The created ImageIcon object, or null if the image file cannot be found.
     */
    protected static ImageIcon createImageIcon(String path) {
        URL imgURL = DecodeExtract.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Description: Initializes and displays the DecodeExtract GUI.
     * Parameters: none
     * Returns: none
     */
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Decode and Extract");
        frame.setDefaultCloseOperation(3);
        frame.add(new DecodeExtract());
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Description: Main method to launch the DecodeExtract application.
     * Parameters: args - Command line arguments.
     * Returns: none
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);

                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    UIManager.put("Button.font", new Font("Arial", 0, 14));
                    UIManager.put("TextArea.font", new Font("Arial", 0, 14));
                    UIManager.put("ToolTip.font", new Font("Arial", 0, 14));
                } catch (Exception var2) {
                    var2.printStackTrace();
                }

                DecodeExtract.createAndShowGUI();
            }
        });
    }
}