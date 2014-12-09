package com.coupledays.entity

import com.coupledays.ast.RestableEntity
import com.coupledays.ast.ToJson
import groovy.transform.CompileStatic

@CompileStatic
@ToJson(includes = ['id', 'name'])
@RestableEntity
class City {
    Long id
    String name
    Set<Apartment> apartments = new HashSet<>()
    Set<Holder> holders = new HashSet<>()

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
