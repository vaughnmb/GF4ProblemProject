package com.iecokc.bom.model.util;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.iecokc.bom.model.bean.AssemblyContext;
import com.iecokc.bom.model.bean.Product;
import com.iecokc.bom.model.bean.impl.BaseAssemblyContext;
import com.iecokc.bom.model.exception.WhereIsYourGodNowException;

public class ProductSelector {
	
	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	private static final XPathFactory xpathFactory = XPathFactory.newInstance();
	private static final String ASSEMBLY_CONTEXT_KEY = "assemblyContext";
	private static final Pattern STARTS_WITH_A_LETTER = Pattern.compile("^[a-z].*", Pattern.CASE_INSENSITIVE);
	
	private final Document document;

	public ProductSelector(Product product) {
		this.document = buildDoc(product);
	}
	
	private Document buildDoc(Product product) {
		Document doc = null;
		try {
			doc = docBuilderFactory.newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException e) {
			throw new WhereIsYourGodNowException(e);
		}
		Element elem = buildElement(doc, new BaseAssemblyContext(product));
		doc.appendChild(elem);
		return doc;
	}
	
	private Element buildElement(Document doc, AssemblyContext context) {
		Product product = context.getProduct();
		Element elem = doc.createElement(validateTag(product.getPlannerId()));
		// fill out element attributes
		String description = product.getDescription();
		if (!(product.getSecondaryDescription() == null || product.getSecondaryDescription().trim().isEmpty())) {
			description = description + " - " + product.getSecondaryDescription();
		}
		elem.setAttribute(Attributes.DESCRIPTION.getAttribute(), description);
		elem.setAttribute(Attributes.PRODUCT.getAttribute(), product.getProductNumber());
		elem.setAttribute(Attributes.TYPE.getAttribute(), product.getTypeFlag());
		if (product.getObsolete()) {
			elem.setAttribute(Attributes.OBSOLETE.getAttribute(), "true");
		}
		elem.setAttribute(Attributes.QTY.getAttribute(), context.getQuantity().toString());
		elem.setAttribute(Attributes.TOTALQTY.getAttribute(), context.getTotalQuantity().toString());
		elem.setUserData(ASSEMBLY_CONTEXT_KEY, context, null);
		for (AssemblyContext ac : context.getChildren()) {
			elem.appendChild(buildElement(doc, ac));
		}
		return elem;
	}
	
	/**
	 * Usually our planner ids make valid tag names, but we may have outliers
	 */
	private String validateTag(String potentialTagName) {
		String tag = potentialTagName;
		// Make something up if the planner id is missing
		if (tag == null) {
			tag = "xxxx";
		}
		tag = tag.trim().toLowerCase().replaceAll(" ", "");
		// prepend an x if it doesn't start with a letter
		if (!STARTS_WITH_A_LETTER.matcher(tag).matches()) {
			tag = "x" + tag;
		}
		return tag;
	}
	
	public List<AssemblyContext> select(String xpath) throws XPathExpressionException {
		return select(xpathFactory.newXPath().compile(xpath));
	}
	
	public List<AssemblyContext> select(XPathExpression xpathExpr) throws XPathExpressionException {
		NodeList nl = (NodeList) xpathExpr.evaluate(this.document, XPathConstants.NODESET);
		List<AssemblyContext> ret = new ArrayList<AssemblyContext>();
		for (int i=0; i<nl.getLength(); i++) {
			ret.add((AssemblyContext)nl.item(i).getUserData(ASSEMBLY_CONTEXT_KEY));
		}
		return ret;
	}
	

	public String toXml() {
		
		Writer out = new StringWriter();
		try {
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.transform(new DOMSource(this.document), new StreamResult(out));
		}
		catch (TransformerException e) {
			return super.toString();
		}
		return out.toString();
	}
	
	/**
	 * Yes, I want these to be lowercase, despite the java naming convention, since I use
	 * the values in XML
	 */
	public static enum Attributes {
		
		DESCRIPTION("description"),
		PRODUCT("product"),
		TYPE("type"),
		OBSOLETE("obsolete"),
		QTY("qty"),
		TOTALQTY("totalqty");
		
		private String attribute;
		
		private Attributes(String attribute) {
			this.attribute = attribute;
		}
		
		public String getAttribute() {
			return attribute;
		}
		
		public static Attributes findByXmlAttribute(String value) {
			Attributes ret = null;
			if (value != null) {
				for (Attributes a : Attributes.values()) {
					if (value.equals(a.getAttribute())) {
						ret = a;
						break;
					}
				}
			}
			return ret;
		}
	}
}

