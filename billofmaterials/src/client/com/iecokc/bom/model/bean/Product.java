package com.iecokc.bom.model.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public interface Product extends Comparable<Product>, Serializable, MessageTarget {

	// Product master-type operations
	public String getProductNumber();
	public String getDescription();
	public String getSecondaryDescription();
	public String getUnitOfMeasure();
	public String getPlannerId();
	public String getTypeFlag(); // Make/Buy
	public boolean getObsolete();
	public String getLastEcn();
	public Date getEcnDate();
	
	// Structure operations
	public List<AssemblyContext> getChildren();
	public List<AssemblyContext> getDescendants();
	
	// This includes the context for the current (top-level) product
	public List<AssemblyContext> getFullAssembly();
	public BigDecimal getCost();
	public BigDecimal getListPrice();
	
	public boolean equals(Object o);
	public int hashCode();
}
