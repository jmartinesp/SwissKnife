package com.coupledays.entity

import com.android.ast.restable.RestableEntity
import groovy.transform.CompileStatic

@CompileStatic
@RestableEntity
class City {
    Long id
    String name
    Set<Apartment> apartments = new HashSet<>()
    Set<Holder> holders = new HashSet<>()

    static fromJSON = {
        fromDefaultJson { Map map ->
            new City(id: (Long) map.id, name: (String) map.name)
        }
    }

    static toJSON = {
        defaultJson {
            [id: id, name: name]
        }
        withApartments {
            Map map = defaultJson()
            map += [apartments: apartments*.defaultJson()]
        }
    }
}
