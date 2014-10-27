package com.iecokc.bom.model.dao.impl;

import javax.ejb.Stateless;

import com.iecokc.bom.model.dao.ProductDao;

@Stateless(mappedName="ejb/BillOfMaterials/iec")
public class PrmsIecProductDao extends PrmsProductDao implements ProductDao {
	@Override
	protected String getLibrary() {
		return "iec84files";
	}
		
}
