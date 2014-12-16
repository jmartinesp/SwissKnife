package com.coupledays.entity

import com.android.ast.restable.RestableEntity
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod

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

    List<String> images

    static constraints = {
        address pattern: ~/[a-zA-Z]+/, min: 3, max: 5
        lat range: 0..72
        lon range: 0..52
    }

    public static final String restUrl = 'http://37.99.55.14:9000/'

    static restMethods = {
        apartmentList(url: "${restUrl}rest/apartment/list", method: HttpMethod.GET, type: List) {
            Apartment.fromDefaultJson((Map) it)
        }
        apartmentById(url: "${restUrl}rest/apartment/{id}", method: HttpMethod.GET) {
            Apartment.fromJsonWithHolder((Map) it)
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

    static fromJSON = {
        fromDefaultJson { Map map ->
            new Apartment(id: (Long) map.id, address: (String) map.address, rooms: (int) map.rooms, lat: (BigDecimal) map.lat, lon: (BigDecimal) map.lon, images: map.images as List)
        }
        fromJsonWithHolder { Map map ->
            Apartment apartment = fromDefaultJson(map)
            apartment.setHolder((Holder) Holder.fromDefaultJson((Map) map.holder))
            apartment
        }
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
