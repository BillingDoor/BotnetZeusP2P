import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLHandler {
	// Info.xml has bot info: IP, port, HardcodedpeerTable, HardcodedproxyTable
	// Config.xml has configuration info: version, etc
	// PeerList.xml has peerlist up to date info: IP, port
	String infoPath = "C:\\BotSimulator\\Info.xml";
	String malwarePath = "C:\\BotSimulator\\Config.xml";
	String peerListPath = "C:\\BotSimulator\\PeerList.xml";

	Document infoDocument = null;
	Document malwareDocument = null;
	Document peerDocument = null;

	LogWriter log = null;

	public XMLHandler(LogWriter log) {
		this.log = log;
		instantiateInfoDocument();
	}

	void instantiateInfoDocument() {
		try {
			File fXmlFile = new File(infoPath);
			DocumentBuilderFactory dbFactory = null;
			DocumentBuilder dBuilder = null;

			if (!fXmlFile.exists()) {
				log.addLogEntry("Info file is missing");
				System.exit(1);
			} else {
				log.addLogEntry("Info file exists");
				dbFactory = DocumentBuilderFactory.newInstance();
				dBuilder = dbFactory.newDocumentBuilder();
				infoDocument = dBuilder.parse(fXmlFile);
				infoDocument.getDocumentElement().normalize();
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			log.addLogEntry(e.getMessage());
			System.exit(1);
		}
	}

	void instantiateMalwareDocument() {
		try {
			File fXmlFile = new File(malwarePath);
			DocumentBuilderFactory dbFactory = null;
			DocumentBuilder dBuilder = null;

			if (!fXmlFile.exists()) {
				log.addLogEntry("Malware Configuration file is missing");
				malwareDocument = null;
			} else {
				log.addLogEntry("Info file exist");
				dbFactory = DocumentBuilderFactory.newInstance();
				dBuilder = dbFactory.newDocumentBuilder();
				malwareDocument = dBuilder.parse(fXmlFile);
				malwareDocument.getDocumentElement().normalize();
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			log.addLogEntry(e.getMessage());
			System.exit(1);
		}
	}

	void instantiatePeerDocument() {
		try {
			File fXmlFile = new File(infoPath);
			DocumentBuilderFactory dbFactory = null;
			DocumentBuilder dBuilder = null;

			if (!fXmlFile.exists()) {
				log.addLogEntry("Peer List File is missing");
				peerDocument = null;
			} else {
				log.addLogEntry("Peer List file exists");
				dbFactory = DocumentBuilderFactory.newInstance();
				dBuilder = dbFactory.newDocumentBuilder();
				peerDocument = dBuilder.parse(fXmlFile);
				peerDocument.getDocumentElement().normalize();
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			log.addLogEntry(e.getMessage());
			System.exit(1);
		}
	}

	/*
	 * get peer id from xml
	 */
	public int getMyPeerId() {
		NodeList connectionList = infoDocument
				.getElementsByTagName("connection");

		if (connectionList.getLength() == 0) {
			log.addLogEntry("Connection info is missing");
			System.exit(1);
		}

		Node peer = connectionList.item(0);
		if (peer.getNodeType() != Node.ELEMENT_NODE) {
			System.exit(1);
		}
		Element e = (Element) peer;
		String id = e.getAttribute("id");
		return Integer.parseInt(id);
	}

	/*
	 * get peer information from xml
	 */
	public Peer getMyPeerInfo() {
		NodeList connectionList = infoDocument
				.getElementsByTagName("connection");

		if (connectionList.getLength() == 0) {
			log.addLogEntry("Connection info is missing");
			System.exit(1);
		}

		log.addLogEntry("Connection info found");

		Node peer = connectionList.item(0);
		if (peer.getNodeType() != Node.ELEMENT_NODE) {
			System.exit(1);
		}
		Element e = (Element) peer;
		String id = e.getAttribute("id");
		String port = e.getElementsByTagName("port").item(0).getTextContent();
		String host = e.getElementsByTagName("host").item(0).getTextContent();
		log.addLogEntry("Peer " + id + ", Port:" + port + ", Host:" + host
				+ " was added to the peer table");
		return new Peer(port, host);
	}

	/*
	 * get version from xml
	 */
	public int getVersion() {
		NodeList connectionList = infoDocument
				.getElementsByTagName("connection");

		if (connectionList.getLength() == 0) {
			log.addLogEntry("Connection info is missing");
			System.exit(1);
		}

		log.addLogEntry("Connection info found");

		Node peer = connectionList.item(0);
		if (peer.getNodeType() != Node.ELEMENT_NODE) {
			System.exit(1);
		}
		Element e = (Element) peer;
		String version = e.getAttribute("version");
		return Integer.parseInt(version);
	}

	/*
	 * Get list of hardcoded peers from info Document
	 */
	public HashMap<Integer, Peer> getHardcodedPeerList() {
		NodeList peerList = infoDocument.getElementsByTagName("peerBot");
		return getPeers(peerList);
		
	}

	/*
	 * Get list of peers from peerDocument xml
	 */
	public HashMap<Integer, Peer> getUpdatedPeerList() {
		NodeList peerList = peerDocument.getElementsByTagName("peerBot");
		return getPeers(peerList);
	}

	public HashMap<Integer, Peer> getPeers(NodeList peerList) {
		HashMap<Integer, Peer> peersTable = new HashMap<Integer, Peer>();
		if (peerList.getLength() == 0) {
			log.addLogEntry("Peer Table is empty");
			System.exit(1);
			for (int i = 0; i < peerList.getLength(); i++) {
				Node peer = peerList.item(i);
				if (peer.getNodeType() != Node.ELEMENT_NODE) {
					System.exit(1);
				}
				Element e = (Element) peer;
				String id = e.getAttribute("id");
				String port = e.getElementsByTagName("port").item(0)
						.getTextContent();
				String host = e.getElementsByTagName("host").item(0)
						.getTextContent();
				log.addLogEntry("Peer " + id + ", " + port + "," + host
						+ "added to the peer table");
				Peer nPeer = new Peer(port, host);
				peersTable.put(Integer.parseInt(id), nPeer);
			}
		}
		return peersTable;

	}


	/*
	 * Get list of proxy peers from xml
	 */
	public HashMap<Integer, Peer> getHardcodedProxyPeerList() {
		HashMap<Integer, Peer> proxiesTable = new HashMap<Integer, Peer>();

		NodeList peerList = infoDocument.getElementsByTagName("proxyBot");
		if (peerList.getLength() == 0) {
			log.addLogEntry("Proxy Table is empty");
			System.exit(1);
			for (int i = 0; i < peerList.getLength(); i++) {
				Node peer = peerList.item(i);
				if (peer.getNodeType() != Node.ELEMENT_NODE) {
					System.exit(1);
				}
				Element e = (Element) peer;
				String id = e.getAttribute("id");
				String port = e.getElementsByTagName("port").item(0)
						.getTextContent();
				String host = e.getElementsByTagName("host").item(0)
						.getTextContent();
				log.addLogEntry("Peer " + id + ", " + port + "," + host
						+ "added to the proxy table");
				Peer nPeer = new Peer(port, host);
				proxiesTable.put(Integer.parseInt(id), nPeer);
			}
		}
		return proxiesTable;
	}

	public boolean writeMalwareFile(ByteArrayOutputStream baos) {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(malwarePath);
			bos = new BufferedOutputStream(fos);
			bos.write(baos.toByteArray());
			bos.flush();
			bos.close();
		} catch (IOException ex) {
			return false;
		}
		return true;

	}

	public void writePeerList(String content) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(peerListPath), "utf-8"));
			writer.write(content);
			System.out.println("Done");

		} catch (IOException ex) {
			System.out.println("erro writing Peer List File");
			log.addLogEntry("erro writing Peer List File");
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}
}