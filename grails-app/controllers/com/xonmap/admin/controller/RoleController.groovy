package com.xonmap.admin.controller

import com.xonmap.Constants
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

        def input = request.JSON
        def name = input.name
        try {
            def role = commonService.getRole(name, messages, Constants.WARN_IF_NOT_FOUND)
            if (role) {
                result.putAll role.map
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

    def members() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def name = input.name
        try {
            def role = commonService.getRole(name, messages, Constants.WARN_IF_NOT_FOUND)
            if (role) {
                result.members = role.members*.map
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
            def roleList
            if (filter) {
                roleList = Role.findAllByNameLike("%" + filter + "%", [sort: "name"])
            } else {
                roleList = Role.findAll()
            }

            result.roles = roleList*.map
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
        def name = input.name
        try {
            if (!name) {
                messages.add message(code: "role.name.empty")
            } else {
                def role = commonService.getRole(name, messages, Constants.WARN_IF_FOUND)
                if (!role) {
                    role = new Role();
                    role.name = name

                    commonService.saveObject(role, result, messages, true);
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

    def update() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def name = input.name
        def newName = input.newName
        try {
            if (newName == null) {
                messages.add message(code: "role.nothing.to.update")
            } else {
                if (newName == "") {
                    messages.add message(code: "role.new.name.empty")
                } else {
                    def updated = false
                    def role = commonService.getRole(name, messages, Constants.WARN_IF_NOT_FOUND)
                    if (role) {
                        def existingRole = commonService.getRole(newName, messages, Constants.WARN_IF_FOUND)
                        if (!existingRole) {
                            if (name != newName) {
                                role.name = newName
                                updated = true
                            }
                        }
                    }

                    if (!messages) {
                        commonService.saveObject(role, result, messages, updated)
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

}
