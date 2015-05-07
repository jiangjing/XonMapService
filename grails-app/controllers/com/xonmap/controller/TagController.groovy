package com.xonmap.controller

import com.xonmap.domain.Tag
import org.grails.web.json.JSONObject
import grails.transaction.Transactional

class TagController{
    static responseFormats = ["json"]
    def commonService

    def get() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def name = input.name

            validate(null, null, true, false, false, messages)
            if(!messages) {
                def tag = commonService.getTag(name, messages, commonService.WARN_IF_NOT_FOUND)
                if (tag) {
                    result.putAll tag.map
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
            if(!messages) {
                def tagList
                if (filter) {
                    tagList = Tag.findAllByNameLike("%" + filter + "%", [sort: "name"])
                } else {
                    tagList = Tag.findAll()
                }

                result.tags = tagList*.map
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

}
