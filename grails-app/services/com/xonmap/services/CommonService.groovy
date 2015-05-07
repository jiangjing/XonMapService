package com.xonmap.services

import com.xonmap.domain.Comment
import com.xonmap.domain.User
import com.xonmap.domain.Role
import com.xonmap.domain.Tag
import com.xonmap.domain.Post

import java.security.MessageDigest

class CommonService {
    def static final STATUS_FAIL = 0
    def static final STATUS_SUCCESSFUL = 1
    def static final ROLE_USER = "user"
    def static final ROLE_ADMIN = "admin"
    def static final STATE_ACTIVE = 0
    def static final STATE_INACTIVE = 1
    def static final WARN_IF_FOUND = 0
    def static final WARN_IF_NOT_FOUND = 1
    def static final WARN_MUTE = 2
    def static final USER_SOURCE_INLINE = 0
    def static final USER_SOURCE_FACEBOOK = 1
    def static final EARTH_RADIUS = 6371
    def messageSource

    def stringToDate(s){
        return new Date().parse("dd/MM/yyyy", s)
    }

    def stringToDateTime(s){
        return new Date().parse("dd/MM/yyyy HH:mm:ss", s + " 23:59:59")
    }

    def dateToString(d){
        return d.format("dd/MM/yyyy")
    }

    def dateTimeToString(d){
        return d.format("dd/MM/yyyy HH:mm:ss")
    }

    def distance(startLatitude, startLongitude, endLatitude, endLongitude){
        def latitudeDistance = Math.toRadians(endLatitude - startLatitude);
        def longitudeDistance = Math.toRadians(endLongitude - startLongitude);
        def a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2) +
                        Math.cos(Math.toRadians(startLatitude)) * Math.cos(Math.toRadians(endLatitude)) *
                        Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2)

        def c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        def distance = EARTH_RADIUS * c;

        return distance;
    }

    def encodePassword(password) {
        MessageDigest messageDigest = MessageDigest.getInstance('SHA')
        messageDigest.update(password.getBytes("UTF-8"))

        return messageDigest.digest().encodeBase64().toString();
    }

    def validateAccount(email, password, messages){
        def user
        if (!email || !password) {
            if (!email) {
                messages?.add getMessage("user.email.empty")
            }

            if (!password) {
                messages?.add getMessage("user.password.empty")
            }
        }
        else{
            user = User.findByEmail(email)
            if (user) {
                if (encodePassword(password) != user.password) {
                    messages?.add getMessage("user.credentials.invalid")
                    user = null
                }
            }
            else{
                messages?.add getMessage("user.credentials.invalid")
            }
        }

        return user
    }

    def validateAdmin(email, password, messages){
        def user = validateAccount(email, password, messages)
        if (user && !user.isAdmin) {
            messages?.add getMessage("user.not.authorized")

            user = null
        }

        return user
    }

    def getPost(postId, messages, option){
        def post
        if(!postId && option == WARN_IF_NOT_FOUND){
            messages?.add getMessage("post.id.empty")
        }
        else{
            post = Post.get(postId)
            if(!post && option == WARN_IF_NOT_FOUND){
                messages?.add getMessage("post.id.invalid", [postId])
            }
        }

        return post
    }

    def getTag(name, messages, option){
        def tag
        if(!name && option == WARN_IF_NOT_FOUND){
            messages?.add getMessage("tag.name.empty")
        }
        else{
            tag = Tag.findByName(name)
            if(!tag && option == WARN_IF_NOT_FOUND){
                messages?.add getMessage("tag.name.invalid", [name])
            }

            if(tag && option == WARN_IF_FOUND){
                messages?.add getMessage("tag.name.existing", [name])
            }
        }

        return tag
    }

    def getComment(commentId, messages, option){
        def comment
        if(!commentId && option == WARN_IF_NOT_FOUND){
            messages?.add getMessage("comment.id.empty")
        }
        else{
            comment = Comment.get(commentId)
            if(!comment && option == WARN_IF_NOT_FOUND){
                messages?.add getMessage("comment.id.invalid", [commentId])
            }
        }

        return comment
    }

    def getRole(name, messages, option){
        def role
        if(!name && option == WARN_IF_NOT_FOUND){
            messages?.add getMessage("role.name.empty")
        }
        else{
            role = Role.findByName(name)
            if(!role && option == WARN_IF_NOT_FOUND){
                messages?.add getMessage("role.name.invalid", [name])
            }

            if(role && option == WARN_IF_FOUND){
                messages?.add getMessage("role.name.existing", [name])
            }
        }

        return role
    }

    def getUser(email, messages, option){
        def user
        if (!email && option == WARN_IF_NOT_FOUND) {
            messages?.add getMessage("user.email.empty")
        }
        else{
            user = User.findByEmail(email)
            if (!user && option == WARN_IF_NOT_FOUND) {
                messages?.add getMessage("user.email.invalid", [email])
            }

            if(user && option == WARN_IF_FOUND){
                messages?.add getMessage("user.email.existing", [email])
            }
        }

        return user
    }

    private getMessage(code){
        return messageSource.getMessage(code, null, null)
    }

    private getMessage(code, params){
        return messageSource.getMessage(code, params as Object[], null)
    }
}
