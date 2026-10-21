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

/** Core reads and writes. The sandbox implementation opens no socket. */
public interface CredentialPort {
    Mono<CredentialView> read(UUID id);

    Mono<StoredCredential> load(UUID id);

    Mono<StoredCredential> create(CredentialDTO draft);

    Mono<StoredCredential> update(UUID id, CredentialDTO draft);

    Mono<Void> remove(UUID id);
}
