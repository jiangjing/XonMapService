package com.xonmap.domain

class Audit {
    String user
    String type
    String event
    String ip
    Date dateCreated
    String targetUser
    int status
    String message

    static constraints = {
        targetUser nullable : true
        message nullable : true
    }
}
