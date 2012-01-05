package org.kvj.keepass2org;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLDocumentBuilder {

	private Element rootElement = null;
	private Document doc = null;

	/** Creates a new instance of XMLDocumentBuilder */
	public XMLDocumentBuilder() {
	}

	public static String nodeToString(Node node, boolean format) {
		XMLDocumentBuilder xml = new XMLDocumentBuilder();
		return xml.makeXML(node, format);
	}

	public void addText(Element node, String text) {
		node.appendChild(getDocument().createTextNode(text));
	}

	public void addChildWithValue(Node e, String name, String err) {
		Element n = doc.createElement(name);
		e.appendChild(n);
		addText(n, err == null ? "" : err);
	}

	public Document getDocument() {
		if (doc == null) {
			try {
				DocumentBuilderFactory dbfac = DocumentBuilderFactory
						.newInstance();
				dbfac.setNamespaceAware(true);
				DocumentBuilder docBuilder;

				docBuilder = dbfac.newDocumentBuilder();
				doc = docBuilder.newDocument();

			} catch (ParserConfigurationException ex) {
				ex.printStackTrace();
			}
		}
		return doc;
	}

	public boolean loadXML(String source) {
		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			dbfac.setNamespaceAware(true);
			DocumentBuilder docBuilder;

			docBuilder = dbfac.newDocumentBuilder();
			doc = docBuilder.parse(new InputSource(new StringReader(source)));
			rootElement = doc.getDocumentElement();
			return true;
		} catch (SAXException ex) {
			Logger.getLogger(XMLDocumentBuilder.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(XMLDocumentBuilder.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public boolean readXML(String fileName) {
		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;

			docBuilder = dbfac.newDocumentBuilder();
			doc = docBuilder.parse(new FileInputStream(fileName));
			rootElement = doc.getDocumentElement();
			return true;
		} catch (SAXException ex) {
			Logger.getLogger(XMLDocumentBuilder.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(XMLDocumentBuilder.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public boolean readXML(InputStream stream) {
		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;

			docBuilder = dbfac.newDocumentBuilder();
			doc = docBuilder.parse(stream);
			rootElement = doc.getDocumentElement();
			return true;
		} catch (SAXException ex) {
			Logger.getLogger(XMLDocumentBuilder.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(XMLDocumentBuilder.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public Element getRootElement(String nodeNS, String nodeName) {
		if (rootElement == null) {
			rootElement = createElement(nodeNS, nodeName);
			getDocument().appendChild(rootElement);
		}
		return rootElement;
	}

	public Element getRootElement(String nodeName) {
		if (rootElement == null) {
			rootElement = getDocument().createElement(nodeName);
			getDocument().appendChild(rootElement);
		}
		return rootElement;
	}

	public Element getRootElement() {
		if (rootElement == null) {
			rootElement = getDocument().createElement("root");
			getDocument().appendChild(rootElement);
		}
		return rootElement;
	}

	public Element createElement(String name) {
		return createElement(null, name);
	}

	public Element createElement(String ns, String name) {
		String _name = name;
		String _prefix = "";
		int idx = _name.indexOf(":");
		if (idx != -1) {
			_prefix = _name.substring(0, idx);
			_name = _name.substring(idx + 1);
		}
		if (ns != null && !"".equals(ns)) {
			Element e = getDocument().createElementNS(ns, _name);
			e.setPrefix(_prefix);
			return e;
		}
		return getDocument().createElement(_name);
	}

	public String makeXML() {
		return makeXML(getDocument(), false);

	}

	public String makeXML(boolean format) {
		return makeXML(getDocument(), format);

	}

	public String makeXML(Node root, boolean format) {
		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			if (format) {
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
			} else {
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				trans.setOutputProperty(OutputKeys.INDENT, "no");
			}

			// create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(root);
			trans.transform(source, result);
			String xmlString = sw.toString();
			return xmlString;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return "";
	}

	private String tabs(int size) {
		StringBuilder sb = new StringBuilder("\n");
		for (int i = 0; i < size; i++) {
			sb.append('\t');
		}
		return sb.toString();
	}

	private void prettyFormat(Node node, int level) {
		if (level > 0) {
			node.getParentNode().insertBefore(
					getDocument().createTextNode(tabs(level)), node);
		}
		NodeList nl = node.getChildNodes();
		List<Node> list = new ArrayList<Node>();
		for (int i = 0; i < nl.getLength(); i++) {
			list.add(nl.item(i));
		}
		for (int i = 0; i < list.size(); i++) {
			Node n = list.get(i);
			if (n instanceof Element) {
				prettyFormat(n, level + 1);
				continue;
			}
			if (n instanceof Comment) {
				String[] lines = n.getNodeValue().split("\n");
				StringBuilder sb = new StringBuilder(lines[0]);
				for (int j = 1; j < lines.length; j++) {
					// sb.append( '\n' );
					sb.append(tabs(level + 1));
					sb.append(lines[j].trim());
				}
				n.setNodeValue(sb.toString());
				node.insertBefore(
						getDocument().createTextNode(tabs(level + 1)), n);
				continue;
			}
		}
		if (nl.getLength() > 0) {
			node.appendChild(getDocument().createTextNode(tabs(level)));
		}
	}

	public boolean writeXML(Node root, String fileName, boolean xmlDecl,
			boolean indent) {
		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			if (indent) {
				prettyFormat(root, 0);
			}
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					xmlDecl ? "no" : "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "no");

			// create string from xml tree
			FileOutputStream fos = new FileOutputStream(fileName);
			StreamResult result = new StreamResult(fos);
			DOMSource source = new DOMSource(root);
			trans.transform(source, result);
			fos.close();
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	public NodeList evaluateXPath(String expression) {
		return evaluateXPath(expression, getDocument());
	}

	public NodeList evaluateXPath(String expression, Node e) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			return (NodeList) xpath.evaluate(expression, e,
					XPathConstants.NODESET);
		} catch (XPathExpressionException ex) {
			Logger.getLogger(XMLDocumentBuilder.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return null;
	}

	public Node evaluateXPathOneNode(String expression) {
		return evaluateXPathOneNode(expression, getDocument());
	}

	public Node evaluateXPathOneNode(String expression, Node e) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nl = (NodeList) xpath.evaluate(expression, e,
					XPathConstants.NODESET);
			if (null != nl && nl.getLength() > 0) {
				return nl.item(0);
			}
		} catch (XPathExpressionException ex) {
			Logger.getLogger(XMLDocumentBuilder.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return null;
	}

	public String makeXSLTransform(String xslPath) {
		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Templates template = transfac.newTemplates(new StreamSource(this
					.getClass().getResourceAsStream(xslPath)));
			Transformer trans = template.newTransformer();

			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(getDocument());
			trans.transform(source, result);
			String xmlString = sw.toString();

			return xmlString;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return "";
	}

	public void setRootElement(Element root) {
		this.rootElement = root;
		this.doc = root.getOwnerDocument();
	}

	public Element getChildElement(Node n, String name) {
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (name.equals(nl.item(i).getNodeName())) {
				return (Element) nl.item(i);
			}
		}
		return null;
	}
}
