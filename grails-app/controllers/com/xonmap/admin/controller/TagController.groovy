package com.xonmap.admin.controller

import com.xonmap.Constants
import com.xonmap.domain.Tag
import grails.transaction.Transactional
import org.grails.web.json.JSONObject

class TagController {
    static responseFormats = ["json"]
    static namespace = "admin"
    def commonService

    def create() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def name = input.name
        def duration = input.duration
        try {
            if (!name) {
                messages.add message(code: "tag.name.empty")
            }

            if (duration == null || duration < Constants.DURATION_NA) {
                duration = Constants.DURATION_NA
            }

            if (!messages) {
                def tag = commonService.getTag(name, messages, Constants.WARN_IF_FOUND)
                if (!tag) {
                    tag = new Tag()
                    tag.name = name
                    tag.duration = duration

                    commonService.saveObject(tag, result, messages, true);
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
        def newDuration = input.newDuration
        try {
            if (newName == null && newDuration == null) {
                messages.add message(code: "tag.nothing.to.update")
            } else {
                if (newName != null && newName == "") {
                    messages.add message(code: "tag.new.name.empty")
                }

                if (newDuration != null && newDuration < Constants.DURATION_NA) {
                    newDuration = Constants.DURATION_NA
                }

                if (!messages) {
                    def updated = false
                    def tag = commonService.getTag(name, messages, Constants.WARN_IF_NOT_FOUND)
                    if (tag) {
                        def existingTag = commonService.getTag(newName, messages, Constants.WARN_IF_FOUND)
                        if (!existingTag) {
                            if (newName) {
                                if (name != newName) {
                                    tag.name = newName
                                    updated = true
                                }
                            }

                            if (newDuration) {
                                if (tag.duration != newDuration) {
                                    tag.duration = newDuration
                                    updated = true
                                }
                            }
                        }
                    }

                    if (!messages) {
                        commonService.saveObject(tag, result, messages, updated)
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
