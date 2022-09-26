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
package de.fraunhofer.iosb.app.controller;

import de.fraunhofer.iosb.app.RequestType;
import de.fraunhofer.iosb.app.aas.AasAgent;
import de.fraunhofer.iosb.app.aas.AssetAdministrationShellServiceManager;
import de.fraunhofer.iosb.app.aas.FaaastServiceManager;
import de.fraunhofer.iosb.app.model.aas.CustomAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import jakarta.ws.rs.core.Response;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Handles requests regarding the Asset Administration Shells registered to this
 * extension
 */
public class AasController implements Controllable {
    private final AasAgent aasAgent;
    private final AssetAdministrationShellServiceManager aasServiceManager;

    public AasController(OkHttpClient okHttpClient) {
        aasAgent = new AasAgent(okHttpClient);
        aasServiceManager = new FaaastServiceManager();
    }

    @Override
    public Response handleRequest(RequestType requestType, URL url, String... requestData) {
        switch (requestType) {
            case POST:
                return aasAgent.postModel(url, requestData[1], requestData[2]);
            case PUT:
                return aasAgent.putModel(url, requestData[1], requestData[2]);
            case DELETE:
                return aasAgent.deleteModel(url, requestData[1], requestData[2]);

            default:
                return Response.status(Response.Status.NOT_IMPLEMENTED).build();
        }
    }

    /**
     * Returns the AAS model of the AAS service behind the aasServiceUrl, as a self
     * description (see model/aas/*). This model has the access URL of each AAS
     * element in the contractId field.

     * @param aasServiceUrl url of the service
     * @return aasServiceUrl's model, in self description form
     * @throws DeserializationException AAS from service could not be deserialized
     * @throws IOException Communication with AAS service failed
     */
    public CustomAssetAdministrationShellEnvironment getAasModelWithUrls(URL aasServiceUrl)
            throws IOException, DeserializationException {
        return aasAgent.getAasEnvWithUrls(aasServiceUrl);
    }

    /**
     * Start an AAS service internally by given parameters

     * @param aasModelPath   AAS Environment for the AAS service
     * @param aasServicePort AAS service's exposed HTTP port for communication
     *                       with this extension
     * @param aasConfigPath  AAS config (optional)
     * @return The URL of the new service or null on error
     * @throws IOException If the URL creation fails
     */
    public URL startService(Path aasModelPath, int aasServicePort, Path aasConfigPath) throws IOException {
        if (Objects.isNull(aasConfigPath)) {
            return aasServiceManager.startService(aasModelPath, aasServicePort);
        }
        return aasServiceManager.startService(aasModelPath, aasConfigPath);
    }

    /**
     * Stops an AAS service by its URL if internally started

     * @param aasServiceUrl URL of service to be stopped
     */
    public void stopAssetAdministrationShellService(URL aasServiceUrl) {
        aasServiceManager.stopService(aasServiceUrl);
    }

    /**
     * Stops all internally started AAS services
     */
    public void stopServices() {
        aasServiceManager.stopServices();
    }
}
