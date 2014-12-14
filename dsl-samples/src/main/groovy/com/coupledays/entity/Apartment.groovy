package com.coupledays.entity

import com.coupledays.ast.RestableEntity
import com.coupledays.ast.ToJson
import groovy.transform.CompileStatic

@CompileStatic
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
        address pattern: ~/[a-zA-Z]+/, length: 100
        lat range: 0..72
        lon range: 0..52
    }

    static restUrl = 'http://localhost:8080/'

    static fromJSON = {
        fromDefaultJson { Map map ->
            new Apartment(id: (Long) map.id, rooms: (int) map.rooms,
                    address: (String) map.address, lat: (BigDecimal) map.lat, lon: (BigDecimal) map.lon)
        }
    }

    static toJSON = {
        defaultJson {
            [id: id, rooms: rooms, address: address, lat: lat, lon: lon]
        }
        withHolderName {
            Map map = defaultJson()
            map += [holder: [name: holder.name]]
        }
        withHolder {
            Map map = defaultJson()
            map += [holder: holder.defaultJson()]
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
        lat.setScale(16)
    }

    void setLat(BigDecimal lat) {
        this.lat = lat.setScale(16)
    }

    BigDecimal getLon() {
        lon.setScale(16)
    }

    void setLon(BigDecimal lon) {
        this.lon = lon.setScale(16)
    }
}
