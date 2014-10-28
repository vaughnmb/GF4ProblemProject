package com.ieokc.bom.test.dao;

/*import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.ibm.as400.access.AS400JDBCConnectionPoolDataSource;
import com.iecokc.bom.model.bean.AssemblyContext;
import com.iecokc.bom.model.bean.Product;
import com.iecokc.bom.model.bean.impl.BaseAssemblyContext;
import com.iecokc.bom.model.dao.impl.EngFileProductDao;
import com.iecokc.bom.model.dao.impl.PrmsIecProductDao;
import com.iecokc.bom.model.dao.impl.PrmsProductDao;
import com.iecokc.bom.model.exception.InvalidFileException;
import com.iecokc.bom.model.util.ProductFilter;
import com.iecokc.bom.model.util.ProductSelector;
import com.iecokc.bom.model.util.ProductUtils;*/

public class BillOfMaterialTestDao {

	/*private InputStream xmlBom = null;

	private InputStream txtBom = null;
	private InputStream riserBom = null;

	private InputStream badXmlBom = null;

	private EngFileProductDao engFileProductDao = null;

	@Before
	public void setUp() throws Exception {
		// load the xml file
		xmlBom = BillOfMaterialTestDao.class.getResourceAsStream("/BomStructure.xml");

		// load the xml file
		badXmlBom = BillOfMaterialTestDao.class.getResourceAsStream("/BadBomStructure.xml");

		// load the txt file
		txtBom = BillOfMaterialTestDao.class.getResourceAsStream("/d065-00067914.txt");
		riserBom = BillOfMaterialTestDao.class.getResourceAsStream("/c000-00163264.txt");
		// create a new dao
		engFileProductDao = new EngFileProductDao();
	}

	@After
	public void tearDown() throws Exception {
		// close files
		xmlBom.close();
		txtBom.close();
		badXmlBom.close();
	}

	@BeforeClass
	public static void classSetup() {

	}

	@AfterClass
	public static void classTearDown() throws IOException {

	}

	@Test
	public void loadXmlBomTest() {
		Assert.assertNotNull("xml BOM file shouldn't be null", xmlBom);
	}

	@Test
	public void loadTxtBomTest() {
		Assert.assertNotNull("txt BOM file shouldn't be null", txtBom);
	}

	@Test
	public void loadEngFileTest() throws InvalidFileException, IOException {
		Product product = engFileProductDao.loadEngFile(txtBom);
		Assert.assertNotNull("Product should not be null", product);
	}

	@Test
	public void loadBomFileTest() throws ParserConfigurationException, SAXException, IOException {
		Product product = engFileProductDao.loadBomFile(xmlBom);
		Assert.assertNotNull("Product should not be null", product);
	}

	@Test
	public void loadInvalidXmlBomTest() throws ParserConfigurationException, IOException {
		try
		{
			engFileProductDao.loadBomFile(badXmlBom);
			Assert.fail("Should've failed against DTD validation");
		}
		catch (SAXException exception)
		{

		}
	}

	@Test
	public void compareBomFiles() throws InvalidFileException, IOException,
			ParserConfigurationException, SAXException {
		Product txtProduct = engFileProductDao.loadEngFile(txtBom);
		Product xmlProduct = engFileProductDao.loadBomFile(xmlBom);
		Assert.assertTrue("Top-level products should be equal", txtProduct.equals(xmlProduct));
		List<AssemblyContext> txtList = txtProduct.getChildren();
		List<AssemblyContext> xmlList = xmlProduct.getChildren();

		Assert.assertTrue("The lists should be the same size", txtList.size() == xmlList.size());
		for (int i = 0; i < txtList.size(); i++)
		{
			AssemblyContext acTxt = txtList.get(i);
			AssemblyContext acXml = xmlList.get(i);
			BaseAssemblyContext bacTxt = null;
			BaseAssemblyContext bacXml = null;
			if (acTxt instanceof BaseAssemblyContext)
			{
				bacTxt = (BaseAssemblyContext) acTxt;

			}
			if (acXml instanceof BaseAssemblyContext)
			{
				bacXml = (BaseAssemblyContext) acXml;
			}

			Assert.assertTrue("Assembly Contexts should be equal.", bacTxt.compareTo(bacXml) == 0);
		}
	}

	@Test
	public void getProductTest() {
		PrmsProductDao dao = new PrmsIecProductDao();		
		dao.setDataSource(getDataSource());
		Long start = System.currentTimeMillis();
		Product product = dao.getProduct("CXB0400086084");
		Long end = System.currentTimeMillis();
		System.out.println("Just get a product: " + (end - start));
		Assert.assertNotNull(product);		
	}
	
	@Test
	public void getLeafTest() {
		PrmsProductDao dao = new PrmsIecProductDao();		
		dao.setDataSource(getDataSource());
		Long start = System.currentTimeMillis();
		Product product = dao.getProduct("R010-70001009");
		Long end = System.currentTimeMillis();
		System.out.println("Just get a leaf: " + (end - start));
		Assert.assertNotNull(product);		
	}
	
	@Test
	public void productExistsTest() {
		PrmsProductDao dao = new PrmsIecProductDao();		
		dao.setDataSource(getDataSource());
		Long start = System.currentTimeMillis();
		boolean product = dao.productExists("CXB0400086084");
		Long end = System.currentTimeMillis();
		System.out.println("Product exists: " + (end - start));
		Assert.assertNotNull(product);		
	}
	
	@Test
	public void showAllSubProductsTest() {
		PrmsProductDao dao = new PrmsIecProductDao();		
		dao.setDataSource(getDataSource());
		Product product = dao.getProduct("CXB0400086084");		
		Assert.assertNotNull(product);
	}

	@Test
	public void handleDuplicateChildrenInTextFiles() throws InvalidFileException, IOException {
		// Duplicate products with the same parent need to both show
		handleDuplicateValves(engFileProductDao.loadEngFile(riserBom));
	}
	
	// @Test - This has been corrected in PRMS, so is no longer a valid test case
	public void handleDuplicateChildrenInPrms() {
		// Duplicate products with the same parent need to both show
		PrmsProductDao dao = new PrmsIecProductDao();		
		dao.setDataSource(getDataSource());
		handleDuplicateValves(dao.getProduct("C000-00163264"));
	}
	
	@Test
	public void itpr1508() throws XPathExpressionException {
		PrmsProductDao dao = new PrmsIecProductDao();
		dao.setDataSource(getDataSource());
		Product mgy1630007952 = dao.getProduct("MGY1630007952");
		// Count the number of times A060-71141501 occurs
		Assert.assertEquals(2, ProductUtils.findAll(mgy1630007952, new ProductFilter() {
			public boolean filter(Product p) {
				return "A060-71141501".equals(p.getProductNumber());
			}
		}).size());
		// Count the number of times  A060-71141701 occurs
		// this is incorrectly doubled to 4 in Gopher
		Assert.assertEquals(2, ProductUtils.findAll(mgy1630007952, new ProductFilter() {
			public boolean filter(Product p) {
				return "A060-71141701".equals(p.getProductNumber());
			}
		}).size());
		// Another way of checking the same thing:
		Assert.assertEquals(2,
				new ProductSelector(mgy1630007952)
					.select("//a060[@product='A060-71141701']").size());
	}
	
	@Test
	public void productSelectorTest() throws XPathExpressionException {
		Long start = System.currentTimeMillis();
		PrmsProductDao dao = new PrmsIecProductDao();		
		dao.setDataSource(getDataSource());
		Long daoCreation = System.currentTimeMillis();
		Product product = dao.getProduct("CXB0400086084");
		Long getProduct = System.currentTimeMillis();
		ProductSelector selector = new ProductSelector(product);
		Long createSelector = System.currentTimeMillis();
		for (AssemblyContext ac : selector.select("//f800")) {
			Assert.assertEquals("f800", ac.getProduct().getPlannerId().toLowerCase());
		}
		Long end = System.currentTimeMillis();
		System.out.println("DaoCreation: " + (daoCreation - start));
		System.out.println("getProduct: " + (getProduct - daoCreation));
		System.out.println("createSelector: " + (createSelector - getProduct));
		System.out.println("xpathSelect: " + (end - createSelector));
	}
	
	@Test
	public void rawMaterialSelectorTest() throws Exception {
		String xpath = "/*[*[starts-with(@product,\"R\")] and count(*)=1]";
		PrmsProductDao dao = new PrmsIecProductDao();
		dao.setDataSource(getDataSource());
		Assert.assertEquals(0, new ProductSelector(dao.getProduct("CPY0300095121")).select(xpath).size());
		Assert.assertEquals(1, new ProductSelector(dao.getProduct("A040-90009563")).select(xpath).size());
		Assert.assertEquals(1, new ProductSelector(dao.getProduct("D090-90000071")).select(xpath).size());
		Assert.assertEquals(0, new ProductSelector(dao.getProduct("D110-90000114")).select(xpath).size());
		Assert.assertEquals(0, new ProductSelector(dao.getProduct("B000-00003226")).select(xpath).size());
	}
	
	private class ThreadedDaoWorker implements Runnable {
		private final PrmsProductDao dao;
		private final String productToSearch;
		private String potentialError = null;
		public ThreadedDaoWorker(PrmsProductDao dao, String productToSearch) {
			this.dao = dao;
			this.productToSearch = productToSearch;
		}
		@Override
		public void run() {
			try {
				Product p = dao.getProduct(productToSearch);
				Assert.assertEquals(1, new ProductSelector(p).select("//b500").size());
			}
			catch (Throwable e) {
				potentialError = e.getMessage();
			}
		}
	}
	
	@Test
	public void whyDoTheyCallThemStatelessSessionBeans() throws InterruptedException {
		final PrmsProductDao dao = new PrmsIecProductDao();
		dao.setDataSource(getDataSource());
		
		List<ThreadedDaoWorker> workers = new LinkedList<>();
		List<Thread> threads = new LinkedList<>();
		
		// a bunch of top-levels with common subassemblies
		workers.add(new ThreadedDaoWorker(dao, "MPY0430009373"));
		workers.add(new ThreadedDaoWorker(dao, "MPY0430009604"));
		workers.add(new ThreadedDaoWorker(dao, "MPY0430009376"));
		workers.add(new ThreadedDaoWorker(dao, "MPY0430009377"));
		workers.add(new ThreadedDaoWorker(dao, "MPY0430009371"));
		workers.add(new ThreadedDaoWorker(dao, "MPY0430009372"));
		
		for (ThreadedDaoWorker w : workers) {
			threads.add(new Thread(w));
		}
		for (Thread t : threads) {
			t.start();
		}
		for (Thread t : threads) {
			t.join();
		}
		for (ThreadedDaoWorker w : workers) {
			if (w.potentialError != null) throw new RuntimeException(w.potentialError);
		}
	}
	
	
	// Convenience to test if this riser shows both entries for the valve B005-70022321
	private void handleDuplicateValves(Product c000_00163264) {
		List<Product> valves = ProductUtils.findAll(c000_00163264, new ProductFilter() {
			@Override
			public boolean filter(Product p) {
				return "B005-70022321".equals(p.getProductNumber());
			}
			
		});
		Assert.assertEquals(2, valves.size());		
	}

	private DataSource getDataSource() {
		AS400JDBCConnectionPoolDataSource as400ds = new AS400JDBCConnectionPoolDataSource();
		as400ds.setServerName("xxxxxxx");
		as400ds.setDatabaseName("xxxxxxx");
		as400ds.setTimeFormat("ISO");
		as400ds.setDateFormat("ISO");
		as400ds.setKeepAlive(true);
		as400ds.setLazyClose(true);
		as400ds.setUser("xxxxxxx");
		as400ds.setPassword("xxxxxxxxxxxxx");
		
		return as400ds;
	}*/

}
