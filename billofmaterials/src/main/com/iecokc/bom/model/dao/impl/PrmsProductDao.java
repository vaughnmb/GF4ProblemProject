package com.iecokc.bom.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

public abstract class PrmsProductDao implements ProductDao {

	protected Logger logger = Logger.getLogger(this.getClass());

	@Resource(mappedName = "jdbc/PRMS_RO")
	private DataSource ds;

	private Connection conn;

	protected abstract String getLibrary();

	protected int connectionCount = 0;

	public DataSource getDataSource() {
		return ds;
	}

	protected synchronized Connection getConnection() throws SQLException {
		connectionCount++;
		if (conn == null)
		{
			conn = ds.getConnection();
		}

		return conn;
	}

	protected synchronized void closeConnection() {
		connectionCount--;
		if (connectionCount == 0)
		{
			if (conn != null)
			{
				try
				{
					conn.close();
					conn = null;
				}
				catch (SQLException e)
				{
					logger.error(e, e.fillInStackTrace());
				}
			}
		}
	}

	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}

	public Product getProduct(String partNumber) {
		String trimmedPartNumber = partNumber.trim();
		// Check to see if it already exists
		PrmsProduct p = new PrmsProduct();
		p.setProductNumber(trimmedPartNumber);
		if (!fillProduct(p)) {
			p = null;
		}
		return p;
	}
	
	public boolean productExists(String partNumber) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean foundProduct = false;
		try
		{
			con = getConnection();
			stmt = con.prepareStatement("select prdno from " + getLibraryWithPeriod() + "mspmp100" +
										" where prdno = ? ");
			stmt.setString(1, partNumber);
			rs = stmt.executeQuery();
			foundProduct = rs.next();
		}
		catch (SQLException e)
		{
			logger.error(e, e.fillInStackTrace());
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
				if (stmt != null)
				{
					stmt.close();
				}
				closeConnection();
			}
			catch (SQLException e)
			{
				// swallow
			}
		}
		return foundProduct;
	}

	/***************************************************************************
	 * Append a period "." to the library name returned from the overridden
	 * function
	 */
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

	private boolean fillProduct(PrmsProduct p) {

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean foundProduct = false;
		try
		{
			con = getConnection();
			stmt = con.prepareStatement(
					"  with usedanywhere (parnt, child, nest, descp, dscps, ittyp, plnid, " +
					"      utmes, stdc1, recec, lstpc, obsol, psreq, pseng) as ( " +
					"    select distinct '', prdno, 0, descp, dscps, ittyp, plnid, " +
					"      utmes, stdc1, recec, lstpc, obsol, 1.0, 1.0 " +
					"    from " + getLibraryWithPeriod() + "mspmp100 " +
					"    where prdno = ? " +
					"    union all " +
					"    select ua.child, ps.child, ua.nest + 1, ms.descp, ms.dscps, ms.ittyp, ms.plnid, " +
					"      ms.utmes, ms.stdc1, ms.recec, ms.lstpc, ms.obsol, ps.psreq, ps.pseng " +
					"    from usedanywhere ua, " + getLibraryWithPeriod() + "pspsp100 ps, " +
					"  " + getLibraryWithPeriod() + "mspmp100 ms " +
					"    where ps.parnt = ua.child " +
					"    and ms.prdno = ps.child " +
					"    and ps.psddt = '0001-01-01' and ps.psedt <= current date " +
					"  ) " +
					"  select * from usedanywhere ");
			stmt.setString(1, p.getProductNumber());
			rs = stmt.executeQuery();
			Map<String, PrmsProduct> localCache = new HashMap<String, PrmsProduct>();
			Set<String> completedParents = new HashSet<String>();
			String lastParent = "";
			while (rs.next())
			{
				foundProduct = true;
				String productNumber = rs.getString("CHILD").trim();
				String parent = rs.getString("PARNT").trim();
				Integer nestLevel = rs.getInt("NEST");
				PrmsProduct product = new PrmsProduct();
				product.setProductNumber(productNumber);
				if (0 == nestLevel) {
					product = p;
				}
				product.setDescription(rs.getString("DESCP").trim());
				product.setSecondaryDescription(rs.getString("DSCPS").trim());
				product.setTypeFlag(rs.getString("ITTYP").trim());
				product.setPlannerId(rs.getString("PLNID").trim());
				product.setCost(rs.getBigDecimal("STDC1"));
				product.setObsolete("Y".equals(rs.getString("OBSOL").trim()));
				product.setUnitOfMeasure(rs.getString("UTMES").trim());
				product.setLastEcn(rs.getString("RECEC").trim());
				product.setListPrice(rs.getBigDecimal("LSTPC"));
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
						Quantity q = new Quantity(rs.getDouble("PSREQ"), rs.getDouble("PSENG"));
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
		catch (SQLException e)
		{
			logger.error(e, e.fillInStackTrace());
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
				if (stmt != null)
				{
					stmt.close();
				}
				closeConnection();
			}
			catch (SQLException e)
			{
				// swallow
			}
		}
		return foundProduct;
	}

	private void unrecoverableException(String msg) {
		throw new WhereIsYourGodNowException(msg);
	}

}
