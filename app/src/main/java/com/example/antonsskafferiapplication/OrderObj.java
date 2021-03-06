package com.example.antonsskafferiapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderObj implements Parcelable {
    /*Create an object to contain food name and quantity.
    This object is sent between activities and thus needs to implement Parcelable
    */
    private String foodName;
    private int quantity;
    private boolean isLunch;

    public OrderObj(String name, int q, boolean lunchStatus){
        foodName = name;
        quantity = q;
        isLunch = lunchStatus;
    }

    protected OrderObj(Parcel in) {
        foodName = in.readString();
        quantity = in.readInt();
    }

    public static final Creator<OrderObj> CREATOR = new Creator<OrderObj>() {
        @Override
        public OrderObj createFromParcel(Parcel in) {
            return new OrderObj(in);
        }

        @Override
        public OrderObj[] newArray(int size) {
            return new OrderObj[size];
        }
    };

    public String getFoodName(){
        return foodName;
    }

    public int getQuantity(){
        return quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(foodName);
        dest.writeInt(quantity);
    }

    public boolean getIsLunch(){
        return isLunch;
    }

}
