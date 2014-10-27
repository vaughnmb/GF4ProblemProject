package com.iecokc.bom.model.util;

import com.iecokc.bom.model.bean.Product;

public abstract class ProductFilter {
	public abstract boolean filter(Product p);
}
