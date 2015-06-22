package com.xonmap.security

import org.apache.catalina.core.ApplicationFilterChain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.filter.GenericFilterBean
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import grails.util.Holders

@Component
class XAuthenticationFilter extends GenericFilterBean {
    def config = Holders.config
    def mainContext = Holders.grailsApplication.mainContext

    @Autowired
    AuthenticationManager authenticationManager

    XAuthenticationFilter() {

    }

    void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(chain.class.name == ApplicationFilterChain.class.name){
            chain.doFilter(request, response)
        }
        else {
            def httpRequest = (HttpServletRequest) request
            def httpResponse = (HttpServletResponse) response
            httpResponse.setHeader("Access-Control-Allow-Origin", "*")

            if (!httpRequest.post) {
                def parameters = new Object[1]
                parameters[0] = httpRequest.method
                httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, mainContext.getMessage("method.not.supported", parameters, null));
            } else {
                def contextPath = httpRequest.contextPath
                def requestURI = httpRequest.requestURI
                def authenticationURIList = config.getProperty("xonmap.authentication.uri").split(",")

                def authenticationRequired
                def role
                for (def i = 0; i < authenticationURIList.size(); i++) {
                    def authenticationURI = authenticationURIList[i]
                    role = null
                    def index = authenticationURI.indexOf('[')
                    if (index >= 0) {
                        role = authenticationURI.substring(index + 1, authenticationURI.length() - 1)
                        authenticationURI = authenticationURI.substring(0, index)
                    }

                    if (requestURI.startsWith(contextPath + authenticationURI)) {
                        authenticationRequired = true
                        break;
                    }
                }

                if (authenticationRequired) {
                    logger.debug("Authentication Required")
                    def email = httpRequest.getHeader("X-Auth-Email")
                    def password = httpRequest.getHeader("X-Auth-Password")
                    def token = httpRequest.getHeader("X-Auth-Token")

                    def authenticationToken
                    if (token) {
                        authenticationToken = new PreAuthenticatedAuthenticationToken(email, token)
                    } else {
                        authenticationToken = new UsernamePasswordAuthenticationToken(email, password)
                    }

                    def authentication
                    try {
                        authentication = attemptToAuthenticate(authenticationToken)
                        if (role) {
                            logger.debug("Authorization Required " + role)
                            def authorities = authentication.getAuthorities() as java.util.List
                            if (!authorities.contains(role)) {
                                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, mainContext.getMessage("user.not.authorized", null, null));
                            } else {
                                SecurityContextHolder.context.setAuthentication(authentication)
                                chain.doFilter(request, response)
                            }
                        } else {
                            SecurityContextHolder.context.setAuthentication(authentication)
                            chain.doFilter(request, response)
                        }
                    }
                    catch (AuthenticationException e) {
                        logger.error("Authentication failed", e)
                        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message);
                    }
                } else {
                    logger.debug("Authentication Not Required")
                    chain.doFilter(request, response)
                }
            }
        }
    }

    private Authentication attemptToAuthenticate(Authentication authentication) {
        Authentication responseAuthentication = authenticationManager.authenticate(authentication)
        if (responseAuthentication == null || !responseAuthentication.isAuthenticated()) {
            throw new InternalAuthenticationServiceException(mainContext.getMessage("user.unable.authenticate", null, null));
        }

        return responseAuthentication;
    }
}
