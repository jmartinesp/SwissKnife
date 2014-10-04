package com.dexafree.sample;


import android.os.Parcel;
import android.os.Parcelable;

public class Cosa implements Parcelable {

    private String name;
    private int age;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    int getAge() {
        return age;
    }

    void setAge(int age) {
        this.age = age;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.age);
    }

    public Cosa() {
    }

    private Cosa(Parcel in) {
        this.name = in.readString();
        this.age = in.readInt();
    }

    public static final Parcelable.Creator<Cosa> CREATOR = new Parcelable.Creator<Cosa>() {
        public Cosa createFromParcel(Parcel source) {
            return new Cosa(source);
        }

        public Cosa[] newArray(int size) {
            return new Cosa[size];
        }
    };
}
