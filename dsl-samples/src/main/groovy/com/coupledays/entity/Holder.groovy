package com.coupledays.entity

import com.android.ast.restable.RestableEntity
import groovy.transform.CompileStatic

@CompileStatic
@RestableEntity
class Holder {
    Long id
    String phone
    String name
    String avatar
    Set<Apartment> apartments = new HashSet<>()
    City city

    static fromJSON = {
        fromDefaultJson { Map map ->
            def city = (City) City.fromDefaultJson((Map) map.city)
            new Holder(id: (Long) map.id, phone: (String) map.phone, name: (String) map.name, avatar: (String) map.avatar, city: city)
        }
    }

    static toJSON = {
        defaultJson {
            [id: id, phone: phone, name: name, city: city.defaultJson()]
        }
        withApartments {
            Map map = defaultJson()
            map += [apartments: apartments*.defaultJson()]
        }
    }
}
