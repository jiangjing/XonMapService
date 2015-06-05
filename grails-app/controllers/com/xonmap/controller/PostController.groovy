package com.xonmap.controller

import com.xonmap.Constants
import com.xonmap.domain.Image
import com.xonmap.domain.Post
import org.grails.web.json.JSONObject
import org.springframework.security.core.context.SecurityContextHolder

class PostController {
    static responseFormats = ["json"]
    static namespace = "user"
    def commonService

    def get() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def postId = input.postId
        try {
            def post = commonService.getPost(postId, messages, Constants.WARN_IF_NOT_FOUND)
            if (post) {
                result.putAll post.map
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
        try {
            if (latitude != null && longitude != null && (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)) {
                messages.add message(code: "post.location.invalid")
            } else {
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
                        creationDateTo = commonService.stringToDateX(strCreationDateTo)
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
                        startDateTo = commonService.stringToDateX(strStartDateTo)
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
                        endDateTo = commonService.stringToDateX(strEndDateTo)
                    }
                }

                def searchResult = Post.createCriteria().list {
                    if (content) {
                        or {
                            like("text", "%" + content + "%")
                            like("text", "%" + content + "%")
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

                    if (tagName) {
                        tag {
                            eq("name", tagName)
                        }
                    }

                    if (authorEmail) {
                        author {
                            eq("email", authorEmail)
                        }
                    }

                    order("id", "desc")
                }

                def posts = []
                searchResult.each {
                    def postLatitude = it.latitude
                    def postLongitude = it.longitude

                    if (distanceRange <= 0 || commonService.distance(latitude, longitude, postLatitude, postLongitude) <= distanceRange) {
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
        def text = input.text
        def strStartDate = input.startDate
        def strEndDate = input.endDate
        def latitude = input.latitude
        def longitude = input.longitude
        def tagName = input.tagName
        def imageUrls = input.imageUrls
        try {
            if (!text) {
                messages.add message(code: "post.text.empty")
            }

            if (latitude == null || longitude == null || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                messages.add message(code: "post.location.invalid")
            }

            def tag = commonService.getTag(tagName, messages, Constants.WARN_IF_NOT_FOUND)
            if (tag) {
                if (tag.duration == Constants.DURATION_NA) {
                    if (!strStartDate || !strEndDate) {
                        messages.add message(code: "post.duration.invalid")
                    }
                }
            }

            if (!messages) {
                def user = commonService.getUser(SecurityContextHolder.context.authentication.principal, messages, Constants.WARN_IF_NOT_FOUND)
                if (user) {
                    def post = new Post()
                    post.text = text
                    post.latitude = latitude
                    post.longitude = longitude
                    post.tag = tag
                    if (tag.duration == Constants.DURATION_NA) {
                        post.startDate = commonService.stringToDate(strStartDate)
                        post.endDate = commonService.stringToDateX(strEndDate)
                    }

                    post.author = user

                    if (imageUrls) {
                        imageUrls.each {
                            def image = new Image()
                            image.imageUrl = it

                            post.addToImages(image)
                        }
                    }

                    commonService.saveObject(post, result, messages, true)
                } else {
                    messages.add message(code: "user.not.found", args: [SecurityContextHolder.context.authentication.principal])
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

    def like() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def postId = input.postId
        try {
            def user = commonService.getUser(SecurityContextHolder.context.authentication.principal, messages, Constants.WARN_IF_NOT_FOUND)
            if (user) {
                def post = commonService.getPost(postId, messages, Constants.WARN_IF_NOT_FOUND)
                if (post) {
                    commonService.like(user, post, result, messages)
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

    def dislike() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def postId = input.postId
        try {
            def user = commonService.getUser(SecurityContextHolder.context.authentication.principal, messages, Constants.WARN_IF_NOT_FOUND)
            if (user) {
                def post = commonService.getPost(postId, messages, Constants.WARN_IF_NOT_FOUND)
                if (post) {
                    commonService.dislike(user, post, result, messages)
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

    def liking() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def email = input.email
        def postId = input.postId
        try {
            def user = commonService.getUser(email, messages, Constants.WARN_IF_NOT_FOUND)
            if (user) {
                def post = commonService.getPost(postId, messages, Constants.WARN_IF_NOT_FOUND)
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
            result.status = Constants.STATUS_FAIL
            result.messages = messages
        } else {
            result.status = Constants.STATUS_SUCCESSFUL
        }

        respond result
    }

    def comment() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def postId = input.postId
        def commentText = input.commentText
        try {
            if (!commentText) {
                messages.add message(code: "comment.text.empty")
            } else {
                def user = commonService.getUser(SecurityContextHolder.context.authentication.principal, messages, Constants.WARN_IF_NOT_FOUND)
                if (user) {
                    def post = commonService.getPost(postId, messages, Constants.WARN_IF_NOT_FOUND)
                    if (post) {
                        commonService.comment(user, post, commentText, result, messages)
                    }
                } else {
                    messages.add message(code: "user.not.found", args: [SecurityContextHolder.context.authentication.principal])
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

    def uncomment() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def commentId = input.commentId
        try {
            def comment = commonService.getComment(commentId, messages, Constants.WARN_IF_NOT_FOUND)
            if (comment) {
                commonService.uncomment(comment, result, messages)
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

    def delete() {
        def result = new JSONObject()
        def messages = []

        def input = request.JSON
        def postId = input.postId
        try {
            def post = commonService.getPost(postId, messages, Constants.WARN_IF_NOT_FOUND)
            if (post) {
                if (post.author.email != email) {
                    messages.add message(code: "post.delete.not.authorized")
                } else {
                    commonService.deleteObject(post)
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
