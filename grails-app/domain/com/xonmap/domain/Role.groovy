package com.xonmap.domain

class Role {
    String name

    static hasMany = [members : User]

    static constraints = {
        name blank: false, unique: true
    }

    static mapping = {
        table 'role'
        members sort : "email"
        sort "name"
    }

    def getMap(){
        def map = [:]
        map.id = id
        map.name = name
        map.memberCount = members? members?.size() : 0

        return map
    }
}
