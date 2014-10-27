package com.iecokc.bom.model.bean;

import java.io.Serializable;
import java.math.BigDecimal;

public class Quantity implements Serializable {
	
	private static final long serialVersionUID = 5710825826139391372L;
	private Double engQty;
	private Double reqQty;
	
	public Quantity(Double qty) {
		this.engQty = qty;
		this.reqQty = qty;
	}
	
	public Quantity(Double reqQty, Double engQty) {
		this.reqQty = reqQty;
		this.engQty = engQty;
	}
	
	// Convenience constructor for rolling up quantities
	public Quantity(Quantity init, Quantity multiplier) {
		this.reqQty = new Double(init.getReqQty() * multiplier.getReqQty());
		this.engQty = new Double(init.getEngQty() * multiplier.getEngQty());
	}
	
	public Double getEngQty() {
		return this.engQty;
	}
	
	public Double getReqQty() {
		return this.reqQty;
	}
	
	/***
	 * The standard qty is really the reqQty, so enshrine this fact in the
	 * interface
	 */
	public Double getQty() {
		return this.reqQty;
	}
	
	public Quantity add(Quantity q){
		return new Quantity(this.getReqQty() + q.getReqQty(), this.getEngQty() + q.getEngQty());
	}
	/***
	 * Convenience function for display
	 */
	public String toString() {
		BigDecimal bd = new BigDecimal(this.reqQty).setScale(3, BigDecimal.ROUND_HALF_UP);
		try {
			return new Integer(bd.intValueExact()).toString();
		}
		catch (Exception e) {
			// well, just use the toPlainString, then
		}
		return bd.toPlainString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((engQty == null) ? 0 : engQty.hashCode());
		result = prime * result + ((reqQty == null) ? 0 : reqQty.hashCode());
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
		final Quantity other = (Quantity) obj;
		if (engQty == null) {
			if (other.engQty != null)
				return false;
		} else if (!engQty.equals(other.engQty))
			return false;
		if (reqQty == null) {
			if (other.reqQty != null)
				return false;
		} else if (!reqQty.equals(other.reqQty))
			return false;
		return true;
	}
	
	
}
