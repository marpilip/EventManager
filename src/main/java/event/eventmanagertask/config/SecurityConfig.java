package event.eventmanagertask.config;

import event.eventmanagertask.jwt.JwtTokenFilter;
import event.eventmanagertask.jwt.JwtTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;
    @Autowired
    private CustomUserDetailService customUserDetailService;
    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @Autowired
    private final JwtTokenManager jwtTokenManager;

    public SecurityConfig(JwtTokenManager jwtTokenManager) {
        this.jwtTokenManager = jwtTokenManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // регистрация
                                .requestMatchers(HttpMethod.POST, "/users")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/users/auth")
                                .permitAll()

                                // Мероприятия
                                .requestMatchers(HttpMethod.POST, "/events")
                                .hasAnyAuthority("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/events")
                                .hasAnyAuthority("USER", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/events/**")
                                .hasAnyAuthority("USER", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/events/**")
                                .hasAnyAuthority("USER", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/events/**")
                                .hasAnyAuthority("USER", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/events/search")
                                .hasAnyAuthority("USER", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/events/my")
                                .hasAnyAuthority("USER", "ADMIN")

                                // Регистрация
                                .requestMatchers(HttpMethod.POST, "/events/registrations/**")
                                .hasAnyAuthority("USER", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/events/registrations/**")
                                .hasAnyAuthority("USER", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/events/registrations/**")
                                .hasAnyAuthority("USER", "ADMIN")

                                // Локации
                                .requestMatchers(HttpMethod.POST, "/locations")
                                .hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/locations/**")
                                .hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/locations/**")
                                .hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/locations/**")
                                .hasAnyAuthority("USER", "ADMIN")

                                .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler))
                .addFilterBefore(jwtTokenFilter(), AnonymousAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customUserDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtTokenFilter jwtTokenFilter() {
        return new JwtTokenFilter(jwtTokenManager);
    }
}
