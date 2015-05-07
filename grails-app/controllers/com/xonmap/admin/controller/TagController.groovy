package com.xonmap.admin.controller

import com.xonmap.domain.Role
import com.xonmap.domain.Tag
import grails.transaction.Transactional
import org.grails.web.json.JSONObject

class TagController {
    static responseFormats = ["json"]
    static namespace = "admin"
    def commonService

    @Transactional
    def create() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def name = input.name;

            def user = validate(email, password, true, true, true, messages)
            if (user) {
                if (!name) {
                    messages.add message(code: "tag.name.empty")
                }

                if (!messages) {
                    def tag = commonService.getTag(name, messages, commonService.WARN_IF_FOUND)
                    if (!tag) {
                        tag = new Tag()
                        tag.name = name

                        saveTag(tag, result, messages, true);
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
                    messages.add message(code: "tag.nothing.to.update")
                } else {
                    if (newName == "") {
                        messages.add message(code: "tag.new.name.empty")
                    }
                    else{
                        def updated = false
                        def tag = commonService.getTag(name, messages, commonService.WARN_IF_NOT_FOUND)
                        if (name != newName) {
                            def existingTag = commonService.getTag(newName, messages, commonService.WARN_IF_FOUND)
                            if (!existingTag) {
                                tag.name = newName
                                updated = true
                            }
                        }

                        if (!messages) {
                            saveTag(tag, result, messages, updated)
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

    private saveTag(tag, result, messages, updated) {
        if(updated) {
            if (tag.validate()) {
                tag.save(flush : true)

                result?.putAll tag.map
            } else {
                tag.errors.allErrors.each {
                    messages?.add message(error: it)
                }
            }
        }
        else{
            result?.putAll tag.map
        }
    }
}
