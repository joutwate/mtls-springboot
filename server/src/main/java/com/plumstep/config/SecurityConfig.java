package com.plumstep.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    protected void configure(HttpSecurity http) throws Exception {
        // Disable http basic authentication since we are using client auth.
        http.httpBasic().disable();

        // Turn off CSRF and session management for this REST service.
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
    }
}
