package com.xonmap.controller

import com.xonmap.domain.Comment
import com.xonmap.domain.Image
import com.xonmap.domain.Post
import org.grails.web.json.JSONObject
import grails.transaction.Transactional

class PostController {
    static responseFormats = ["json"]
    def commonService

    def get() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def postId = input.postId

            validate(null, null, true, false, false, messages)
            if (!messages) {
                def post = commonService.getPost(postId, messages, commonService.WARN_IF_NOT_FOUND)
                if (post) {
                    result.putAll post.map
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
            def content = input.content
            def strCreationDateFrom = input.creationDateFrom
            def strCreationDateTo = input.creationDateTo
            def strStartDateFrom = input.startDateFrom
            def strStartDateTo = input.startDateTo
            def strEndDateFrom = input.endDateFrom
            def strEndDateTo = input.endDateTo
            def distanceRange = input.distanceRange
            def latitude = input.latitude
            def longitude = input.longitude
            def tagName = input.tagName
            def authorEmail = input.authorEmail

            if(latitude != null && longitude != null && (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)){
                messages.add message(code : "post.location.invalid")
            }
            else {
                def defaultDateFrom = commonService.stringToDate("01/01/1970")
                def defaultDateTo = new Date()

                def creationDateFrom
                def creationDateTo
                if (strCreationDateFrom || strCreationDateTo) {
                    if (!strCreationDateFrom) {
                        creationDateFrom = defaultDateFrom
                    } else {
                        creationDateFrom = commonService.stringToDate(strCreationDateFrom)
                    }

                    if (!strCreationDateTo) {
                        creationDateTo = defaultDateTo
                    } else {
                        creationDateTo = commonService.stringToDateTime(strCreationDateTo)
                    }
                }

                def startDateFrom
                def startDateTo
                if (strStartDateFrom || strStartDateTo) {
                    if (!strStartDateFrom) {
                        startDateFrom = defaultDateFrom
                    } else {
                        startDateFrom = commonService.stringToDate(strStartDateFrom)
                    }

                    if (!strStartDateTo) {
                        startDateTo = defaultDateTo
                    } else {
                        startDateTo = commonService.stringToDateTime(strStartDateTo)
                    }
                }

                def endDateFrom
                def endDateTo
                if (strEndDateFrom || strEndDateTo) {
                    if (!strEndDateFrom) {
                        endDateFrom = defaultDateFrom
                    } else {
                        endDateFrom = commonService.stringToDate(strEndDateFrom)
                    }

                    if (!strEndDateTo) {
                        endDateTo = defaultDateTo
                    } else {
                        endDateTo = commonService.stringToDateTime(strEndDateTo)
                    }
                }

                def searchResult = Post.createCriteria().list {
                    if (content) {
                        or {
                            like("title", "%" + content + "%")
                            like("title", "%" + content + "%")
                        }
                    }

                    if (creationDateFrom) {
                        between("dateCreated", creationDateFrom, creationDateTo)
                    }

                    if (startDateFrom) {
                        between("startDate", startDateFrom, startDateTo)
                    }

                    if (endDateFrom) {
                        between("endDate", endDateFrom, endDateTo)
                    }

                    if(tagName){
                        tag {
                            eq("name", tagName)
                        }
                    }

                    if(authorEmail){
                        author{
                            eq("email", authorEmail)
                        }
                    }

                    order("id", "desc")
                }

                def posts = []
                searchResult.each{
                    def postLatitude = it.latitude
                    def postLongitude = it.longitude

                    if(distanceRange <= 0 || commonService.distance(latitude, longitude, postLatitude, postLongitude) <= distanceRange){
                        posts.add it
                    }
                }

                result.posts = posts*.mapI
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
            def title = input.title
            def text = input.text
            def strStartDate = input.startDate
            def strEndDate = input.endDate
            def latitude = input.latitude
            def longitude = input.longitude
            def tagName = input.tagName
            def imageUrls = input.imageUrls

            def user = validate(email, password, true, true, false, messages)
            if (user) {
                if(!title){
                    messages.add message(code : "post.title.empty")
                }

                if(!text){
                    messages.add message(code : "post.text.empty")
                }

                if(latitude == null || longitude == null || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180){
                    messages.add message(code : "post.location.invalid")
                }

                if(!messages) {
                    def tag = commonService.getTag(tagName, messages, commonService.WARN_IF_NOT_FOUND)
                    if (tag) {
                        def post = new Post()
                        post.title = title
                        post.text = text
                        if (strStartDate) {
                            post.startDate = commonService.stringToDate(strStartDate)
                        }
                        if (strEndDate) {
                            post.endDate = commonService.stringToDate(strEndDate)
                        }
                        post.latitude = latitude
                        post.longitude = longitude
                        post.tag = tag
                        post.author = user

                        if (imageUrls) {
                            imageUrls.each {
                                def image = new Image()
                                image.imageUrl = it

                                post.addToImages(image)
                            }
                        }

                        savePost(post, result, messages, true)
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
    def like() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def postId = input.postId

            def user = validate(email, password, true, true, false, messages)
            if (user) {
                def post = commonService.getPost(postId, messages, commonService.WARN_IF_NOT_FOUND)
                if (post) {
                    if (!post.likers?.contains(user)) {
                        post.addToLikers(user)

                        savePost(post, result, messages, true)
                    } else {
                        messages.add message(code: "post.liked")
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
    def dislike() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def postId = input.postId

            def user = validate(email, password, true, true, false, messages)
            if (user) {
                def post = commonService.getPost(postId, messages, commonService.WARN_IF_NOT_FOUND)
                if (post) {
                    if (post.likers?.contains(user)) {
                        post.removeFromLikers(user)

                        savePost(post, result, messages, true)
                    } else {
                        messages.add message(code: "post.disliked")
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

    def liking() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def postId = input.postId

            def user = commonService.getUser(email, messages, commonService.WARN_IF_NOT_FOUND)
            if (user) {
                def post = commonService.getPost(postId, messages, commonService.WARN_IF_NOT_FOUND)
                if (post) {
                    if (!post.likers?.contains(user)) {
                        result.liking = false
                    } else {
                        result.liking = true
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
    def comment() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def postId = input.postId
            def commentText = input.commentText

            if(!commentText){
                messages.add message(code : "comment.text.empty")
            }
            else {
                def user = validate(email, password, true, true, false, messages)
                if (user) {
                    def post = commonService.getPost(postId, messages, commonService.WARN_IF_NOT_FOUND)
                    if (post) {
                        def comment = new Comment()
                        comment.text = commentText
                        comment.user = user

                        post.addToComments(comment)

                        savePost(post, result, messages, true)
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
    def uncomment() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def commentId = input.commentId

            def user = validate(email, password, true, true, false, messages)
            if (user) {
                def comment = commonService.getComment(commentId, messages, commonService.WARN_IF_NOT_FOUND)
                if (comment) {
                    def post = comment.post
                    post.removeFromComments(comment)

                    savePost(post, result, messages, true)
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
    def delete() {
        def result = new JSONObject()
        def messages = []

        try {
            def input = request.JSON
            def email = input.email
            def password = input.password
            def postId = input.postId

            def user = validate(email, password, true, true, false, messages)
            if (user) {
                def post = commonService.getPost(postId, messages, commonService.WARN_IF_NOT_FOUND)
                if (post) {
                    if (post.author.email != email) {
                        messages.add message(code: "post.delete.not.authorized")
                    } else {
                        post.delete()
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

    private savePost(post, result, messages, updated) {
        if (updated) {
            if (post.validate()) {
                post.save(flush: true)

                result?.putAll post.map
            } else {
                post.errors.allErrors.each {
                    messages?.add message(error: it)
                }
            }
        } else {
            result?.putAll post.map
        }
    }
}
