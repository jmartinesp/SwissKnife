package com.coupledays.entity

import com.coupledays.ast.RestableEntity
import com.coupledays.ast.ToJson
import groovy.transform.CompileStatic

@CompileStatic
@ToJson(includes = ['id', 'rooms', 'address', 'lat', 'lon', 'images'], excludes = ['errors'])
@RestableEntity
class Apartment {
    Long id
    Integer rooms
    String address
    BigDecimal lat
    BigDecimal lon
    Holder holder
    City city

    static constraints = {
        address pattern: ~/[a-zA-Z]+/, min: 3, max: 5
        lat range: 0..72
        lon range: 0..52
    }

    static toJSON = {
        defaultJson {
            [id: id, rooms: rooms, address: address, lat: lat, lon: lon]
        }
        withHolderName {
            Map map = defaultJson()
            map += ['holder': [name: holder.name]]
        }
        withHolder {
            Map map = defaultJson()
            map += ['holder': holder.defaultJson()]
        }
    }

    List getImages() {
        def dir = new File("img/apartments")
        def images = []
        dir.listFiles().each { File file ->
            if (file.name.startsWith("${id}_")) {
                images << file.canonicalFile.name
            }
        }
        images
    }

    BigDecimal getLat() {
        return lat.setScale(16)
    }

    void setLat(BigDecimal lat) {
        this.lat = lat.setScale(16)
    }

    BigDecimal getLon() {
        return lon.setScale(16)
    }

    void setLon(BigDecimal lon) {
        this.lon = lon.setScale(16)
    }
}
