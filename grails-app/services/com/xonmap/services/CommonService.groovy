package com.xonmap.services

import com.xonmap.Constants
import com.xonmap.XUserAgentInterceptor
import com.xonmap.domain.Comment
import com.xonmap.domain.User
import com.xonmap.domain.Role
import com.xonmap.domain.Tag
import com.xonmap.domain.Post
import grails.transaction.Transactional
import grails.util.Holders
import org.apache.commons.lang.RandomStringUtils
import org.grails.web.json.JSONObject
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

import java.security.MessageDigest

class CommonService {
    def config = Holders.config
    def messageSource

    def stringToDate(s) {
        if (s.indexOf(' ') == -1) {
            return new Date().parse("dd/MM/yyyy", s)
        } else {
            return new Date().parse("dd/MM/yyyy HH:mm:ss", s)
        }
    }

    def stringToDateX(s) {
        if (s.indexOf(' ') == -1) {
            return new Date().parse("dd/MM/yyyy HH:mm:ss", s + " 23:59:59")
        } else {
            return new Date().parse("dd/MM/yyyy HH:mm:ss", s)
        }
    }

    def dateToString(d) {
        return d.format("dd/MM/yyyy HH:mm:ss")
    }

    def distance(startLatitude, startLongitude, endLatitude, endLongitude) {
        def latitudeDistance = Math.toRadians(endLatitude - startLatitude);
        def longitudeDistance = Math.toRadians(endLongitude - startLongitude);
        def a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2) +
                Math.cos(Math.toRadians(startLatitude)) * Math.cos(Math.toRadians(endLatitude)) *
                Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2)

        def c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        def distance = Constants.EARTH_RADIUS * c;

        return distance;
    }

    def encodeText(text) {
        def messageDigest = MessageDigest.getInstance('SHA')
        messageDigest.update(text.getBytes("UTF-8"))

        return messageDigest.digest().encodeBase64().toString();
    }

    def validateAccount(email, password, messages) {
        def user
        if (!email || !password) {
            if (!email) {
                messages?.add getMessage("user.email.empty")
            }

            if (!password) {
                messages?.add getMessage("user.password.empty")
            }
        } else {
            user = User.findByEmail(email)
            if (user) {
                if (encodeText(password) != user.password) {
                    messages?.add getMessage("user.credentials.invalid")
                    user = null
                }
            } else {
                messages?.add getMessage("user.credentials.invalid")
            }
        }

        return user
    }

    def validateAccountByToken(email, token, messages) {
        def user
        if (!email || !token) {
            if (!email) {
                messages?.add getMessage("user.email.empty")
            }

            if (!token) {
                messages?.add getMessage("user.token.empty")
            }
        } else {
            user = User.findByEmail(email)
            if (user) {
                if (token != user.token) {
                    messages?.add getMessage("user.token.invalid")
                    user = null
                }
            } else {
                messages?.add getMessage("user.credentials.invalid")
            }
        }

        return user
    }

    def validateExternalAccount(userSource, userId, accessToken, messages) {
        def user
        if (!userSource || !userId || !accessToken) {
            if(!userSource){
                messages.add message(code: "user.external.source.empty")
            }

            if (!userId) {
                messages.add message(code: "user.external.id.empty")
            }

            if(!accessToken){
                messages.add message(code: "user.external.accesstoken.empty")
            }
        } else {
            if(userSource == Constants.USER_SOURCE_FACEBOOK){
                def userEmail = validateFacebookAccount(userId, accessToken, messages)
                log.debug("External Account Email " + userEmail)
                if(userEmail){
                    user = User.findByEmail(userEmail)
                    if(!user){
                        log.debug("Couldn't find user by email " + userEmail)
                        messages?.add getMessage("user.credentials.invalid")
                    }
                    else{
                        log.debug("User found")
                        if(user.userSource == Constants.USER_SOURCE_FACEBOOK && user.externalUserId != userId){
                            log.debug("User external id doens't match " + user.externalUserId + "\t" + userId)
                            messages?.add getMessage("user.credentials.invalid")
                        }
                    }
                }
                else{
                    log.debug("Couldn't retrieve external account email.")
                    messages?.add getMessage("user.credentials.invalid")
                }
            }
            else {
                log.debug("Invalid user source " + userSource)
                messages.add getMessage("user.source.invalid", [userSource])
            }
        }

        return user
    }

    def getPost(postId, messages, option) {
        def post
        if (!postId) {
            if(option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("post.id.empty")
            }
        } else {
            post = Post.get(postId)
            if (!post && option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("post.id.invalid", [postId])
            }
        }

        return post
    }

    def getTag(name, messages, option) {
        def tag
        if (!name) {
            if(option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("tag.name.empty")
            }
        } else {
            tag = Tag.findByName(name)
            if (!tag && option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("tag.name.invalid", [name])
            }

            if (tag && option == Constants.WARN_IF_FOUND) {
                messages?.add getMessage("tag.name.existing", [name])
            }
        }

        return tag
    }

    def getComment(commentId, messages, option) {
        def comment
        if (!commentId) {
            if(option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("comment.id.empty")
            }
        } else {
            comment = Comment.get(commentId)
            if (!comment && option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("comment.id.invalid", [commentId])
            }
        }

        return comment
    }

    def getRole(name, messages, option) {
        def role
        if (!name) {
            if(option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("role.name.empty")
            }
        } else {
            role = Role.findByName(name)
            if (!role && option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("role.name.invalid", [name])
            }

            if (role && option == Constants.WARN_IF_FOUND) {
                messages?.add getMessage("role.name.existing", [name])
            }
        }

        return role
    }

    def getUser(email, messages, option) {
        def user
        if (!email) {
            if(option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("user.email.empty")
            }
        } else {
            user = User.findByEmail(email)
            if (!user && option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("user.email.invalid", [email])
            }

            if (user && option == Constants.WARN_IF_FOUND) {
                messages?.add getMessage("user.email.existing", [email])
            }
        }

        return user
    }

    def getUserByNickname(nickname, messages, option) {
        def user
        if (!nickname) {
            if(option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("user.nickname.empty")
            }
        } else {
            user = User.findByNickname(nickname)
            if (!user && option == Constants.WARN_IF_NOT_FOUND) {
                messages?.add getMessage("user.nickname.invalid", [nickname])
            }

            if (user && option == Constants.WARN_IF_FOUND) {
                messages?.add getMessage("user.nickname.existing", [nickname])
            }
        }

        return user
    }

    def validateFacebookAccount(userid, accessToken, messages){
        def meUrl = config.getProperty("xonmap.facebook.api.contextRoot") + "/me?access_token={access_token}"
        def params = [:]
        params.access_token = accessToken

        def result = get(meUrl, null, params)
        log.debug("Me result: " + result)
        if(result.id != userid){
            messages.add getMessage("user.facebook.credentials.invalid")

            return null
        }

        return result.email
    }

    private def get(url, headers, params) {
        log.debug("Get url " + url)
        log.debug("Params " + params)
        def xUserAgentInterceptor = new XUserAgentInterceptor(headers)

        def restTemplate = new RestTemplate()
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter())
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter())
        restTemplate.setInterceptors(Collections.singletonList(xUserAgentInterceptor))

        return restTemplate.getForObject(url, JSONObject.class, params)
    }

    def randomPassword() {
        return RandomStringUtils.random(8, true, true)
    }

    def newToken(email) {
        return encodeText(UUID.randomUUID().toString().toUpperCase() + "|" + email)
    }

    private getMessage(code) {
        return messageSource.getMessage(code, null, null)
    }

    private getMessage(code, params) {
        return messageSource.getMessage(code, params as Object[], null)
    }

    @Transactional
    def like(user, post, result, messages) {
        if (!post.likers?.contains(user)) {
            post.addToLikers(user)

            saveObject(post, result, messages, true)
        } else {
            messages.add getMessage("post.liked")
        }
    }

    @Transactional
    def dislike(user, post, result, messages) {
        if (post.likers?.contains(user)) {
            post.removeFromLikers(user)

            saveObject(post, result, messages, true)
        } else {
            messages.add getMessage(code: "post.disliked")
        }
    }

    @Transactional
    def comment(user, post, commentText, result, messages) {
        def comment = new Comment()
        comment.text = commentText
        comment.user = user

        post.addToComments(comment)

        saveObject(post, result, messages, true)
    }

    @Transactional
    def uncomment(comment, result, messages) {
        def post = comment.post
        post.removeFromComments(comment)

        saveObject(post, result, messages, true)
    }

    @Transactional
    def follow(user, followee, result, messages) {
        if (!user.followees?.contains(followee)) {
            if (!followee.followers?.contains(user)) {
                followee.addToFollowers(user)
                saveObject(followee, null, messages, true)
            }

            user.addToFollowees(followee)
            saveObject(user, result, messages, true)
        } else {
            messages.add getMessage("user.followed", [followee.email])
        }
    }

    @Transactional
    def unfollow(user, followee, result, messages) {
        if (user.followees?.contains(followee)) {
            if (followee.followers?.contains(user)) {
                followee.removeFromFollowers(user)
                saveObject(followee, null, messages, true)
            }

            user.removeFromFollowees(followee)
            saveObject(user, result, messages, true)
        } else {
            messages.add getMessage("user.not.followed", [followee.email])
        }
    }

    @Transactional
    def deleteObject(object) {
        object.delete()
    }

    @Transactional
    def saveObject(object, result, messages, updated) {
        if (updated) {
            if (object.validate()) {
                object.save()

                result?.putAll object.map
            } else {
                object.errors.allErrors.each {
                    messages?.add message(error: it)
                }
            }
        } else {
            result?.putAll object.map
        }
    }
}
