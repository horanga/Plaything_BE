package com.plaything.api.security;

import com.plaything.api.domain.auth.model.response.CustomUserDetails;
import com.plaything.api.domain.profile.constants.Role;
import com.plaything.api.domain.repository.entity.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {

  private final JWTProvider jwtProvider;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

    String servletPath = request.getServletPath();
    return Stream.of(SecurityConstants.getAuthWhitelist())
        .anyMatch(p -> pathMatcher.match(p, servletPath));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {

      String authorization = request.getHeader("Authorization");

      if (authorization == null || !authorization.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
      }

      String token = authorization.split(" ")[1];

      //토큰 소멸 시간 검증

      if (jwtProvider.isExpired(token)) {
        filterChain.doFilter(request, response);
        return;
      }

      String username = jwtProvider.getUsername(token);
      String roleString = jwtProvider.getRole(token);

      Role role = roleString.equals("ROLE_USER") ? Role.ROLE_USER : Role.ROLE_ADMIN;

      User user = User.builder().loginId(username).role(role).build();
      CustomUserDetails customUserDetails = new CustomUserDetails(user);

      //스프링 시큐리티 인증 토큰 생성
      Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null,
          customUserDetails.getAuthorities());
      //세션에 사용자 등록

      SecurityContextHolder.getContext().setAuthentication(authToken);
      filterChain.doFilter(request, response);

    } catch (SignatureException | MalformedJwtException | ExpiredJwtException e) {
      handleAuthenticationException(response, e);
    } catch (Exception e) {
      handleUnexpectedException(response, e);
    }
  }

  private void handleAuthenticationException(HttpServletResponse response, Exception e)
      throws IOException {
    SecurityContextHolder.clearContext();
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write("Authentication failed");
  }

  private void handleUnexpectedException(HttpServletResponse response, Exception e)
      throws IOException {
    SecurityContextHolder.clearContext();
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    response.getWriter().write("Authentication failed");
  }

}
