package com.xonmap.admin.controller

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

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def targetEmail = input.targetEmail

            def user = validate(email, password, true, true, true, messages)
            if (user) {
                def targetUser = commonService.getUser(targetEmail, messages, commonService.WARN_IF_NOT_FOUND)
                if (targetUser) {
                    result.putAll targetUser.map
                }
            }
        }
        catch (Exception e) {
            messages.add e.toString()
        }

        if (messages) {
            result.status = commonService.STATUS_FAIL
            result.messages = messages
        } else {
            result.status = commonService.STATUS_SUCCESSFUL
        }

        respond result
    }

    def search() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def filter = input.filter

            def user = validate(email, password, true, true, true, messages)
            if (user) {
                def userList
                if (filter) {
                    userList = User.findAllByEmailLikeOrNicknameLike("%" + filter + "%", "%" + filter + "%", [sort: "email"])
                } else {
                    userList = User.findAll()
                }

                result.users = userList*.map
            }
        }
        catch (Exception e) {
            messages.add e.toString()
        }

        if (messages) {
            result.status = commonService.STATUS_FAIL
            result.messages = messages
        } else {
            result.status = commonService.STATUS_SUCCESSFUL
        }

        respond result
    }

    @Transactional
    def assign() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def targetEmail = input.targetEmail
            def roleName = input.roleName

            def user = validate(email, password, true, true, true, messages)
            if (user) {
                def targetUser = commonService.getUser(targetEmail, messages, commonService.WARN_IF_NOT_FOUND)
                if (targetUser) {
                    def updated
                    if (targetUser.role.name != roleName) {
                        def role = commonService.getRole(roleName, messages, commonService.WARN_IF_NOT_FOUND)
                        if (role) {
                            targetUser.role = role
                            updated = true
                        }
                    }

                    if(!messages){
                        saveUser(targetUser, result, messages, updated)
                    }
                }
            }
        }
        catch (Exception e) {
            messages.add e.toString()
        }

        if (messages) {
            result.status = commonService.STATUS_FAIL
            result.messages = messages
        } else {
            result.status = commonService.STATUS_SUCCESSFUL
        }

        respond result
    }

    private validate(email, password, requirePost, validateUser, requireAdmin, messages) {
        def user
        if (requirePost && !request.post) {
            messages.add message(code: "method.not.supported", args: [request.method])
        }

        if (validateUser) {
            if (requireAdmin) {
                user = commonService.validateAdmin(email, password, messages)
            } else {
                user = commonService.validateAccount(email, password, messages)
            }
        }

        return user
    }

    private saveUser(user, result, messages, updated) {
        if (updated) {
            if (user.validate()) {
                user.save(flush : true)

                result?.putAll user.map
            } else {
                user.errors.allErrors.each {
                    messages?.add message(error: it)
                }
            }
        } else {
            result?.putAll user.map
        }
    }
}
