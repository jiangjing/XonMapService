package com.xonmap.security

import com.xonmap.domain.User
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import grails.util.Holders
import org.apache.commons.logging.LogFactory
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Component
class XAuthenticationProvider implements AuthenticationProvider {
    def mainContext = Holders.grailsApplication.mainContext
    def commonService = mainContext.commonService
    protected final def logger = LogFactory.getLog(getClass())

    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        def actualAuthentication
        if(authentication instanceof UsernamePasswordAuthenticationToken){
            actualAuthentication = (UsernamePasswordAuthenticationToken)authentication

            def email = actualAuthentication.principal
            if(!email){
                throw new AuthenticationServiceException(mainContext.getMessage("user.email.empty", null, null))
            }

            def password = actualAuthentication.credentials
            if(!password){
                throw new AuthenticationServiceException(mainContext.getMessage("user.password.empty", null, null))
            }

            User.withTransaction { status ->
                def roleName
                def user = User.findByEmail(email)
                if (user) {
                    if (commonService.encodeText(password) != user.password) {
                        throw new AuthenticationServiceException(mainContext.getMessage("user.credentials.invalid", null, null))
                    }

                    roleName = user.role?.name
                } else {
                    throw new AuthenticationServiceException(mainContext.getMessage("user.credentials.invalid", null, null))
                }

                return new UsernamePasswordAuthenticationToken(email, password, [new SimpleGrantedAuthority(roleName)])
            }
        }
        else{
            actualAuthentication = (PreAuthenticatedAuthenticationToken)authentication

            def email = actualAuthentication.principal
            if(!email){
                throw new AuthenticationServiceException(mainContext.getMessage("user.email.empty", null, null))
            }

            def token = actualAuthentication.credentials
            if(!token){
                throw new AuthenticationServiceException(mainContext.getMessage("user.token.empty", null, null))
            }

            User.withTransaction { status ->
                def roleName
                def user = User.findByEmailAndToken(email, token)
                if (user) {
                    roleName = user.role?.name
                } else {
                    throw new AuthenticationServiceException(mainContext.getMessage("user.token.invalid", null, null))
                }

                return new PreAuthenticatedAuthenticationToken(user.email, token, [new SimpleGrantedAuthority(roleName)])
            }
        }
    }

    @Override
    boolean supports(Class<?> authentication) {
        return authentication == UsernamePasswordAuthenticationToken.class || authentication == PreAuthenticatedAuthenticationToken.class
    }
}