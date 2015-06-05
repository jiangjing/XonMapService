import com.xonmap.Constants
import com.xonmap.domain.Role
import com.xonmap.domain.Tag

class BootStrap {
    def commonService

    def init = { servletContext ->
        def userRole = Role.findByName(Constants.ROLE_USER)
        if(!userRole){
            userRole = new Role(name : Constants.ROLE_USER).save(failOnError: true)
        }

        def adminRole = Role.findByName(Constants.ROLE_ADMIN)
        if(!adminRole){
            adminRole = new Role(name : Constants.ROLE_ADMIN).save(faileOnError : true)
        }

        def newsTag = Tag.findByName(Constants.TAG_NEWS)
        if(!newsTag){
            newsTag = new Tag(name : Constants.TAG_NEWS, duration : 7).save(failOnError: true)
        }

        def promotionTag = Tag.findByName(Constants.TAG_PROMOTION)
        if(!promotionTag){
            promotionTag = new Tag(name : Constants.TAG_PROMOTION).save(failOnError: true)
        }

        def momentsTag = Tag.findByName(Constants.TAG_MOMENTS)
        if(!momentsTag){
            momentsTag = new Tag(name : Constants.TAG_MOMENTS, duration : 3).save(failOnError: true)
        }

        def othersTag = Tag.findByName(Constants.TAG_OTHERS)
        if(!othersTag){
            othersTag = new Tag(name : Constants.TAG_OTHERS, duration : 7).save(failOnError: true)
        }
    }

    def destroy = {
    }
}
