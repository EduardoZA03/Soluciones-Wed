package com.calsoft.seguridad;

import static com.calsoft.seguridad.Roles.ADMIN;
import static com.calsoft.seguridad.Roles.ALMACEN;
import static com.calsoft.seguridad.Roles.COCINA;
import static com.calsoft.seguridad.Roles.EMPLEADO;
import static com.calsoft.seguridad.Roles.MESERO;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Seguridad de la API:
 *  - Sin sesión en el servidor: cada petición trae su token JWT (stateless).
 *  - Contraseñas con BCrypt.
 *  - Permisos por rol para cada grupo de rutas (HU-01).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain seguridad(HttpSecurity http, JwtFiltro jwtFiltro) throws Exception {
        http
            // Con JWT en la cabecera no hay cookies de sesión: CSRF no aplica.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> { })
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                // Públicas
                .requestMatchers(POST, "/api/auth/login").permitAll()
                .requestMatchers("/api/proveedor-simulado/**").permitAll()   // usa su propia API key
                .requestMatchers(GET, "/api/qr").permitAll()                  // solo dibuja un texto

                // Tablas maestras: lectura para quien las usa, escritura restringida
                .requestMatchers(GET, "/api/categorias/**", "/api/insumos/**").hasAnyAuthority(ADMIN, ALMACEN, COCINA)
                .requestMatchers("/api/categorias/**", "/api/insumos/**").hasAnyAuthority(ADMIN, ALMACEN)
                .requestMatchers(GET, "/api/proveedores/**").hasAnyAuthority(ADMIN, ALMACEN)
                .requestMatchers("/api/proveedores/**").hasAuthority(ADMIN)
                .requestMatchers("/api/movimientos/**").hasAnyAuthority(ADMIN, ALMACEN)

                // Carta: el POS necesita leerla
                .requestMatchers(GET, "/api/platos/**").hasAnyAuthority(ADMIN, COCINA, MESERO, EMPLEADO)
                .requestMatchers("/api/platos/**").hasAnyAuthority(ADMIN, COCINA)

                // Operación
                .requestMatchers(GET, "/api/pedidos/**").hasAnyAuthority(ADMIN, MESERO, EMPLEADO, COCINA)
                .requestMatchers(PATCH, "/api/pedidos/*/estado").hasAnyAuthority(ADMIN, MESERO, EMPLEADO, COCINA)
                .requestMatchers("/api/pedidos/**").hasAnyAuthority(ADMIN, MESERO, EMPLEADO)
                .requestMatchers("/api/reservas/**").hasAnyAuthority(ADMIN, MESERO, EMPLEADO)

                // Inventario con IA e interoperabilidad
                .requestMatchers("/api/alertas/**").hasAnyAuthority(ADMIN, ALMACEN)
                .requestMatchers("/api/prediccion/**").hasAuthority(ADMIN)
                .requestMatchers("/api/ordenes/**").hasAnyAuthority(ADMIN, ALMACEN)

                // Administración
                .requestMatchers("/api/usuarios/**", "/api/reportes/**").hasAuthority(ADMIN)

                // El resto de la API (inicio, perfil) solo exige sesión
                .requestMatchers("/api/**").authenticated()
                // Archivos del frontend Angular (si se sirve desde aquí)
                .anyRequest().permitAll())
            .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Permite que el frontend Angular (localhost:4200) llame a la API. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${calsoft.cors.origenes}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origenes.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}
