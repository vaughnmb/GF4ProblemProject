package com.iecokc.bom.model.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.iecokc.bom.model.bean.Product;
import com.iecokc.bom.model.bean.Quantity;
import com.iecokc.bom.model.bean.impl.AssemblyLink;
import com.iecokc.bom.model.bean.impl.PrmsProduct;
import com.iecokc.bom.model.dao.ProductDao;
import com.iecokc.bom.model.exception.WhereIsYourGodNowException;

public abstract class DummyPrmsProductDao implements ProductDao {

	protected Logger logger = Logger.getLogger(this.getClass());
	
	@Resource(mappedName = "jdbc/PRMS_RO")
	private DataSource ds;

	protected abstract String getLibrary();

	protected int connectionCount = 0;

	public Product getProduct(String partNumber) {
		PrmsProduct p = null;
		String prodNo = productExists(partNumber).toString();
		String trimmedPartNumber = partNumber.trim();
		p = new PrmsProduct();
		p.setProductNumber(trimmedPartNumber);
		fillProduct(p);
		p.setDescription(prodNo);
		return p;
	}
	
	public Double productExists(String partNumber) {
		try (Connection con = ds.getConnection(); PreparedStatement stmt = getProductExistsStatement(con, partNumber); ResultSet rs = stmt.executeQuery();)
		{
			rs.next();
			return rs.getDouble("stdc1");
		}
		catch (SQLException e)
		{
			logger.error(e, e.fillInStackTrace());
			throw new RuntimeException(e);
		}
	}

	private PreparedStatement getProductExistsStatement(Connection con, String partNumber) throws SQLException {
		PreparedStatement statement = con.prepareStatement("select prdno, stdc1, 1.0 from " + getLibraryWithPeriod() + "mspmp100" +
				" where prdno = ? ");
		statement.setString(1, partNumber);
		return statement;
	}

	private String getLibraryWithPeriod() {
		String ret = getLibrary();
		if (ret == null)
		{
			ret = "";
		}
		if (ret.length() > 0)
		{
			ret = ret + ".";
		}
		return ret;
	}

	private class InnerProduct implements Comparable<InnerProduct>{
		private String parnt;
		private String child;
		private Integer nest;
		private String descp;
		private String dscps;
		private String ittyp;
		private String plnid;
		private String utmes;
		private BigDecimal stdc1;
		private String recec;
		private BigDecimal lstpc;
		private String obsol;
		private Double psreq;
		private Double pseng;
		public String getParnt() {
			return parnt;
		}
		public void setParnt(String parnt) {
			this.parnt = parnt;
		}
		public String getChild() {
			return child;
		}
		public void setChild(String child) {
			this.child = child;
		}
		public Integer getNest() {
			return nest;
		}
		public void setNest(Integer nest) {
			this.nest = nest;
		}
		public String getDescp() {
			return descp;
		}
		public void setDescp(String descp) {
			this.descp = descp;
		}
		public String getDscps() {
			return dscps;
		}
		public void setDscps(String dscps) {
			this.dscps = dscps;
		}
		public String getIttyp() {
			return ittyp;
		}
		public void setIttyp(String ittyp) {
			this.ittyp = ittyp;
		}
		public String getPlnid() {
			return plnid;
		}
		public void setPlnid(String plnid) {
			this.plnid = plnid;
		}
		public String getUtmes() {
			return utmes;
		}
		public void setUtmes(String utmes) {
			this.utmes = utmes;
		}
		public BigDecimal getStdc1() {
			return stdc1;
		}
		public void setStdc1(BigDecimal stdc1) {
			this.stdc1 = stdc1;
		}
		public String getRecec() {
			return recec;
		}
		public void setRecec(String recec) {
			this.recec = recec;
		}
		public BigDecimal getLstpc() {
			return lstpc;
		}
		public void setLstpc(BigDecimal lstpc) {
			this.lstpc = lstpc;
		}
		public String getObsol() {
			return obsol;
		}
		public void setObsol(String obsol) {
			this.obsol = obsol;
		}
		public Double getPsreq() {
			return psreq;
		}
		public void setPsreq(Double psreq) {
			this.psreq = psreq;
		}
		public Double getPseng() {
			return pseng;
		}
		public void setPseng(Double pseng) {
			this.pseng = pseng;
		}
		@Override
		public int compareTo(InnerProduct other) {
			return this.getNest().compareTo(other.getNest());
		}
	}

	private boolean fillProduct(PrmsProduct p) {
		List<InnerProduct> innerProducts = getInnerProducts(p.getProductNumber());
		try
		{
			Map<String, PrmsProduct> localCache = new HashMap<String, PrmsProduct>();
			Set<String> completedParents = new HashSet<String>();
			String lastParent = "";
			for (InnerProduct innerProduct : innerProducts)
			{
				String productNumber = innerProduct.getChild().trim();
				String parent = innerProduct.getParnt().trim();
				Integer nestLevel = innerProduct.getNest();
				PrmsProduct product = new PrmsProduct();
				product.setProductNumber(productNumber);
				if (0 == nestLevel) {
					product = p;
				}
				product.setDescription(innerProduct.getDescp().trim());
				product.setSecondaryDescription(innerProduct.getDscps().trim());
				product.setTypeFlag(innerProduct.getIttyp().trim());
				product.setPlannerId(innerProduct.getPlnid().trim());
				product.setCost(innerProduct.getStdc1());
				product.setObsolete("Y".equals(innerProduct.getObsol().trim()));
				product.setUnitOfMeasure(innerProduct.getUtmes().trim());
				product.setLastEcn(innerProduct.getRecec().trim());
				product.setListPrice(innerProduct.getLstpc());
				product.assemblyLinks = new LinkedList<AssemblyLink>();
				
				if (nestLevel != 0) {
					PrmsProduct parentProduct = localCache.get(parent);
					if (parentProduct == null) {
						// Eclipse told me that extracting this to a method was better than just throwing it
						unrecoverableException("Parent not found in unlikely occurance");
					}
					
					if (!lastParent.equals(parent)) {
						completedParents.add(lastParent);
					}
				
					// Link it
					if (localCache.containsKey(product.getProductNumber())) {
						product = localCache.get(product.getProductNumber());
					}
					if (!completedParents.contains(parent)) {
						Quantity q = new Quantity(innerProduct.getPsreq(), innerProduct.getPseng());
						AssemblyLink al = new AssemblyLink(product, q);
						parentProduct.assemblyLinks.add(al);
					}
					
					lastParent = parent;
				}
				
				// Locally cache it for this query
				if (!localCache.containsKey(product.getProductNumber())) {
					localCache.put(product.getProductNumber(), product);
				}

			}

		}
		catch (Exception e)
		{
			logger.error(e, e.fillInStackTrace());
			throw new RuntimeException(e);
		}
		return true;
	}

	private List<InnerProduct> getInnerProducts(String parentProduct) {
		List<InnerProduct> innerProducts = new ArrayList<InnerProduct>();
		innerProducts.add(createParentInnerProduct(parentProduct));
		for (int i = 0; i <= 275; i++){
			innerProducts.add(createInnerProduct(i, parentProduct));
		}
		Collections.sort(innerProducts);
		return innerProducts;
	}

	private InnerProduct createParentInnerProduct(String parentProduct) {
		InnerProduct innerProduct = new InnerProduct();
		innerProduct.setChild(parentProduct);
		innerProduct.setDescp("Parent");
		innerProduct.setDscps("Dscps");
		innerProduct.setIttyp("Ittyp");
		innerProduct.setLstpc(BigDecimal.valueOf(12.3456));
		innerProduct.setNest(0);
		innerProduct.setObsol("N");
		innerProduct.setParnt("None");
		innerProduct.setPlnid("Plnid");
		innerProduct.setPseng(Double.valueOf(1));
		innerProduct.setPsreq(Double.valueOf(1));
		innerProduct.setRecec("Recec");
		innerProduct.setStdc1(BigDecimal.valueOf(1.2345));
		innerProduct.setUtmes("Utmes");
		return innerProduct;
	}

	private InnerProduct createInnerProduct(int nestLevel, String parentProduct) {
		Integer nest = (nestLevel % 7) + 1;
		InnerProduct innerProduct = new InnerProduct();
		innerProduct.setChild("Child" + nestLevel);
		innerProduct.setDescp("Descp" + nestLevel);
		innerProduct.setDscps("Dscps" + nestLevel);
		innerProduct.setIttyp("Ittyp" + nestLevel);
		innerProduct.setLstpc(BigDecimal.valueOf(12.3456));
		innerProduct.setNest(nest);
		innerProduct.setObsol("N");
		if (nest > 1){
			innerProduct.setParnt("Child" + (nestLevel - 1));
		} else {
			innerProduct.setParnt(parentProduct);
		}
		innerProduct.setPlnid("Plnid" + nestLevel);
		innerProduct.setPseng(Double.valueOf((nestLevel % 5) + 1));
		innerProduct.setPsreq(Double.valueOf((nestLevel % 5) + 1));
		innerProduct.setRecec("Recec" + nestLevel);
		innerProduct.setStdc1(BigDecimal.valueOf(1.2345));
		innerProduct.setUtmes("Utmes" + nestLevel);
		return innerProduct;
	}

	private void unrecoverableException(String msg) {
		throw new WhereIsYourGodNowException(msg);
	}

}
