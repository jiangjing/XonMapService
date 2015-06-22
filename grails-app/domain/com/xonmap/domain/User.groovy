package com.xonmap.domain

import com.xonmap.Constants

class User {
    transient commonService
    String email
    String password
    String nickname
    String token
    String externalUserId
    int userSource
    Date dateCreated
    Date lastLoginDate
    Role role

    static hasMany = [posts: Post, followees: User, followers : User]
    static mappedBy = [posts: "author"]

    static constraints = {
        email blank : false, email: true, nullable: false, unique: true
        password blank: false, nullable: false
        nickname blank: false, nullable: false, unique: true
        token nullable : true
        externalUserId nullable : true
        userSource blank: false, nullable: false
        lastLoginDate nullable: true
    }

    static mapping = {
        table "user"
        followees joinTable : [ name : "user_followee", key : "user_id", column: "followee_id"]
        followers joinTable : [ name : "user_follower", key : "user_id", column: "follower_id"]
        posts sort : "id", order : "desc"
        batchSize 100
        sort "email"
    }

    def beforeInsert() {
        password = commonService.encodeText(password)
        lastLoginDate = new Date()
    }

    def beforeUpdate() {
        if (isDirty("password")) {
            password = commonService.encodeText(password)
        }
    }

    def getMapI(){
        def map = [:]
        map.id = id
        map.email = email
        map.nickname = nickname
        map.followerCount = followers? followers?.size() : 0
        map.followeeCount = followees? followees?.size() : 0

        return map
    }

    def getMap(){
        def map = [:]
        map.id = id
        map.email = email
        map.nickname = nickname
        map.userSource = userSource
        map.roleName = role.name
        map.followerCount = followers? followers?.size() : 0
        map.followeeCount = followees? followees?.size() : 0

        return map
    }

    def getIsAdmin(){
        return role.name == Constants.ROLE_ADMIN
    }

    def getIsInlineUser(){
        return userSource == Constants.USER_SOURCE_INLINE
    }

    def getIsFacebookUser(){
        return userSource == Constants.USER_SOURCE_FACEBOOK
    }
}
