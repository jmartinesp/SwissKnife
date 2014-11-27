package me.champeau.wearapp

import android.os.Parcel
import android.os.Parcelable
import com.android.ast.restable.Restable
import com.android.ast.restable.RestableEntity
import com.android.ast.restable.RestableValidationBuilder
import com.arasthel.swissknife.annotations.ToJson
import groovy.transform.ToString

@RestableEntity
@ToString
@ToJson(value="simple",
includes=["name", "phone"])
class User implements Restable, Parcelable {
    String name
    String phone
    String avatar
    Integer balance

    static {
        RestableValidationBuilder.create(User)
    }

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
