package com.xonmap.admin.controller

import com.xonmap.domain.Role
import org.grails.web.json.JSONObject
import grails.transaction.Transactional

class RoleController {
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
            def name = input.name

            def user = validate(email, password, true, true, true, messages)
            if (user) {
                def role = commonService.getRole(name, messages, commonService.WARN_IF_NOT_FOUND)
                if (role) {
                    result.putAll role.map
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

    def members() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def name = input.name

            def user = validate(email, password, true, true, true, messages)
            if (user) {
                def role = commonService.getRole(name, messages, commonService.WARN_IF_NOT_FOUND)
                if (role) {
                    result.members = role.members*.map
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
                def roleList
                if (filter) {
                    roleList = Role.findAllByNameLike("%" + filter + "%", [sort: "name"])
                } else {
                    roleList = Role.findAll()
                }

                result.roles = roleList*.map
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
            def name = input.name

            def user = validate(email, password, true, true, true, messages)
            if (user) {
                if (!name) {
                    messages.add message(code: "role.name.empty")
                }

                if (!messages) {
                    def role = commonService.getRole(name, messages, commonService.WARN_IF_FOUND)
                    if (!role) {
                        role = new Role();
                        role.name = name

                        saveRole(role, result, messages, true);
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
                def name = input.name
                def newName = input.newName

                def user = validate(email, password, true, true, true, messages)
                if (user) {
                    if (newName == null) {
                        messages.add message(code: "role.nothing.to.update")
                    } else {
                        if (newName == "") {
                            messages.add message(code: "role.new.name.empty")
                        }
                        else{
                            def updated = false
                            def role = commonService.getRole(name, messages, commonService.WARN_IF_NOT_FOUND)
                            if (name != newName) {
                                def existingRole = commonService.getRole(newName, messages, commonService.WARN_IF_FOUND)
                                if (!existingRole) {
                                    role.name = newName
                                    updated = true
                                }
                            }

                            if (!messages) {
                                saveRole(role, result, messages, updated)
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

    private saveRole(role, result, messages, updated) {
        if (updated) {
            if (role.validate()) {
                role.save(flush : true)

                result?.putAll role.map
            } else {
                role.errors.allErrors.each {
                    messages?.add message(error: it)
                }
            }
        } else {
            result?.putAll role.map
        }
    }
}
