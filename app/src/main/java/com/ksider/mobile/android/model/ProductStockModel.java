package com.ksider.mobile.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.ksider.mobile.android.utils.DateUtils;

/**
 * Created by yong on 8/5/15.
 */
public class ProductStockModel implements Parcelable {
    private long startTime;
    private double sellPrice;
    private double marketPrice;
    private long stockId;
    private long quantity;
    private double purchasePrice;
    private long productId;
    private String productName;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getStockId() {
        return stockId;
    }

    public void setStockId(long stockId) {
        this.stockId = stockId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setMarketPrice(double marketPrice) {
        this.marketPrice = marketPrice;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public long getStartTime() {

        return startTime;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public long getQuantity() {
        return quantity;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public static ProductStockComparator getComparator() {
        return new ProductStockComparator();
    }

    public static ProductStockComparator getComparator(int sortType) {
        return new ProductStockComparator(sortType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(startTime);
        parcel.writeDouble(sellPrice);
        parcel.writeDouble(marketPrice);
        parcel.writeLong(stockId);
        parcel.writeLong(quantity);
        parcel.writeDouble(purchasePrice);
        parcel.writeLong(productId);
        parcel.writeString(productName);
    }

    public static final Parcelable.Creator<ProductStockModel> CREATOR = new Creator<ProductStockModel>() {
        @Override
        public ProductStockModel createFromParcel(Parcel parcel) {
            ProductStockModel product = new ProductStockModel();
            product.startTime = parcel.readLong();
            product.sellPrice = parcel.readDouble();
            product.marketPrice = parcel.readDouble();
            product.stockId = parcel.readLong();
            product.quantity = parcel.readLong();
            product.purchasePrice = parcel.readDouble();
            product.productId = parcel.readLong();
            product.productName = parcel.readString();
            return product;
        }

        @Override
        public ProductStockModel[] newArray(int size) {
            return new ProductStockModel[size];
        }
    };
}

class ProductStockComparator extends BaseComparator {
    public ProductStockComparator() {
        super();
    }

    public ProductStockComparator(int sortType) {
        super(sortType);
    }

    @Override
    public int compare(Object object1, Object object2) {
        ProductStockModel stock1 = (ProductStockModel) object1;
        ProductStockModel stock2 = (ProductStockModel) object2;
        long time1 = DateUtils.getFirstMilSeconds(stock1.getStartTime());
        long time2 = DateUtils.getFirstMilSeconds(stock2.getStartTime());
        if (sortType == ASC_SORT) {
            if (time1 > time2) {
                return 1;
            } else if (time1 < time2) {
                return -1;
            } else {
//                return stock1.getSellPrice() > stock2.getSellPrice() ? 1 : (stock1.getSellPrice() == stock2.getSellPrice() ? 0 : -1);
                return 0;
            }
        } else {
            if (time1 > time2) {
                return -1;
            } else if (time1 < time2) {
                return 1;
            } else {
//                return time1 > stock2.getSellPrice() ? -1 : (stock1.getSellPrice() == stock2.getSellPrice() ? 0 : 1);
                return 0;
            }
        }
    }
}
