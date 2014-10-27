package com.iecokc.bom.model.exception;

public class ProductNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 2328085449272765605L;

	public ProductNotFoundException(String msg) {
		super(msg);
	}
}
