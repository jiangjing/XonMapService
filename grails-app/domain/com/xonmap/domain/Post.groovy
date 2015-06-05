package com.xonmap.domain

import groovy.time.TimeCategory

class Post {
    transient commonService
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

            use(TimeCategory){
                endDate = startDate + tag.duration.days
            }
        }
    }

    def getMap(){
        def map = [:]
        map.id = id
        map.text = text
        map.dateCreated = commonService.dateToString(dateCreated)
        map.startDate = commonService.dateToString(startDate)
        map.endDate = commonService.dateToString(endDate)
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
        map.text = text
        map.latitude = latitude
        map.longitude = longitude
        map.tagName = tag.name
        map.authorEmail = author.email
        map.images = images? images*.map : []

        return map
    }
}
