/*
 * Copyright 2026 Firefly Software Foundation
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

package io.fireflysoftware.domain.vault.core.credential;

import io.fireflysoftware.common.security.vault.sdk.api.CredentialsApi;
import io.fireflysoftware.common.security.vault.sdk.model.CredentialDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Live adapter. Selected with firefly.vault.credential.adapter=core. */
public final class SdkCredentialAdapter implements CredentialPort {
    private final CredentialsApi api;

    public SdkCredentialAdapter(CredentialsApi api) {
        this.api = api;
    }

    @Override
    public Mono<CredentialView> read(UUID id) {
        return api.getCredentialById(id, null).map(dto -> new CredentialView(dto.getId(), dto.getCode()));
    }

    @Override
    public Mono<StoredCredential> load(UUID id) {
        return api.getCredentialById(id, null).map(dto -> new StoredCredential(dto.getId() == null ? id : dto.getId(), dto));
    }

    @Override
    public Mono<StoredCredential> create(CredentialDTO draft) {
        return api.createCredential(draft, key()).map(dto -> new StoredCredential(dto.getId(), dto));
    }

    @Override
    public Mono<StoredCredential> update(UUID id, CredentialDTO draft) {
        return api.updateCredential(id, draft, key()).map(dto -> new StoredCredential(dto.getId() == null ? id : dto.getId(), dto));
    }

    @Override
    public Mono<Void> remove(UUID id) {
        return api.deleteCredential(id, key()).then();
    }

    private static String key() {
        return UUID.randomUUID().toString();
    }
}
