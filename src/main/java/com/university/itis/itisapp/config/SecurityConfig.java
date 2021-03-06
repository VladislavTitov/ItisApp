package com.university.itis.itisapp.config;//package com.todoist.sql.server.config;

import com.university.itis.itisapp.authentication.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

@Configuration
@ComponentScan
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private UserDetailsService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RestAuthenticationFailureHandler restAuthenticationFailureHandler;
    @Autowired
    private RestAuthenticationSuccessHandler restAuthenticationSuccessHandler;
    @Autowired
    private RestAuthenticationFilter restAuthenticationFilter;
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    @Autowired
    private RestLogoutSuccessHandler restLogoutSuccessHandler;

//    @Bean
//    public RestUserNameAuthenticationFilter restUserNameAuthenticationFilter() throws Exception {
//        RestUserNameAuthenticationFilter restUserNameAuthenticationFilter = new RestUserNameAuthenticationFilter();
//        restUserNameAuthenticationFilter.setAuthenticationManager(authenticationManager());
//        restUserNameAuthenticationFilter.setAuthenticationSuccessHandler(restAuthenticationSuccessHandler);
//        restUserNameAuthenticationFilter.setAuthenticationFailureHandler(restAuthenticationFailureHandler);
//        restUserNameAuthenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
//        restUserNameAuthenticationFilter.setUsernameParameter("username");
//        restUserNameAuthenticationFilter.setPasswordParameter("password");
//        return restUserNameAuthenticationFilter;
//    }
    @Autowired
    private RestLogoutHandler restLogoutHandler;

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public RestAuthenticationProcessingFilter restAuthenticationProcessingFilter() throws Exception {
        RestAuthenticationProcessingFilter restAuthenticationProcessingFilter =
                new RestAuthenticationProcessingFilter("/login", HttpMethod.POST);
        restAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        restAuthenticationProcessingFilter.setAuthenticationFailureHandler(restAuthenticationFailureHandler);
        restAuthenticationProcessingFilter.setAuthenticationSuccessHandler(restAuthenticationSuccessHandler);
        return restAuthenticationProcessingFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().authenticationEntryPoint(restAuthenticationEntryPoint).and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/news/week/*", "/news/month/*", "/search/*")
                .permitAll()

                .antMatchers(HttpMethod.POST, "/login", "/ping").permitAll()
                .anyRequest().authenticated()
                .and().logout().permitAll()

                .logoutSuccessHandler(restLogoutSuccessHandler).addLogoutHandler(restLogoutHandler)
                .and().logout().permitAll()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().csrf().disable()
                .addFilterBefore(restAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(restAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationMgr) throws Exception {
        authenticationMgr.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }
}
