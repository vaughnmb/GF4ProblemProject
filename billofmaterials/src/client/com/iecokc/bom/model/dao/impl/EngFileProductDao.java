package com.iecokc.bom.model.dao.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.iecokc.bom.model.bean.Product;
import com.iecokc.bom.model.bean.Quantity;
import com.iecokc.bom.model.bean.impl.BaseProduct;
import com.iecokc.bom.model.bean.impl.PrmsProduct;
import com.iecokc.bom.model.dao.ProductDao;
import com.iecokc.bom.model.exception.InvalidFileException;

public class EngFileProductDao implements ProductDao {
	
	protected Logger logger = Logger.getLogger(getClass());
	
	private Map<String, WeakReference<BaseProduct>> products = new HashMap<String, WeakReference<BaseProduct>>();

	private static int ENG_FILE_LINE_LENGTH = 163;

	private Pattern regularAssemblyPattern = Pattern.compile("[A-Z]\\d{3}-\\d{8}");

	private Pattern topLevelAssemblyPattern = Pattern.compile("[A-Z]{3}\\d{10}");
	
	private String prn;

	// public Product getProduct(String partNumber) {
	// Product ret = null;
	//		
	// BaseProduct p = new BaseProduct();
	// p.setProductNumber(partNumber);
	// if (products.containsKey(partNumber)) {
	// WeakReference<BaseProduct> ref = products.get(partNumber);
	// if (ref != null) {
	// ret = ref.get();
	// if (ret == null) {
	// products.remove(partNumber);
	// }
	// }
	// else {
	// products.remove(partNumber);
	// }
	// }
	// return ret;
	// }

	public PrmsProduct getProduct(String partNumber) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * You must first call loadBomFile method to get the PRN from it.
	 * @return
	 */
	public String getPrnFromBomFile()
	{		
		return prn;
	}

	public Product loadBomFile(InputStream xmlFile) throws ParserConfigurationException, SAXException, IOException{
		List<BaseProduct> path = new LinkedList<BaseProduct>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		ErrorHandler errorHandler = new MyErrorHandler();
		builder.setErrorHandler(errorHandler);
		Document document = builder.parse(xmlFile);

		for (int j = 0; j < document.getChildNodes().getLength(); j++)
		{
			Node rootNode = document.getChildNodes().item(j);
			if (rootNode.getNodeName().equals("bom"))
			{
				for (int i = 0; i < rootNode.getChildNodes().getLength(); i++)
				{
					Node node = rootNode.getChildNodes().item(i);
					if (node.getNodeName().equalsIgnoreCase("prn"))
					{
						prn = node.getTextContent();
					}
					else if (node.getNodeName().equalsIgnoreCase("bomdata"))
					{
						// found the bomdata node
						for (int k = 0; k < node.getChildNodes().getLength(); k++)
						{
							// found the bomentry(s) nodes
							Node bomEntryNode = node.getChildNodes().item(k);
							if (bomEntryNode.getNodeName()
								.equalsIgnoreCase("bomentry"))
							{

								// Create the product
								BaseProduct prd = createProduct(bomEntryNode);

								// Find the depth
								int depth = findDepth(bomEntryNode);

								// Find qty
								Quantity qty = findQuantity(bomEntryNode);

								// add to BOM object structure
								BaseProduct cachedProduct = addProductToBomStructure(prd,
									depth,
									path,
									qty);

								// Create the raw material
								BaseProduct rawMaterialPrd = addRawMaterial(prd,
									bomEntryNode);

								// See if we have a raw material
								if (cachedProduct == null)
								{
									if (rawMaterialPrd != null)
									{
										depth++;
										if (depth == path.size())
										{
											path.add(rawMaterialPrd);
										}
										else
										{
											path.set(depth, rawMaterialPrd);
										}
										products.put(rawMaterialPrd.getProductNumber(),
											new WeakReference<BaseProduct>(rawMaterialPrd)); // cache
									}
									products.put(prd.getProductNumber(),
										new WeakReference<BaseProduct>(prd)); // cache
								}
							}
						}

					}
				}
			}
		}
		return path.get(0);
	}

	public Product loadEngFile(InputStream is) throws InvalidFileException,
			IOException {

		// Parse the file
		BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
		String line;
		int depth = 0;
		List<BaseProduct> path = new LinkedList<BaseProduct>();
		while ((line = rdr.readLine()) != null)
		{
			// skip blank lines
			if (line.trim().length() == 0)
			{
				continue;
			}

			// Check the line length
			if (line.length() != ENG_FILE_LINE_LENGTH)
			{
				throw new InvalidFileException("File line wrong length: ("
						+ line.length() + " characters)");
			}

			// See if we are being redundant
			depth = Integer.parseInt(line.substring(4, 6).trim());
			if (depth > path.size())
			{
				continue;
			}

			// Create the Product
			BaseProduct prd = parseEngTextLineToProduct(line);
			Quantity qty = parseEngTextLineToQty(line);
			BaseProduct x = null;
			if (products.containsKey(prd.getProductNumber()))
			{
				WeakReference<BaseProduct> ref = products.get(prd.getProductNumber());
				if (ref != null)
				{
					x = ref.get();
				}
			}
			if (x != null)
			{
				prd = x;
			}

			// Fix up the path
			if (depth == path.size())
			{
				path.add(prd);
			}
			else
			{
				path.set(depth, prd);
			}
			path = path.subList(0, depth + 1);

			// Add the new product to the parent
			if (depth > 0)
			{
				path.get(depth - 1).addChild(prd, qty);
			}

			// See if we have a raw material
			if (x == null)
			{
				BaseProduct rm = addRawMatl(prd, line);
				if (rm != null)
				{
					depth++;
					if (depth == path.size())
					{
						path.add(rm);
					}
					else
					{
						path.set(depth, rm);
					}
					products.put(rm.getProductNumber(),
						new WeakReference<BaseProduct>(rm)); // cache
				}
				products.put(prd.getProductNumber(),
					new WeakReference<BaseProduct>(prd)); // cache
			}

		}

		return path.get(0);
	}

	private BaseProduct parseEngTextLineToProduct(String line)
			throws InvalidFileException {
		/*
		 * TODO - This is nasty implicit-definition of the engineering file
		 * format This should be extracted and formalized
		 */
		BaseProduct prd = new BaseProduct();
		prd.setDescription(line.substring(22, 53).trim());
		prd.setSecondaryDescription(line.substring(53, 84).trim());
		prd.setEcnDate(new SimpleDateFormat("dd/MM/yy").parse(line.substring(109,
			120)
			.trim(),
			new ParsePosition(0)));
		prd.setLastEcn(line.substring(98, 109).trim());
		prd.setObsolete(false);
		prd.setProductNumber(line.substring(6, 22).trim());
		prd.setTypeFlag(line.substring(96, 98).trim());
		prd.setUnitOfMeasure(line.substring(160, 163).trim());

		if ("".equals(prd.getProductNumber()))
		{
			throw new InvalidFileException("Could not find part number on line: "
					+ line);
		}

		// Set the planner id
		if (regularAssemblyPattern.matcher(prd.getProductNumber()).matches())
		{
			prd.setPlannerId(prd.getProductNumber().substring(0, 4));
		}
		else if (topLevelAssemblyPattern.matcher(prd.getProductNumber())
			.matches())
		{
			prd.setPlannerId("F900");
		}
		else
		{
			prd.setPlannerId("");
		}

		return prd;
	}

	private Quantity parseEngTextLineToQty(String line) {
		return new Quantity(Double.parseDouble(line.substring(84, 96).trim()));
	}

	private BaseProduct addProductToBomStructure(BaseProduct prd, int depth,
			List<BaseProduct> path, Quantity qty) {
		BaseProduct cachedProduct = null;
		if (products.containsKey(prd.getProductNumber()))
		{
			WeakReference<BaseProduct> ref = products.get(prd.getProductNumber());
			if (ref != null)
			{
				cachedProduct = ref.get();
			}
		}
		if (cachedProduct != null)
		{
			prd = cachedProduct;
		}

		// Fix up the path
		if (depth == path.size())
		{
			path.add(prd);
		}
		else
		{
			path.set(depth, prd);
		}
		path = path.subList(0, depth + 1);

		// Add the new product to the parent
		if (depth > 0)
		{
			path.get(depth - 1).addChild(prd, qty);
		}

		return cachedProduct;
	}

	private BaseProduct addRawMaterial(BaseProduct product, Node bomEntryNode) {
		BaseProduct rmProduct = null;
		String rawMaterialProductNumber = null;
		String reqQty = null;
		String engQty = null;
		for (int x = 0; x < bomEntryNode.getChildNodes().getLength(); x++)
		{
			Node bomEntryChild = bomEntryNode.getChildNodes().item(x);
			if (bomEntryChild.getNodeName().equalsIgnoreCase("raw-material"))
			{
				rawMaterialProductNumber = bomEntryChild.getTextContent().trim();
			}
			else if (bomEntryChild.getNodeName()
				.equalsIgnoreCase("required-qty"))
			{
				reqQty = bomEntryChild.getTextContent().trim();
			}
			else if (bomEntryChild.getNodeName()
				.equalsIgnoreCase("engineering-qty"))
			{
				engQty = bomEntryChild.getTextContent().trim();
			}
		}

		if (!"".equals(rawMaterialProductNumber))
		{
			rmProduct = new BaseProduct();
			rmProduct.setProductNumber(rawMaterialProductNumber);
			Quantity rmQty = null;
			try
			{
				rmQty = new Quantity(Double.parseDouble(reqQty),
					Double.parseDouble(engQty));

			}
			catch (NumberFormatException nfe)
			{
				rmQty = new Quantity(0.0, 0.0);
			}
			rmProduct.setUnitOfMeasure(product.getUnitOfMeasure());
			product.setUnitOfMeasure("EA");
			rmProduct.setTypeFlag("B");
			rmProduct.setObsolete(false);
			rmProduct.setPlannerId("");
			rmProduct.setDescription("");
			rmProduct.setSecondaryDescription("");
			rmProduct.setLastEcn("");
			product.addChild(rmProduct, rmQty);
		}

		return rmProduct;
	}

	private Quantity findQuantity(Node bomEntryNode) {
		Quantity qty = null;
		for (int x = 0; x < bomEntryNode.getChildNodes().getLength(); x++)
		{
			Node bomEntryChild = bomEntryNode.getChildNodes().item(x);
			if (bomEntryChild.getNodeName().equalsIgnoreCase("quantity"))
			{
				qty = new Quantity(Double.parseDouble(bomEntryChild.getTextContent()
					.trim()));
			}
		}
		return qty;
	}

	private int findDepth(Node bomEntryNode) {
		int depth = 0;
		for (int x = 0; x < bomEntryNode.getChildNodes().getLength(); x++)
		{
			Node bomEntryChild = bomEntryNode.getChildNodes().item(x);
			if (bomEntryChild.getNodeName().equalsIgnoreCase("depth"))
			{
				depth = Integer.valueOf(bomEntryChild.getTextContent().trim())
					.intValue();
			}
		}
		return depth;
	}

	private BaseProduct createProduct(Node bomEntryNode) {
		BaseProduct baseProduct = new BaseProduct();
		for (int x = 0; x < bomEntryNode.getChildNodes().getLength(); x++)
		{
			Node bomEntryChild = bomEntryNode.getChildNodes().item(x);

			if (bomEntryChild.getNodeName().equalsIgnoreCase("product-number"))
			{				
				
				String productNumber = bomEntryChild.getTextContent().trim();
				baseProduct.setProductNumber(productNumber);

				// Set the planner id
				if (regularAssemblyPattern.matcher(baseProduct.getProductNumber())
					.matches())
				{
					baseProduct.setPlannerId(baseProduct.getProductNumber()
						.substring(0, 4));
				}
				else if (topLevelAssemblyPattern.matcher(baseProduct.getProductNumber())
					.matches())
				{
					baseProduct.setPlannerId("F900");
				}
				else
				{
					baseProduct.setPlannerId("");
				}
			}
			else if (bomEntryChild.getNodeName()
				.equalsIgnoreCase("description"))
			{
				baseProduct.setDescription(bomEntryChild.getTextContent().trim());
			}
			else if (bomEntryChild.getNodeName()
				.equalsIgnoreCase("secondary-description"))
			{
				baseProduct.setSecondaryDescription(bomEntryChild.getTextContent()
					.trim());
			}
			else if (bomEntryChild.getNodeName().equalsIgnoreCase("type-flag"))
			{
				baseProduct.setTypeFlag(bomEntryChild.getTextContent().trim());
			}
			else if (bomEntryChild.getNodeName().equalsIgnoreCase("last-ecn"))
			{
				baseProduct.setLastEcn(bomEntryChild.getTextContent().trim());
			}
			else if (bomEntryChild.getNodeName().equalsIgnoreCase("ecn-date"))
			{
				baseProduct.setEcnDate(new SimpleDateFormat("dd/MM/yy").parse(bomEntryChild.getTextContent()
					.trim(),
					new ParsePosition(0)));
			}
			else if (bomEntryChild.getNodeName().equalsIgnoreCase("obsolete"))
			{
				baseProduct.setObsolete(new Boolean(bomEntryChild.getTextContent()
					.trim()).booleanValue());
			}
			else if (bomEntryChild.getNodeName()
				.equalsIgnoreCase("unit-of-measure"))
			{
				baseProduct.setUnitOfMeasure(bomEntryChild.getTextContent()
					.trim());
			}
		}

		return baseProduct;
	}

	private BaseProduct addRawMatl(BaseProduct prd, String line) {
		BaseProduct rm = null;

		String rmProductNumber = line.substring(120, 136).trim();
		if (!"".equals(rmProductNumber))
		{
			rm = new BaseProduct();
			rm.setProductNumber(rmProductNumber);
			Quantity rmQty = null;
			try
			{
				// Old way...
				// rmQty = new Quantity(Double.parseDouble(line.substring(136,
				// 148).trim()),
				// Double.parseDouble(line.substring(148, 160).trim()));

				rmQty = new Quantity(Double.parseDouble(line.substring(148, 160)
					.trim()),
					Double.parseDouble(line.substring(136, 148).trim()));

			}
			catch (NumberFormatException nfe)
			{
				rmQty = new Quantity(0.0, 0.0);
			}
			rm.setUnitOfMeasure(prd.getUnitOfMeasure());
			prd.setUnitOfMeasure("EA");
			rm.setTypeFlag("B");
			rm.setObsolete(false);
			rm.setPlannerId("");
			rm.setDescription("");
			rm.setSecondaryDescription("");
			rm.setLastEcn("");
			prd.addChild(rm, rmQty);
		}
		return rm;
	}

	private class MyErrorHandler implements ErrorHandler {

		public void error(SAXParseException exception) throws SAXException {
			logger.error(exception, exception.fillInStackTrace());
			throw exception;
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			logger.error(exception, exception.fillInStackTrace());
			throw exception;
		}

		public void warning(SAXParseException exception) throws SAXException {
			logger.warn(exception, exception.fillInStackTrace());
			throw exception;
		}

	}

}
