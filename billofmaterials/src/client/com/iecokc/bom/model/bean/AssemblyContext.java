package com.iecokc.bom.model.bean;

import java.util.List;

public interface AssemblyContext extends MessageTarget {

	public Product getProduct();
	public AssemblyContext getParent();
	public Product getTopLevel();
	public List<AssemblyContext> getChildren();
	public List<AssemblyContext> getPath();
	public int getLevel();
	public Quantity getQuantity();
	public Quantity getTotalQuantity();
}
