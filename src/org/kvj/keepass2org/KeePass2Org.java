package org.kvj.keepass2org;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KeePass2Org {

	private static void parse(InputStream inputStream, Writer outWriter) {
		XMLDocumentBuilder xml = new XMLDocumentBuilder();
		try {
			if (!xml.readXML(inputStream)) {
				System.err.println("Error parsing XML");
				return;
			}
			List<String> groups = new ArrayList<String>();
			NodeList nl = xml.evaluateXPath("/pwlist/pwentry/group/text()");
			for (int i = 0; i < nl.getLength(); i++) {
				String group = nl.item(i).getNodeValue();
				if (!groups.contains(group)) {
					groups.add(group);
				}
			}
			for (String group : groups) {
				nl = xml.evaluateXPath("/pwlist/pwentry[group/text() = '"
						+ group + "']");
				// System.err.println("Group: " + group + ", " +
				// nl.getLength());
				outWriter.write(String.format("* %s\n", group));
				for (int i = 0; i < nl.getLength(); i++) {
					Element pwentry = (Element) nl.item(i);
					Node urlNode = xml.evaluateXPathOneNode("./url/text()",
							pwentry);
					String url = "";
					if (null != urlNode) {
						url = urlNode.getNodeValue();
					}
					String name = xml.evaluateXPathOneNode("title/text()",
							pwentry).getNodeValue();
					Node usernameNode = xml.evaluateXPathOneNode(
							"username/text()", pwentry);
					Node passwordNode = xml.evaluateXPathOneNode(
							"password/text()", pwentry);
					Node notesNode = xml.evaluateXPathOneNode("notes/text()",
							pwentry);

					if (!"".equals(url)) {
						url = String.format(" [[%s][%s]]", url, "link");
					}
					outWriter.write(String.format("** %s%s\t:%s:\n", name, url,
							"crypt"));
					if (null != usernameNode) {
						outWriter.write(String.format("   Username: %s\n",
								usernameNode.getNodeValue()));
					}
					if (null != passwordNode) {
						outWriter.write(String.format("   Password: %s\n",
								passwordNode.getNodeValue()));
					}
					if (null != notesNode) {
						outWriter.write(String.format("   %s\n",
								notesNode.getNodeValue()));
					}
				}
			}
			outWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (0 == args.length) {
			System.err.println("No enough parameters: <IN FILE> ?<OUT FILE>");
			return;
		}
		try {
			File file = new File(args[0]);
			if (!file.exists() || !file.isFile()) {
				System.err.println("Invalid IN file");
				return;
			}

			Writer outWriter = new OutputStreamWriter(System.out, "utf-8");
			if (args.length > 1) {
				outWriter = new OutputStreamWriter(new FileOutputStream(
						args[1], false), "utf-8");
			}
			parse(new FileInputStream(file), outWriter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
