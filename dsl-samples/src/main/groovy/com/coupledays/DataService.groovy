package com.coupledays

import com.coupledays.entity.Apartment
import com.coupledays.entity.City
import com.coupledays.entity.Holder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
class DataService {
    List<City> cities = new ArrayList<>()
    List<Holder> holders = new ArrayList<>()
    List<Apartment> apartments = new ArrayList<>()

    DataService() {
        def jsonSlurper = new JsonSlurper()
        def holdersJson = """
            [{"id":1, "name":"Homer Simpson","phone":"77071334508"},
            {"id":2, "name":"Liza Simpson","phone":"77071334509"},
            {"id":3, "name":"Somebody Else","phone":"7707334455"}]
        """
        def citiesJson = """
        [{"id":1, "name":"Almaty"},{"id":2, "name":"Astana"}]
        """
        def apartmentsJson = """
            [{"id":1, "lat":71.10, "lon":58.14,"rooms":3, "address":"Some street 21 ap 2"},
            {"id":2, "lat":71.10, "lon":58.14,"rooms":2, "address":"Some street 33 ap 3"},
            {"id":3, "lat":71.10, "lon":58.14,"rooms":1, "address":"Some street 43 ap 4"},
            {"id":4, "lat":71.10, "lon":58.14,"rooms":1, "address":"Some street 1 ap 1"},
            {"id":5, "lat":71.10, "lon":58.14,"rooms":4, "address":"Some street 56 ap 5"}]
        """
        def cities = jsonSlurper.parseText(citiesJson) as List
        cities.each {
            this.cities.add(City.fromDefaultJson(it as Map))
        }
        def holders = jsonSlurper.parseText(holdersJson) as List
        holders.each {
            Holder holder = Holder.fromDefaultJson(it as Map)
            holder.setCity(this.cities[0])
            this.holders.add(holder)
        }
        def apartments = jsonSlurper.parseText(apartmentsJson) as List
        apartments.each {
            Apartment apartment = Apartment.fromDefaultJson(it as Map)
            apartment.setCity(this.cities[0])
            this.apartments.add(apartment)
        }
        List<List<Apartment>> collatedApartments = this.apartments.collate(2)
        this.holders.eachWithIndex { holder, idx ->
            def aps = collatedApartments.get(idx).toSet()
            aps.each { it.holder = holder }
            holder.apartments = aps
        }
        this.cities[0].apartments = this.apartments.toSet()
        println "Cities default jsons: ${this.cities*.defaultJson()}"
        println "Cities with apartments: ${this.cities*.withApartments()}"
        println "Holders with Apartments: ${this.holders*.withApartments()}"
        println "Apartments with holder: ${this.apartments*.withHolder()}"
        println "Apartments with holder name: ${this.apartments*.withHolderName()}"
    }

    static def main(args) {
        DataService service = new DataService()
    }
}
