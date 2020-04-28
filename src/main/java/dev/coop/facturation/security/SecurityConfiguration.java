package dev.coop.facturation.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = false, prePostEnabled = false)
@EnableWebMvcSecurity
public class SecurityConfiguration 
    extends WebSecurityConfigurerAdapter 
{

    @Autowired
    private UserDetailsService userDetailsService;
    
    private final PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
    
    @Bean
    public PasswordEncoder passwordEncoder() throws Exception {
        return passwordEncoder;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.antMatcher("/**").httpBasic()
           .and().authorizeRequests()
              .antMatchers("/**").permitAll()
          //    .antMatchers("/**").authenticated()
           .and()
              .csrf().disable();
    
//        http.csrf().disable();
    }
}
