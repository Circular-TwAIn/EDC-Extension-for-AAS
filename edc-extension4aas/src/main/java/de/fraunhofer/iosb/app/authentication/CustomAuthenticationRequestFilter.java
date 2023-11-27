/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.app.authentication;

import java.util.Objects;

import org.eclipse.edc.api.auth.spi.AuthenticationRequestFilter;
import org.eclipse.edc.api.auth.spi.AuthenticationService;

import de.fraunhofer.iosb.app.Logger;
import jakarta.ws.rs.container.ContainerRequestContext;

/**
 * Custom AuthenticationRequestFilter filtering requests that go directly to an
 * AAS service (managed by this extension) or the extension's configuration.
 */
public class CustomAuthenticationRequestFilter extends AuthenticationRequestFilter {

    private static final Logger LOGGER = Logger.getInstance();
    private final String[] endpoints;

    public CustomAuthenticationRequestFilter(AuthenticationService authenticationService, String... acceptedEndpoints) {
        super(authenticationService);
        if (Objects.nonNull(acceptedEndpoints)) {
            endpoints = acceptedEndpoints;
        } else {
            endpoints = new String[0];
        }
    }

    /**
     * On automated data transfer: If the request is valid, the key,value pair used
     * for this request will no longer be valid.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        Objects.requireNonNull(requestContext);
        var requestPath = requestContext.getUriInfo().getPath();

        for (String endpoint : endpoints) {
            if (Objects.nonNull(endpoint) && endpoint.equalsIgnoreCase(requestPath)) {
                LOGGER.debug(
                        "CustomAuthenticationRequestFilter: Not intercepting this request to an open endpoint");
                return;
            }
        }

        LOGGER.debug("CustomAuthenticationRequestFilter: Intercepting this request");
        super.filter(requestContext);
    }
}
