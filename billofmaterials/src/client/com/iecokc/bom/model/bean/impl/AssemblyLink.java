package com.iecokc.bom.model.bean.impl;

import java.io.Serializable;

import com.iecokc.bom.model.bean.Quantity;

public class AssemblyLink implements Serializable, Comparable<AssemblyLink> {
	private static final long serialVersionUID = 1L;
	
	private BaseProduct child;
	private Quantity qty;
	
	public AssemblyLink(BaseProduct child, Quantity qty) {
		this.child = child;
		this.qty = qty;
	}
	public BaseProduct getChild() {
		return child;
	}
	public Quantity getQty() {
		return qty;
	}
	public boolean equals(AssemblyLink al) {
		if (this.child == null ) {
			if (al.child == null) {
				return true;
			}
			return false;
		}
		if (al.child == null) {
			return false;
		}
		if (this.child.equals(al.child)) {
			return true;
		}
		return false;
	}
	public int compareTo(AssemblyLink al) {
		if (this.child == null ) {
			if (al.child == null) {
				return 0;
			}
			return -1;
		}
		if (al.child == null) {
			return 1;
		}
		return this.child.compareTo(al.child);
	}
}
