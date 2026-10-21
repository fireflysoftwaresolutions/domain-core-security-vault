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

import io.fireflysoftware.common.security.vault.sdk.model.CredentialDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Default adapter. Holds rows in memory and does not call the core. */
public final class SandboxCredentialAdapter implements CredentialPort {
    private final ConcurrentHashMap<UUID, CredentialDTO> rows = new ConcurrentHashMap<>();

    @Override
    public Mono<CredentialView> read(UUID id) {
        if (id != null && rows.containsKey(id)) {
            return Mono.just(view(id, rows.get(id)));
        }
        UUID resolved = id == null ? UUID.fromString("00000000-0000-4000-8000-000000000001") : id;
        return Mono.just(new CredentialView(resolved, "primary"));
    }

    @Override
    public Mono<StoredCredential> load(UUID id) {
        CredentialDTO body = id == null ? null : rows.get(id);
        if (body == null) {
            return Mono.error(new IllegalStateException("Credential " + id + " was not found"));
        }
        return Mono.just(new StoredCredential(id, body));
    }

    @Override
    public Mono<StoredCredential> create(CredentialDTO draft) {
        if (draft == null) {
            return Mono.error(new IllegalArgumentException("Credential is required"));
        }
        UUID id = UUID.randomUUID();
        rows.put(id, draft);
        return Mono.just(new StoredCredential(id, draft));
    }

    @Override
    public Mono<StoredCredential> update(UUID id, CredentialDTO draft) {
        if (id == null || !rows.containsKey(id)) {
            return Mono.error(new IllegalStateException("Credential " + id + " was not found"));
        }
        if (draft == null) {
            return Mono.error(new IllegalArgumentException("Credential is required"));
        }
        rows.put(id, draft);
        return Mono.just(new StoredCredential(id, draft));
    }

    @Override
    public Mono<Void> remove(UUID id) {
        if (id != null) {
            rows.remove(id);
        }
        return Mono.empty();
    }

    private static CredentialView view(UUID id, CredentialDTO body) {
        String label = body == null ? null : body.getCode();
        return new CredentialView(id, label);
    }
}
