package com.iecokc.bom.model.util;

import java.util.LinkedList;
import java.util.List;

import com.iecokc.bom.model.bean.AssemblyContext;
import com.iecokc.bom.model.bean.Product;

public class ProductUtils {

	/***
	 * Utility method to find a product in an assembly tree that meets a certain criteria, as defined by the filter parameter
	 * @param p
	 * @param filter
	 * @return
	 */
	public static Product findFirst(Product p, ProductFilter filter) {
		Product found = null;
		
		if (filter.filter(p)) {
			found = p;
		}
		else {
			for (AssemblyContext ctx : p.getChildren()) {
				Product x = findFirst(ctx.getProduct(), filter);
				if (x != null) {
					found = x;
					break;
				}
			}
		}
		
		return found;
	}
	
	public static List<Product> findAll(Product p, ProductFilter filter) {
		List<Product> found = new LinkedList<Product>();
		
		if (filter.filter(p)) {
			found.add(p);
		}
		for (AssemblyContext ctx : p.getChildren()) {
			found.addAll(findAll(ctx.getProduct(), filter));
		}
		
		return found;
	}
}
