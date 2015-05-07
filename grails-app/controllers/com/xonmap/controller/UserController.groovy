package com.xonmap.controller

import com.xonmap.domain.User
import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.Transactional
import org.grails.web.json.JSONObject

class UserController {
    static responseFormats = ["json"]
    def commonService

    def login() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password

            def user = validate(email, password, true, true, false, messages)
            if (user) {
                result.putAll user.map

                try {
                    user.lastLoginDate = new Date()
                    user.save()
                }
                catch (Exception e) {
                    log.error("Failed to update last login date.", e)
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

    def get() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email

            validate(null, null, true, false, false, messages)
            if (!messages) {
                def user = commonService.getUser(email, messages, commonService.WARN_IF_NOT_FOUND)
                if (user) {
                    result.putAll user.mapI
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
            def filter = input.filter

            validate(null, null, true, false, false, messages)
            if (!messages) {
                def userList
                if (filter) {
                    userList = User.findAllByEmailLikeOrNicknameLike("%" + filter + "%", "%" + filter + "%", [sort: "email"])
                } else {
                    userList = User.findAll()
                }

                result.users = userList*.mapI
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

    def followers() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email

            validate(null, null, true, false, false, messages)
            if (!messages) {
                if (!email) {
                    messages.add message(code: "user.email.empty")
                } else {
                    def user = commonService.getUser(email, messages, commonService.WARN_IF_NOT_FOUND)
                    if (user) {
                        result.followers = user.followers*.mapI
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

    def followees() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email

            validate(null, null, true, false, false, messages)
            if (!messages) {
                if (!email) {
                    messages.add message(code: "user.email.empty")
                } else {
                    def user = commonService.getUser(email, messages, commonService.WARN_IF_NOT_FOUND)
                    if (user) {
                        result.followees = user.followees*.mapI
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

    @Transactional
    def create() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def nickname = input.nickname
            def userSource = input.userSource

            validate(null, null, true, false, false, messages)
            if (!messages) {
                if (!email) {
                    messages.add message(code: "user.email.empty")
                }

                if (!password) {
                    messages.add message(code: "user.password.empty")
                }

                if (!nickname) {
                    messages.add message(code: "user.nickname.empty")
                }

                if (!userSource) {
                    userSource = commonService.USER_SOURCE_INLINE
                } else {
                    if (userSource != commonService.USER_SOURCE_INLINE && userSource != commonService.USER_SOURCE_FACEBOOK) {
                        messages.add message(code: "user.source.invalid", args: [userSource])
                    }
                }

                if (!messages) {
                    def user = commonService.getUser(email, messages, commonService.WARN_IF_FOUND);
                    if (!user) {
                        def role = commonService.getRole(commonService.ROLE_USER, messages, commonService.WARN_IF_NOT_FOUND)
                        if (role) {
                            user = new User();
                            user.email = email;
                            user.password = password;
                            user.nickname = nickname;
                            user.userSource = userSource
                            user.role = role

                            saveUser(user, result, messages, true);
                        }
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

    @Transactional
    def update() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def newPassword = input.newPassword
            def newNickname = input.newNickname

            def user = validate(email, password, true, true, false, messages)
            if (user) {
                if (newPassword == null && newNickname == null) {
                    messages.add message(code: "user.nothing.to.update")
                } else {
                    if (newPassword != null && newPassword == "") {
                        messages.add message(code: "user.new.password.empty")
                    }

                    if (newNickname != null && newNickname == "") {
                        messages.add message(code: "user.new.nickname.empty")
                    }

                    if (!messages) {
                        def updated = false
                        if (newPassword) {
                            if (user.password != commonService.encodePassword(newPassword)) {
                                user.password = newPassword
                                updated = true
                            }
                        }

                        if (newNickname) {
                            if (user.nickname != newNickname) {
                                user.nickname = newNickname
                                updated = true
                            }
                        }

                        saveUser(user, result, messages, updated)
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

    def following() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def followeeEmail = input.followeeEmail

            validate(null, null, true, false, false, messages)
            if (!messages) {
                def user = commonService.getUser(email, messages, commonService.WARN_IF_NOT_FOUND)
                if (user) {
                    def followee = commonService.getUser(followeeEmail, messages, commonService.WARN_IF_NOT_FOUND)
                    if (followee) {
                        if (!user.followees?.contains(followee)) {
                            result.following = false
                        } else {
                            result.following = true
                        }
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

    @Transactional
    def follow() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def followeeEmail = input.followeeEmail

            def user = validate(email, password, true, true, false, messages)
            if (user) {
                def followee = commonService.getUser(followeeEmail, messages, commonService.WARN_IF_NOT_FOUND)
                if (followee) {
                    if (!user.followees?.contains(followee)) {
                        if (!followee.followers?.contains(user)) {
                            followee.addToFollowers(user)
                            saveUser(followee, null, messages, true)
                        }

                        user.addToFollowees(followee)
                        saveUser(user, result, messages, true)
                    } else {
                        messages.add message(code: "user.followed", args: [followeeEmail])
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

    @Transactional
    def unfollow() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def followeeEmail = input.followeeEmail

            def user = validate(email, password, true, true, false, messages)
            if (user) {
                def followee = commonService.getUser(followeeEmail, messages, commonService.WARN_IF_NOT_FOUND)
                if (followee) {
                    if (user.followees?.contains(followee)) {
                        if (followee.followers?.contains(user)) {
                            followee.removeFromFollowers(user)
                            saveUser(followee, null, messages, true)
                        }

                        user.removeFromFollowees(followee)
                        saveUser(user, result, messages, true)
                    } else {
                        messages.add message(code: "user.not.followed", args: [followeeEmail])
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
