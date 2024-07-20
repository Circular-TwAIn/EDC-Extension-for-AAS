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
package de.fraunhofer.iosb.app.aas;

import de.fraunhofer.iosb.aas.AasDataProcessorFactory;
import de.fraunhofer.iosb.dataplane.aas.spi.AasDataAddress;
import okhttp3.Response;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.edc.spi.EdcException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static jakarta.ws.rs.HttpMethod.GET;
import static java.lang.String.format;

/**
 * Communicating with AAS service
 */
public class AasAgent {

    private static final int INTERNAL_SERVER_ERROR = 500;

    private final AasDataProcessorFactory aasDataProcessorFactory;

    private ModelParser modelParser;

    public AasAgent(AasDataProcessorFactory aasDataProcessorFactory) {
        this.aasDataProcessorFactory = aasDataProcessorFactory;
    }

    /**
     * Returns AAS model enriched with each elements access URL as string in
     * sourceUrl field.
     *
     * @param aasServiceUrl AAS service to be updated
     * @return AAS model enriched with each elements access URL as string in assetId field.
     */
    public Environment getAasEnvironment(URL aasServiceUrl)
            throws IOException {
        var aasServiceUrlString = format("%s/api/v3.0", aasServiceUrl);
        modelParser = new ModelParser(aasServiceUrlString);

        return readEnvironment(aasServiceUrl);
    }

    /**
     * Returns the AAS environment.
     */
    private Environment readEnvironment(URL aasServiceUrl)
            throws IOException {
        var aasEnv = new DefaultEnvironment();

        URL submodelUrl;
        URL shellsUrl;
        URL conceptDescriptionsUrl;
        try {
            submodelUrl = aasServiceUrl.toURI().resolve("/api/v3.0/submodels").toURL();
            shellsUrl = aasServiceUrl.toURI().resolve("/api/v3.0/shells").toURL();
            conceptDescriptionsUrl = aasServiceUrl.toURI().resolve("/api/v3.0/concept-descriptions").toURL();
        } catch (URISyntaxException resolveUriException) {
            throw new EdcException(
                    format("Error while building URLs for reading from the AAS service at %s", aasServiceUrl),
                    resolveUriException);
        }

        aasEnv.setSubmodels(readSubmodels(submodelUrl));
        aasEnv.setAssetAdministrationShells(readShells(shellsUrl));
        aasEnv.setConceptDescriptions(readConceptDescriptions(conceptDescriptionsUrl));

        return aasEnv;
    }

    private List<ConceptDescription> readConceptDescriptions(URL conceptDescriptionsUrl) throws IOException {
        try (var response = executeRequest(conceptDescriptionsUrl)) {
            var body = response.body();
            if (body == null || response.code() == INTERNAL_SERVER_ERROR) {
                throw new EdcException("Received empty body for concept description request");
            }
            return modelParser.parseConceptDescriptions(body.string());
        }
    }

    private List<AssetAdministrationShell> readShells(URL shellsUrl) throws IOException {
        try (var response = executeRequest(shellsUrl)) {
            var body = response.body();
            if (body == null || response.code() == INTERNAL_SERVER_ERROR) {
                throw new EdcException("Received empty body for shell request");
            }
            return modelParser.parseShells(body.string());
        }
    }

    private List<Submodel> readSubmodels(URL submodelUrl) throws IOException {
        try (var response = executeRequest(submodelUrl)) {
            var body = response.body();
            if (body == null || response.code() == INTERNAL_SERVER_ERROR) {
                throw new EdcException("Received empty body for submodel request");
            }
            return modelParser.parseSubmodels(body.string());
        }
    }

    private Response executeRequest(URL aasServiceUrl) throws IOException {
        return aasDataProcessorFactory.processorFor(aasServiceUrl.toString())
                .getContent()
                .send(AasDataAddress.Builder
                        .newInstance()
                        .method(GET)
                        .baseUrl(aasServiceUrl.toString())
                        .build());
    }
}
