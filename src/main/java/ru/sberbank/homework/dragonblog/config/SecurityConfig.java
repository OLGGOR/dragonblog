package ru.sberbank.homework.dragonblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by Mart
 * 01.07.2019
 **/
@EnableWebMvc
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("admin").password(encoder().encode("adminPass")).roles("ADMIN")
                .and()
                .withUser("user").password(encoder().encode("userPass")).roles("USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling()
                .and().authorizeRequests()
//                    .antMatchers("/posts").permitAll()
                    .antMatchers("/**").hasAnyRole("ADMIN", "USER")
                    .anyRequest().authenticated()
                .and().formLogin()
                    .loginPage("/login")
                    .permitAll()
//                    .loginProcessingUrl("/login")
//                    .usernameParameter("username")
//                    .passwordParameter("password")
//                    .defaultSuccessUrl("/profile")
                .and().logout()
                    .permitAll()
//                    .logoutUrl("/logout")
//                    .logoutSuccessUrl("/login")
                .and().exceptionHandling()
                    .accessDeniedPage("/error");
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

}
