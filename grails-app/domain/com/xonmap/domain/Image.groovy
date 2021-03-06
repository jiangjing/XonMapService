package com.xonmap.domain

class Image {
    String imageUrl
    String thumbnailUrl

    static belongsTo = [post : Post]

    static constraints = {
        imageUrl blank: false, nullable: false
        thumbnailUrl blank: false, nullable: false
    }

    static mapping = {
        table "image"
    }

    def getMap(){
        def map = [:]
        map.id = id
        map.imageUrl = imageUrl

        return map
    }
}
