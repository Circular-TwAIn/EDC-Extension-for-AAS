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
package de.fraunhofer.iosb.app.edc;

import de.fraunhofer.iosb.app.dataplane.aas.pipeline.AasDataAddress;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.asset.spi.index.AssetIndex;

/**
 * Internal communication with EDC. Manages EDC assets and contracts.
 */
public class ResourceHandler {

    private final AssetIndex assetIndex;

    public ResourceHandler(AssetIndex assetIndex) {
        this.assetIndex = assetIndex;
    }

    /**
     * Registers an asset at the EDC.
     *
     * @param sourceUrl   Data of the asset
     * @param name        The name of the asset (e.g., idShort)
     * @param contentType Content behind the sourceUrl
     * @param version     For versioning of assets
     * @return asset ID of created asset
     */
    public String createAsset(String sourceUrl, String name, String contentType, String version) {
        var assetId = createAssetId(sourceUrl);
        var dataAddress = AasDataAddress.Builder.newInstance().baseUrl(sourceUrl).build();
        var asset = Asset.Builder.newInstance()
                .id(assetId)
                .name(name)
                .contentType(contentType)
                .version(version)
                .dataAddress(dataAddress)
                .build();
        assetIndex.create(asset);
        return assetId;
    }

    /**
     * Removes asset from assetIndex.
     *
     * @param assetId asset id
     */
    public void deleteAsset(String assetId) {
        assetIndex.deleteById(assetId);
    }

    private String createAssetId(String sourceUrl) {
        return String.valueOf(sourceUrl.hashCode());
    }
}
