package com.xonmap

import com.xonmap.security.XAuthenticationFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import grails.util.Holders
import org.springframework.security.web.access.ExceptionTranslationFilter

@Configuration
@EnableWebSecurity
public class ProviderSecurityConfig extends WebSecurityConfigurerAdapter {
    def mainContext = Holders.grailsApplication.mainContext
    def commonService = mainContext.commonService

    @Autowired
    AuthenticationProvider authenticationProvider

    @Autowired
    XAuthenticationFilter xAuthenticationFilter

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                csrf().disable().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().
                addFilterAfter(authenticationFilter(), ExceptionTranslationFilter.class)
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.authenticationProvider(authenticationProvider)
    }

    XAuthenticationFilter authenticationFilter() {
        xAuthenticationFilter.setAuthenticationManager(authenticationManager())

        return xAuthenticationFilter
    }
}
