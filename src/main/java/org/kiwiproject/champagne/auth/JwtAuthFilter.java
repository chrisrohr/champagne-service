package org.kiwiproject.champagne.auth;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.isNull;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.collect.KiwiArrays.isNotNullOrEmpty;
import static org.kiwiproject.logging.LazyLogParameterSupplier.lazy;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthenticationException;
import io.jsonwebtoken.JwtException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter extends AuthFilter<JwtContext, UserPrincipal> {

    private final JwtProcessor processor;
    private final String cookieName;
    private final String[] excludedPaths;

    @Context
    @Setter
    private ResourceInfo resourceInfo;

    private JwtAuthFilter(JwtProcessor processor, String cookieName, String[] excludedPaths) {
        this.processor = processor;
        this.cookieName = cookieName;
        this.excludedPaths = excludedPaths;

        if (isNotNullOrEmpty(this.excludedPaths)) {
            LOG.info("Requests containing ANY of the following paths (via substring matching) will NOT require auth: {}", 
                Arrays.asList(this.excludedPaths));
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (authIsNotRequired(requestContext)) {
            LOG.debug("Endpoint is does NOT REQUIRE auth so we will just continue.");
            // TODO: Something to think about, what if we don't require auth but still
            //       want to track user stuff if the JWT is provided.
        }

        var optionalToken = getTokenFromHeaderOrCookie(requestContext);
        
        if (optionalToken.isPresent()) {
            try {
                var claims = verifyToken(optionalToken.get());
                var principal = authenticator.authenticate(claims);

                if (principal.isPresent()) {

                    // This is needed so that we can access the user in Jersey things
                    // e.g. @Auth and @RolesAllowed
                    requestContext.setSecurityContext(new SecurityContext() {

                        @Override
                        public Principal getUserPrincipal() {
                            return principal.get();
                        }

                        @Override
                        public boolean isUserInRole(String role) {
                            return authorizer.authorize(principal.get(), role, requestContext);
                        }

                        @Override
                        public boolean isSecure() {
                            return requestContext.getSecurityContext().isSecure();
                        }

                        @Override
                        public String getAuthenticationScheme() {
                            return SecurityContext.BASIC_AUTH;
                        }
                        
                    });

                    // This is needed so we don't have to pass the current user around
                    SecurityThreadLocal.setSecurityData(principal.get());
                }

            } catch (JwtException e) {
                LOG.warn("Unable to parse JWT token provided", e);
            } catch (AuthenticationException e) {
                LOG.warn("Error authenticating credentials", e);
                throw new InternalServerErrorException();
            }
        }

        throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }

    private boolean authIsNotRequired(ContainerRequestContext requestContext) {
        return !authIsRequired(requestContext);
    }

    private boolean authIsRequired(ContainerRequestContext requestContext) {
        var requestPath = requestContext.getUriInfo().getPath();
        if (containsAny(requestPath, excludedPaths)) {
            LOG.trace("Authorization is NOT REQUIRED for URI containing excluded path. Path: {} ; excluded paths: {}", 
                    requestPath, lazy(() -> Arrays.toString(excludedPaths)));
            return false;
        }

        if (isNull(resourceInfo)) {
            LOG.warn("ResourceInfo not populated, unable to fetch resource class/method context (so assuming auth is required)");
            return true;
        }

        if (ignoreAuthAnnotationIsPresent(resourceInfo)) {
            LOG.debug("Auth is NOT REQUIRED for annotated endpoint method: {}", 
                resourceInfo.getResourceMethod().getName());
            return false;
        }

        return true;
    }

    private static boolean ignoreAuthAnnotationIsPresent(ResourceInfo resource) {
        return Optional.ofNullable(resource.getResourceMethod().getDeclaredAnnotation(IgnoreAuth.class))
                .or(() -> Optional.ofNullable(resource.getResourceClass().getDeclaredAnnotation(IgnoreAuth.class)))
                .isPresent();
    }
    
    private Optional<String> getTokenFromHeaderOrCookie(ContainerRequestContext requestContext) {
        return getTokenFromHeader(requestContext.getHeaders())
                    .or(() -> getTokenFromCookie(requestContext.getCookies()));
    }

    private Optional<String> getTokenFromHeader(MultivaluedMap<String, String> headers) {
        var header = headers.getFirst(AUTHORIZATION);

        if (isNotBlank(header)) {
            var headerParts = header.split(" ");
            if (headerParts.length == 2 && prefix.equalsIgnoreCase(headerParts[0])) {
                return Optional.of(headerParts[1]);
            }
        }

        return Optional.empty();
    }

    private Optional<String> getTokenFromCookie(Map<String, Cookie> cookies) {
        if (isNotBlank(cookieName) && cookies.containsKey(cookieName)) {
            var tokenCookie = cookies.get(cookieName);
            var rawToken = tokenCookie.getValue();
            return Optional.of(rawToken);
        }

        return Optional.empty();
    }

    private JwtContext verifyToken(String token) {
        return processor.processToken(token);
    }

    public static class Builder<P extends Principal> extends AuthFilterBuilder<JwtContext, UserPrincipal, JwtAuthFilter> {

        private JwtProcessor processor;
        private String cookieName;

        public Builder<P> setProcessor(JwtProcessor processor) {
            this.processor = processor;
            return this;
        }

        public Builder<P> setCookieName(String cookieName) {
            this.cookieName = cookieName;
            return this;
        }

        @Override
        protected JwtAuthFilter newInstance() {
            checkNotNull(processor, "JwtProcessor is not set");
            return new JwtAuthFilter(processor, cookieName);
        }

    }
}