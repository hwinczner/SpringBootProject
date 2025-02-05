package com.SpringBoot.Project.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private JwtAuthEntryPoint jwtAuthEntryPoint;
    private CustomUserDetailsService customUserDetailsService;
    private JwtGenerator jwtGenerator;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthEntryPoint jwtAuthEntryPoint, JwtGenerator jwtGenerator) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtGenerator = jwtGenerator;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/leaves/submit").hasAuthority("EMPLOYEE")
                .requestMatchers("/api/leaves/**").hasAnyAuthority("ADMIN", "MANAGER")
                .requestMatchers("/api/departments/**").hasAuthority("ADMIN")
                .requestMatchers("/api/employees/**").hasAuthority("ADMIN")
                .anyRequest().authenticated();
        http.addFilterBefore(jwtAuthenticationFilter(jwtGenerator), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtGenerator jwtGenerator){
        return new JwtAuthenticationFilter(jwtGenerator, customUserDetailsService);
    }
}
