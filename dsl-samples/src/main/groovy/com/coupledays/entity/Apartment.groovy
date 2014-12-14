package com.coupledays.entity

import com.android.ast.restable.RestableEntity
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

    static restUrl = 'http://127.0.0.1:8080/rest/'

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

    static List<Apartment> getApartmentList() {
        /*def url = "${restUrl}apartment/list"
        RestTemplate template = new RestTemplate(true)
        StringHttpMessageConverter converter = new StringHttpMessageConverter()
        converter.setSupportedMediaTypes([MediaType.ALL])
        template.getMessageConverters().add(converter)
        ResponseEntity<String> response = template.getForEntity(url, String)
        def jsonSlurper = new JsonSlurper()
        def parsed = jsonSlurper.parseText(response.getBody()) as List<Map>
        def list = parsed.collect {
            Apartment.fromDefaultJson(it)
        }
        Log.i('INFO', list.toString())
        return list*/
        null
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
