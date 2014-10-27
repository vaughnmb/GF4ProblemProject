package com.iecokc.bom.model.bean.impl;

import java.util.LinkedList;
import java.util.List;

import com.iecokc.bom.model.bean.AssemblyContext;
import com.iecokc.bom.model.bean.Product;
import com.iecokc.bom.model.bean.Quantity;

public class BaseAssemblyContext extends BaseMessageTarget implements AssemblyContext, Comparable<BaseAssemblyContext> {
	private static final long serialVersionUID = 1L;
	private Product product;
	private Quantity qty;
	private BaseAssemblyContext parent;
	
	public BaseAssemblyContext(Product product) {
		this.product = product;
		this.qty = new Quantity(1.0);
		this.parent = null;
	}
	
	public BaseAssemblyContext(Product product, BaseAssemblyContext parent, Quantity qty) {
		this.product = product;
		this.qty = qty;
		this.parent = parent;
	}
	

	public int getLevel() {
		return parent != null ? parent.getLevel() + 1 : 0;
	}

	public AssemblyContext getParent() {
		return this.parent;
	}
	
	public List<AssemblyContext> getChildren() {
		List<AssemblyContext> children = new LinkedList<AssemblyContext>();
		for (AssemblyContext child : this.product.getChildren()) {
			children.add(new BaseAssemblyContext(child.getProduct(), this, child.getQuantity()));
		}
		return children;
	}

	public List<AssemblyContext> getPath() {
		List<AssemblyContext> path = new LinkedList<AssemblyContext>();
		if (this.parent != null) {
			path.addAll(this.parent.getPath());
		}
		path.add(this);
		return path;
	}

	public Product getProduct() {
		return this.product;
	}

	public Quantity getQuantity() {
		return this.qty;
	}

	public Product getTopLevel() {
		if (this.parent == null) {
			return this.product;
		}
		return this.parent.getTopLevel();
	}

	public Quantity getTotalQuantity() {
		return parent == null ? this.qty : new Quantity(this.qty, this.parent.getQuantity());
	}
	
	public int compareTo(BaseAssemblyContext ac) {
		if (this.parent == null && ac.parent != null) {
			return -1;
		}
		if (ac.parent == null && this.parent != null) {
			return 1;
		}
		int parentCompare = 0;
		if (ac.parent != null && this.parent != null) {
			parentCompare = this.parent.compareTo(ac.parent);
		}
		if (parentCompare != 0) {
			return parentCompare;
		}
		return this.product.compareTo(ac.product);
	}

	@Override
	public String toString() {
		return product.getProductNumber() + ", qty=" + qty.toString();
	}
}
