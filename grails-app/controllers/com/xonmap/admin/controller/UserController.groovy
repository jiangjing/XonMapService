package com.xonmap.admin.controller

import com.xonmap.Constants
import com.xonmap.domain.User
import grails.transaction.Transactional
import org.grails.web.json.JSONObject

class UserController {
    static responseFormats = ["json"]
    static namespace = "admin"

    def commonService

    def get() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def email = input.email
        try {
            def user = commonService.getUser(email, messages, Constants.WARN_IF_NOT_FOUND)
            if (user) {
                result.putAll user.map
            }
        }
        catch (Exception e) {
            messages.add e.toString()
        }

        if (messages) {
            result.status = Constants.STATUS_FAIL
            result.messages = messages
        } else {
            result.status = Constants.STATUS_SUCCESSFUL
        }

        respond result
    }

    def search() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def filter = input.filter
        try {
            def userList
            if (filter) {
                userList = User.findAllByEmailLikeOrNicknameLike("%" + filter + "%", "%" + filter + "%", [sort: "email"])
            } else {
                userList = User.findAll()
            }

            result.users = userList*.map
        }
        catch (Exception e) {
            messages.add e.toString()
        }

        if (messages) {
            result.status = Constants.STATUS_FAIL
            result.messages = messages
        } else {
            result.status = Constants.STATUS_SUCCESSFUL
        }

        respond result
    }

    def assign() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def email = input.email
        def roleName = input.roleName
        try {
            def user = commonService.getUser(email, messages, Constants.WARN_IF_NOT_FOUND)
            if (user) {
                def updated
                if (user.role.name != roleName) {
                    def role = commonService.getRole(roleName, messages, Constants.WARN_IF_NOT_FOUND)
                    if (role) {
                        user.role = role
                        updated = true
                    }
                }

                if (!messages) {
                    commonService.saveObject(user, result, messages, updated)
                }
            }
        }
        catch (Exception e) {
            messages.add e.toString()
        }

        if (messages) {
            result.status = Constants.STATUS_FAIL
            result.messages = messages
        } else {
            result.status = Constants.STATUS_SUCCESSFUL
        }

        respond result
    }
}
