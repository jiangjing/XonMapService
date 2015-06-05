package com.xonmap.controller

import com.xonmap.Constants
import com.xonmap.domain.Tag
import org.grails.web.json.JSONObject

class TagController {
    static responseFormats = ["json"]
    static namespace = "user"
    def commonService

    def get() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def name = input.name
        try {
            def tag = commonService.getTag(name, messages, Constants.WARN_IF_NOT_FOUND)
            if (tag) {
                result.putAll tag.map
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
            def tagList
            if (filter) {
                tagList = Tag.findAllByNameLike("%" + filter + "%", [sort: "name"])
            } else {
                tagList = Tag.findAll()
            }

            result.tags = tagList*.map
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
