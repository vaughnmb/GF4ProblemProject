package com.iecokc.bom.model.bean.impl;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import com.iecokc.bom.model.bean.AssemblyContext;
import com.iecokc.bom.model.bean.Product;
import com.iecokc.bom.model.bean.Quantity;

public class BaseProduct extends BaseMessageTarget implements Product, Serializable {
	private static final long serialVersionUID = -4513967738142948657L;
	
	public List<AssemblyLink> assemblyLinks = new LinkedList<AssemblyLink>();
	private String productNumber;
	private String description;
	private String secondaryDescription;
	private String unitOfMeasure;
	private String plannerId;
	private String typeFlag;
	private boolean obsolete;
	private String lastEcn;
	private Date ecnDate;
	private BigDecimal cost;
	private BigDecimal listPrice;
	
	// Messages set on assembly contexts are lost if we don't cache the tree-walking
	private transient WeakReference<BaseAssemblyContext> topLevelContext = null;
	private transient WeakHashMap<BaseAssemblyContext, List<BaseAssemblyContext>> kidCache = null;

	public int compareTo(Product p) {
		return this.productNumber != null && p.getProductNumber() != null ?
				this.productNumber.compareTo(p.getProductNumber()) : 0;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((productNumber == null) ? 0 : productNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final BaseProduct other = (BaseProduct) obj;
		if (productNumber == null) {
			if (other.productNumber != null)
				return false;
		} else if (!productNumber.equals(other.productNumber))
			return false;
		return true;
	}
	
	
        
        public String toString() {
            return "Product {productNumber: " + productNumber +
                ", description: " + description +
                ", secondaryDescription: " + secondaryDescription +
                ", unitOfMeasure: " + unitOfMeasure +
                ", plannerId: " + plannerId +
                ", typeFlag: " + typeFlag +
                ", obsolete: " + obsolete +
                ", lastEcn: " + lastEcn +
                ", ecnDate: " + ecnDate +
                ", cost: " + cost +
                ", listPrice: " + listPrice;
        }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getEcnDate() {
		if(ecnDate == null){
			return ecnDate;
		}
		return new Date(ecnDate.getTime());
	}

	public void setEcnDate(Date ecnDate) {
		if(ecnDate == null){
			this.ecnDate = null;
		}
		else {
			this.ecnDate = new Date(ecnDate.getTime());
		}
	}

	public String getLastEcn() {
		return lastEcn;
	}

	public void setLastEcn(String lastEcn) {
		this.lastEcn = lastEcn;
	}

	public boolean getObsolete() {
		return obsolete;
	}

	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	public String getPlannerId() {
		return plannerId;
	}

	public void setPlannerId(String plannerId) {
		this.plannerId = plannerId;
	}

	public String getProductNumber() {
		return productNumber;
	}

	public void setProductNumber(String productNumber) {
		this.productNumber = productNumber;
	}

	public String getSecondaryDescription() {
		return secondaryDescription;
	}

	public void setSecondaryDescription(String secondaryDescription) {
		this.secondaryDescription = secondaryDescription;
	}

	public String getTypeFlag() {
		return typeFlag;
	}

	public void setTypeFlag(String typeFlag) {
		this.typeFlag = typeFlag;
	}

	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}
	
	public Collection<AssemblyLink> getAssemblyLinks() {
		return assemblyLinks;
	}
	
	public boolean addChild(BaseProduct child, Quantity qty) {
		return getAssemblyLinks().add(new AssemblyLink(child, qty));
	}
	
	public List<AssemblyContext> getChildren() {
		return castToInterface(getChildren(getTopLevelContext()));
	}
	
	private BaseAssemblyContext getTopLevelContext() {
		if (this.topLevelContext == null) {
			this.topLevelContext = new WeakReference<BaseAssemblyContext>(null);
		}
		BaseAssemblyContext ctx = this.topLevelContext.get();
		if (ctx == null) {
			ctx = new BaseAssemblyContext(this);
		}
		return ctx;
	}
	
	private WeakHashMap<BaseAssemblyContext, List<BaseAssemblyContext>> getKidCache() {
		if (kidCache == null) {
			kidCache = new WeakHashMap<BaseAssemblyContext, List<BaseAssemblyContext>>();
		}
		return kidCache;
	}
	
	private List<BaseAssemblyContext> getChildren(BaseAssemblyContext parentCtx) {
		BaseProduct parent = (BaseProduct)parentCtx.getProduct();
		if (parent == null) {
			StringBuilder sb = new StringBuilder();
			for (AssemblyContext ac : parentCtx.getPath()) {
				try {
					sb.append(ac.getProduct().getProductNumber());
					sb.append(":");
				}
				catch (Exception e) {
					
				}
			}
			throw new RuntimeException("Null parent context: " + sb.toString());
		}
		List<BaseAssemblyContext> kids = null;
		
		// Check the cache first
		if (getKidCache().containsKey(parentCtx)) {
			kids = getKidCache().get(parentCtx);
		}
		
		// Make a new one
		if (kids == null) {
			kids = new LinkedList<BaseAssemblyContext>();
			for (AssemblyLink link : parent.getAssemblyLinks()) {
				kids.add(new BaseAssemblyContext(link.getChild(), parentCtx, link.getQty()));
			}
			Collections.sort(kids);
			getKidCache().put(parentCtx, kids);
		}
		
		return kids;
	}
	
	public List<AssemblyContext> getDescendants() {
		return castToInterface(getDescendants(getTopLevelContext()));
	}
	
	private List<BaseAssemblyContext> getDescendants(BaseAssemblyContext parentCtx) {
		List<BaseAssemblyContext> descendants = new LinkedList<BaseAssemblyContext>();
		for (BaseAssemblyContext ctx : getChildren(parentCtx)) {
			descendants.add(ctx);
			descendants.addAll(getDescendants(ctx));
		}
		return descendants;
	}
	
	public List<AssemblyContext> getFullAssembly() {
		BaseAssemblyContext topLevel = getTopLevelContext();
		List<BaseAssemblyContext> ret = getDescendants(topLevel);
		ret.add(0, topLevel);
		return castToInterface(ret);
	}
	
	private List<AssemblyContext> castToInterface(List<BaseAssemblyContext> list) {
		List<AssemblyContext> ret = new LinkedList<AssemblyContext>();
		for (BaseAssemblyContext ctx : list) {
			ret.add((AssemblyContext)ctx);
		}
		return ret;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}


	public BigDecimal getListPrice() {
		return listPrice;
	}


	public void setListPrice(BigDecimal listPrice) {
		this.listPrice = listPrice;
	}
}