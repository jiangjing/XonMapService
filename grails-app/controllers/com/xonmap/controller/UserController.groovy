package com.xonmap.controller

import com.xonmap.Constants
import com.xonmap.domain.User
import org.grails.web.json.JSONObject
import org.springframework.security.core.context.SecurityContextHolder

class UserController {
    static responseFormats = ["json"]
    static namespace = "user"
    def commonService

    def login() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def email = input.email
        def password = input.password
        def externalUserSource = input.userSource
        def externalUserId = input.userId
        def externalAccessToken = input.accessToken
        def token = input.token
        try {
            def user
            if (token) {
                user = commonService.validateAccountByToken(email, token, messages)
            } else if(password) {
                user = commonService.validateAccount(email, password, messages)
            } else{
                user = commonService.validateExternalAccount(externalUserSource, externalUserId, externalAccessToken, messages)
            }

            if (user) {
                user.lastLoginDate = new Date()
                commonService.saveObject(user, result, messages, true)

                result.token = user.token
            }
        }
        catch (Exception e) {
            result.clear()
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

    def get() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def email = input.email
        try {
            def user = commonService.getUser(email, messages, Constants.WARN_IF_NOT_FOUND)
            if (user) {
                result.putAll user.mapI
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

    def getByNickname() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def nickname = input.nickname
        try {
            def user = commonService.getUserByNickname(nickname, messages, Constants.WARN_IF_NOT_FOUND)
            if (user) {
                result.putAll user.mapI
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

            result.users = userList*.mapI
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

    def followers() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def email = input.email
        try {
            if (!email) {
                messages.add message(code: "user.email.empty")
            } else {
                def user = commonService.getUser(email, messages, Constants.WARN_IF_NOT_FOUND)
                if (user) {
                    result.followers = user.followers*.mapI
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

    def followees() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def email = input.email
        try {
            if (!email) {
                messages.add message(code: "user.email.empty")
            } else {
                def user = commonService.getUser(email, messages, Constants.WARN_IF_NOT_FOUND)
                if (user) {
                    result.followees = user.followees*.mapI
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

    def create() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def email = input.email
        def password = input.password
        def externalUserId = input.userId
        def externalAccessToken = input.accessToken
        def nickname = input.nickname
        def userSource = input.userSource
        try {
            if (!email) {
                messages.add message(code: "user.email.empty")
            }

            if (!nickname) {
                messages.add message(code: "user.nickname.empty")
            }

            if (!userSource) {
                userSource = Constants.USER_SOURCE_INLINE
            }

            if (userSource != Constants.USER_SOURCE_INLINE && userSource != Constants.USER_SOURCE_FACEBOOK) {
                messages.add message(code: "user.source.invalid", args: [userSource])
            } else {
                if (userSource == Constants.USER_SOURCE_INLINE) {
                    if (!password) {
                        messages.add message(code: "user.password.empty")
                    }
                } else {
                    if (!externalUserId) {
                        messages.add message(code: "user.external.id.empty")
                    }

                    if(!externalAccessToken){
                        messages.add message(code: "user.external.accesstoken.empty")
                    }
                }
            }

            if (!messages) {
                def user = commonService.getUser(email, messages, Constants.WARN_IF_FOUND)
                if (!user) {
                    if(userSource == Constants.USER_SOURCE_FACEBOOK){
                        commonService.validateFacebookAccount(externalUserId, externalAccessToken, messages)
                    }

                    if(!messages) {
                        if (userSource == Constants.USER_SOURCE_INLINE) {
                            user = commonService.getUserByNickname(nickname, messages, Constants.WARN_IF_FOUND)
                        } else {
                            user = commonService.getUserByNickname(nickname, messages, Constants.WARN_MUTE)
                            if (userSource == Constants.USER_SOURCE_FACEBOOK) {
                                nickname += Constants.FACEBOOK_USER_SURFIX
                            }
                        }

                        if (userSource != Constants.USER_SOURCE_INLINE || !user) {
                            def role = commonService.getRole(Constants.ROLE_USER, messages, Constants.WARN_IF_NOT_FOUND)
                            if (role) {
                                user = new User();
                                user.email = email;
                                if (!password) {
                                    password = commonService.randomPassword()
                                }
                                user.password = password
                                user.nickname = nickname;
                                user.userSource = userSource
                                def token = commonService.newToken(email)
                                user.token = token
                                if (userSource != Constants.USER_SOURCE_INLINE) {
                                    user.externalUserId = externalUserId
                                }
                                user.role = role

                                commonService.saveObject(user, result, messages, true);

                                result.token = token
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to create user", e)
            result.clear()
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

    def update() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def newPassword = input.newPassword
        def newNickname = input.newNickname
        try {
            def user = commonService.getUser(SecurityContextHolder.context.authentication.principal, messages, Constants.WARN_IF_NOT_FOUND)
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
                        if (newNickname) {
                            if (user.nickname != newNickname) {
                                def existingUser = commonService.getUserByNickname(newNickname, messages, Constants.WARN_IF_FOUND)
                                if(!existingUser) {
                                    user.nickname = newNickname
                                    updated = true
                                }
                            }
                        }

                        if(!messages) {
                            if (newPassword) {
                                if (user.password != commonService.encodeText(newPassword)) {
                                    user.password = newPassword
                                    updated = true
                                }
                            }
                        }

                        commonService.saveObject(user, result, messages, updated)
                    }
                }
            } else {
                messages.add message(code: "user.not.found", args: [SecurityContextHolder.context.authentication.principal])
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

    def following() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def email = input.email
        def followeeEmail = input.followeeEmail
        try {
            def user = commonService.getUser(email, messages, Constants.WARN_IF_NOT_FOUND)
            if (user) {
                def followee = commonService.getUser(followeeEmail, messages, Constants.WARN_IF_NOT_FOUND)
                if (followee) {
                    if (!user.followees?.contains(followee)) {
                        result.following = false
                    } else {
                        result.following = true
                    }
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

    def follow() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def followeeEmail = input.followeeEmail
        try {
            if (SecurityContextHolder.context.authentication.principal == followeeEmail) {
                messages.add message(code: "user.follow.self")
            } else {
                def user = commonService.getUser(SecurityContextHolder.context.authentication.principal, messages, Constants.WARN_IF_NOT_FOUND)
                if (user) {
                    def followee = commonService.getUser(followeeEmail, messages, Constants.WARN_IF_NOT_FOUND)
                    if (followee) {
                        commonService.follow(user, followee)
                    }
                } else {
                    messages.add message(code: "user.not.found", args: [SecurityContextHolder.context.authentication.principal])
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to follow " + followeeEmail, e)
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

    def unfollow() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def followeeEmail = input.followeeEmail
        try {
            def user = commonService.getUser(SecurityContextHolder.context.authentication.principal, messages, Constants.WARN_IF_NOT_FOUND)
            if (user) {
                def followee = commonService.getUser(followeeEmail, messages, Constants.WARN_IF_NOT_FOUND)
                if (followee) {
                    commonService.unfollow(user, followee, result, messages)
                }
            } else {
                messages.add message(code: "user.not.found", args: [SecurityContextHolder.context.authentication.principal])
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
