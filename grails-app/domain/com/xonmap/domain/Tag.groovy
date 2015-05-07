package com.xonmap.domain

class Tag {
    String name

    static hasMany = [posts : Post]

    static constraints = {
        name blank: false, unique: true
    }

    static mapping = {
        table 'tag'
        posts sort : "dateCreated", order : "desc"
        sort "name"
    }

    def getMap(){
        def map = [:]
        map.id = id
        map.name = name
        map.postCount = posts? posts?.size() : 0

        return map
    }
}
