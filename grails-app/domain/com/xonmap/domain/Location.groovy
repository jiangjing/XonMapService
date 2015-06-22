package com.xonmap.domain

class Location {
    double latitude
    double longitude
    String text

    static belongsTo = [post : Post]

    static constraints = {
        text nullable: false, blank: false
        latitude min: -90 as double, max: 90 as double
        longitude min: -180 as double, max: 180 as double
    }

    static mapping = {
        table 'location'
    }

    def getMap(){
        def map = [:]
        map.latitude = latitude
        map.longitude = longitude
        map.text = text

        return map
    }
}
