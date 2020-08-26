package com.android.uhfdemo.meite.parambean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class FileBean {
    @PrimaryKey
    @NonNull
    private String EPCID;
    private String ProductCode;
    private String ProductName;
    private String ProductModel;
    private String ProductColor;

    public String getEPCID() {
        return EPCID;
    }

    public void setEPCID(String EPCID) {
        this.EPCID = EPCID;
    }

    public String getProductCode() {
        return ProductCode;
    }

    public void setProductCode(String productCode) {
        ProductCode = productCode;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductModel() {
        return ProductModel;
    }

    public void setProductModel(String productModel) {
        ProductModel = productModel;
    }

    public String getProductColor() {
        return ProductColor;
    }

    public void setProductColor(String productColor) {
        ProductColor = productColor;
    }
}
