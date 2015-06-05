package com.xonmap.domain

class Comment {
    transient commonService
    String text
    Date dateCreated
    User user

    static belongsTo = [post : Post]

    static constraints = {
        text blank: false, nullable: false
    }

    static mapping = {
        table "comment"
        batchSize 50
    }

    def getMap(){
        def map = [:]
        map.id = id
        map.text = text
        map.dateCreated = commonService.dateToString(dateCreated)

        return map
    }
}
