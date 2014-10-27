package com.iecokc.bom.model.bean.impl;


public class PrmsProduct extends BaseProduct {
    private static final long serialVersionUID = 1L;

    /***
     * Unlazify this class when serializing
     */
    /*
    private void writeObject(ObjectOutputStream oos) throws IOException {
        if (dao != null) {
            this.assemblyLinks = dao.getAssemblyLinks();
        }
        oos.defaultWriteObject();
    }
    */
}
