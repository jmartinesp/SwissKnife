package com.coupledays.app

import android.os.Parcel
import android.os.Parcelable
import com.android.ast.restable.RestableEntity
import com.coupledays.ast.ToJson
import groovy.transform.CompileStatic
import groovy.transform.ToString

@RestableEntity
@ToString
@ToJson(includes = ['name', 'phone'])
@CompileStatic
class User implements Parcelable {
    String name
    String phone
    String avatar
    Integer balance

    static constraints = {
        name pattern: ~/[a-zA-Z]+/, min: 3, max: 5, blank: false, nullable: false
        phone pattern: ~/\d+/, size: 2..4
        balance range: 10..100
    }

    @Override
    int describeContents() {
        return 0
    }

    @Override
    void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name)
    }

    public static User CREATOR(Parcel parcel) {
        User user = new User()
        user.name = parcel.readString()
        return user
    }
}
