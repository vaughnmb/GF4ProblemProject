package com.iecokc.bom.model.dao;

import javax.ejb.Remote;

import com.iecokc.bom.model.bean.Product;

@Remote
public interface ProductDao {
	public Product getProduct(String partNumber);
}