package com.coupledays.entity

import com.coupledays.ast.RestableEntity
import com.coupledays.ast.ToJson
import groovy.transform.CompileStatic

@CompileStatic
@ToJson(includes = ['id', 'phone', 'name', 'avatar'])
@RestableEntity
class Holder {
    Long id
    String phone
    String name
    String avatar
    Set<Apartment> apartments = new HashSet<>()
    City city

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
