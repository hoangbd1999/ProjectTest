package com.elcom.vn.processor.cleaner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author anhdv
 */
public class DirectoryWatcherExample {

    public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        
        WatchService watchService = FileSystems.getDefault().newWatchService();

        File rootFolder = new File("H:\\Work\\SatScan\\scan_results");
        Path path = rootFolder.toPath();
        
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        WatchKey key;
        while ((key = watchService.take()) != null) {
            
            for (WatchEvent<?> event : key.pollEvents()) {
                
                System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                
                File folder = new File(rootFolder.getPath() + File.separator + event.context());
                if( folder.isDirectory() ) {
                    try {
                        processFolder(folder);
                    } catch (Exception e) {
                        System.err.println("ex: " + e.toString());
                    }
                }
            }
            key.reset();
        }
    }
    
    private static void processFolder(File folder) {
        
        System.out.println("process for folder: " + folder.getPath());

        File[] resultFile = folder.listFiles( s -> s.getName().endsWith("result.xml") );
        if( resultFile == null || resultFile.length == 0 ) {
            System.out.println("chua co file result.xml");
            try {
                Thread.sleep(5000L);
                processFolder(folder);
            } catch (Exception e) {
                System.err.println("ex: " + e.toString());
            }
        } else {
            try {
                
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                Document doc = dbf.newDocumentBuilder().parse(resultFile[0]);
                doc.getDocumentElement().normalize();
                NodeList nListCarriers = doc.getElementsByTagName("Carriers");
                if( nListCarriers != null && nListCarriers.getLength() > 0 ) {
                    Node nNodeCarriers = nListCarriers.item(0);
                    if (nNodeCarriers.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElementCarriers = (Element) nNodeCarriers;

                        String scanStopTime = getCharacterDataFromElement(eElementCarriers, "Scan_Stop_Time");
                        System.out.println("scanStopTime: " + scanStopTime);

                        String Satscan_Version = getCharacterDataFromElement(eElementCarriers, "Satscan_Version");
                        System.out.println("Satscan_Version: " + Satscan_Version);
                        
                        NodeList nameCarrier = eElementCarriers.getElementsByTagName("Carrier");
                        for (int temp2 = 0; temp2 < nameCarrier.getLength(); temp2++) {
                            Node nNodeCarrier = nameCarrier.item(temp2);
                            if (nNodeCarrier.getNodeType() != Node.ELEMENT_NODE)
                                continue;
                            Element eElementCarrier = (Element) nNodeCarrier;
                            
                            String frequency = getCharacterDataFromElement(eElementCarrier, "Frequency");
                            System.out.println("frequency: " + frequency);
                            
                            String Code_Rate = getCharacterDataFromElement(eElementCarrier, "Code_Rate");
                            System.out.println("Code_Rate: " + Code_Rate);
                            
                        } 
                    }
                }
                
                /*if( StringUtil.isNullOrEmpty(resultFileXml.getScan_Stop_Time()) ) {
                    System.out.println("chua co Scan_Stop_Time");
                    Thread.sleep(5000L);
                    processFolder(folder);
                } else {
                    System.out.println("tmpXmlFile.Scan_Stop_Time: " + resultFileXml.getScan_Stop_Time());
                }*/
            } catch (Exception e) {
                System.err.println("ex: " + e.toString());
            }
        }
    }
    
    private static String getCharacterDataFromElement(Element e, String tagName) {
        try {
            Element eChild = (Element) e.getElementsByTagName(tagName).item(0);
            Node child = eChild.getFirstChild();
            if (child instanceof CharacterData)
                return ((CharacterData) child).getData();
        } catch (Exception ex) {
            System.err.println("ex: " + ex.toString());
        }
        return null;
    }
}
