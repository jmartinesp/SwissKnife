package com.coupledays.entity

import com.android.ast.restable.RestableEntity
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod

@CompileStatic
@RestableEntity
class Apartment implements Serializable {
    Long id
    Integer rooms
    String address
    String lat
    String lon
    Integer price
    Holder holder
    City city

    List<String> images

    static constraints = {
        address(pattern: ~/[a-zA-Z]+/)
    }

    public static final String restUrl = 'http://test.chocolife.me:8080/springapp/'

    static restMethods = {
        apartmentList(url: "${restUrl}rest/apartment/list/all/{offset}", method: HttpMethod.GET) { Map map ->
            ((List<Map>) map.apartments).collect {
                Apartment.fromJsonWithHolder(it)
            }
        }
        apartmentById(url: "${restUrl}rest/apartment/{id}", method: HttpMethod.GET) { Map map ->
            Apartment.fromJsonWithHolder(map)
        }
        apartmentByRooms(url: "${restUrl}rest/apartment/list/byrooms/{rooms}/{offset}", method: HttpMethod.GET) { Map map ->
            ((List<Map>) map.apartments).collect { Map apartment ->
                Apartment.fromJsonWithHolder(apartment)
            }
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
            new Apartment(id: (Long) map.id,
                    address: (String) map.address,
                    rooms: (int) map.rooms,
                    lat: (String) map.lat,
                    lon: (String) map.lon,
                    images: map.images as List,
                    price: (int) map.price)
        }
        fromJsonWithHolder { Map map ->
            Apartment apartment = fromDefaultJson(map)
            apartment.setHolder((Holder) Holder.fromDefaultJson((Map) map.holder))
            apartment
        }
    }
}
