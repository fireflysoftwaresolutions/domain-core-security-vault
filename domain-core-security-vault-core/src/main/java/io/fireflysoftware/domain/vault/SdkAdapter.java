/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fireflysoftware.domain.vault;

import io.fireflysoftware.common.security.vault.sdk.api.CredentialsApi;
import io.fireflysoftware.common.security.vault.sdk.invoker.ApiClient;
import io.fireflysoftware.common.security.vault.sdk.model.CredentialDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Live adapter. Calls core-common-security-vault-sdk. Selected with firefly.vault.credential.adapter=core. */
public final class SdkAdapter implements CorePort {
    private final CredentialsApi api;

    public SdkAdapter(String basePath) {
        ApiClient client = new ApiClient();
        client.setBasePath(basePath);
        this.api = new CredentialsApi(client);
    }

    SdkAdapter(CredentialsApi api) {
        this.api = api;
    }

    @Override
    public Mono<CredentialView> read(UUID id) {
        return api.getCredentialById(id, null).map(dto -> new CredentialView(dto.getId(), dto.getCode()));
    }
}
