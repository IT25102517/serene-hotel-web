package lk.serene.shared;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  UserDetailsService users(@Value("${serene.demo-password}") String password) {
    var users = new InMemoryUserDetailsManager();
    var encoder = new BCryptPasswordEncoder();
    for (String role :
        new String[] {
          "customer",
          "customer2",
          "reservations",
          "events",
          "operations",
          "marketing",
          "finance",
          "feedback"
        })
      users.createUser(
          User.withUsername(role)
              .password("{bcrypt}" + encoder.encode(password))
              .roles(role.startsWith("customer") ? "CUSTOMER" : role.toUpperCase())
              .build());
    return users;
  }

  @Bean
  SecurityFilterChain filter(HttpSecurity http) throws Exception {
    return http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/api/public/**", "/api/health", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(withDefaults())
        .build();
  }
}
