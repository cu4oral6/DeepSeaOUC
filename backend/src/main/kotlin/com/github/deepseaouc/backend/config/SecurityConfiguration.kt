package com.github.deepseaouc.backend.config

import com.github.deepseaouc.backend.entity.RestBean
import com.github.deepseaouc.backend.entity.vo.response.AuthorizeVO
import com.github.deepseaouc.backend.filter.JwtAuthorizeFilter
import com.github.deepseaouc.backend.service.AccountService
import com.github.deepseaouc.backend.utils.JwtUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfiguration(
    private val utils: JwtUtils,
    private val jwtAuthorizeFilter: JwtAuthorizeFilter,
    private val service: AccountService,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/api/auth/**",
                        "/api/chat/stream/**",
                        "/error"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin {
                it
                    .loginProcessingUrl("/api/auth/login")
                    .failureHandler(authenticationFailureHandler)
                    .successHandler(authenticationSuccessHandler)
            }
            .logout {
                it
                    .logoutUrl("/api/auth/logout")
                    .logoutSuccessHandler(logoutSuccessHandler)
            }
            .exceptionHandling {
                it
                    .authenticationEntryPoint(unauthenticatedHandler)
                    .accessDeniedHandler(accessDeniedHandler)
            }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    val authenticationSuccessHandler =
        { _: HttpServletRequest,
          response: HttpServletResponse,
          authentication: Authentication ->
            response.contentType = "application/json;charset=UTF-8"

            val user = authentication.principal as UserDetails
            val account = service.findAccountByNameOrEmail(user.username)!!
            val vo = account.toAnotherObject(
                AuthorizeVO::class,
                mapOf(
                    "token" to utils.createJwt(user, account.id!!, account.username),
                    "expire" to utils.expiresTime()
                )
            )

            response.writer.write(
                RestBean.Companion
                    .success(vo)
                    .toJsonString()
            )
        }

    val authenticationFailureHandler =
        { _: HttpServletRequest,
          response: HttpServletResponse,
          exception: Exception ->
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write(
                RestBean.Companion
                    .unauthenticated(exception.message)
                    .toJsonString()
            )
        }

    val logoutSuccessHandler =
        { request: HttpServletRequest,
          response: HttpServletResponse,
          _: Authentication? ->
            response.contentType = "application/json;charset=UTF-8"
            val writer = response.writer
            val authorization = request
                .getHeader(HttpHeaders.AUTHORIZATION)
                .substring("Bearer ".length)
            if (utils.invalidateJwt(authorization))
                writer.write(RestBean.Companion.success().toJsonString())
            else writer.write(RestBean.Companion.logoutFailed().toJsonString())
        }

    val unauthenticatedHandler =
        { _: HttpServletRequest,
          response: HttpServletResponse,
          authException: AuthenticationException ->
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write(
                RestBean.Companion
                    .unauthenticated(authException.message)
                    .toJsonString()
            )
        }

    val accessDeniedHandler =
        { _: HttpServletRequest,
          response: HttpServletResponse,
          accessDeniedException: AccessDeniedException ->
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write(
                RestBean.Companion
                    .forbidden(accessDeniedException.message)
                    .toJsonString()
            )
        }
}