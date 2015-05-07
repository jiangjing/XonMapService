package com.xonmap.domain

class Post {
    transient commonService
    String title
    String text
    Date dateCreated
    Date startDate
    Date endDate
    double latitude
    double longitude
    Tag tag

    static hasMany = [likers : User, comments : Comment, images : Image]
    static belongsTo = [author: User]

    static constraints = {
        title nullable: false, blank: false
        text nullable: false, blank: false
        startDate nullable: true
        endDate nullable : true
    }

    static mapping = {
        table 'post'
        likers joinTable : [ name : "post_liker", key : "post_id", column: "liker_id"]
        comments cascade: "all-delete-orphan", sort : "id", order : "desc"
        images cascade: "all-delete-orphan", sort : "id"
        sort id : "desc"
        batchSize 100
    }

    def beforeInsert() {
        if(!startDate){
            startDate = new Date()
        }

        if(!endDate){
            endDate = new Date()
        }
    }

    def getMap(){
        def map = [:]
        map.id = id
        map.title = title
        map.text = text
        map.dateCreated = commonService.dateTimeToString(dateCreated)
        map.startDate = commonService.dateToString(dateCreated)
        map.endDate = commonService.dateToString(dateCreated)
        map.latitude = latitude
        map.longitude = longitude
        map.tagName = tag.name
        map.authorEmail = author.email
        map.images = images? images*.map : []
        map.likers = likers? likers*.email : []
        map.comments = comments? comments*.map : []

        return map
    }

    def getMapI(){
        def map = [:]
        map.id = id
        map.title = title
        map.tagName = tag.name
        map.authorEmail = author.email
        map.images = images? images*.map : []

        return map
    }
}
