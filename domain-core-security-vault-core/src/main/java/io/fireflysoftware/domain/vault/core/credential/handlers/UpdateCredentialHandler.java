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

package io.fireflysoftware.domain.vault.core.credential.handlers;

import io.fireflysoftware.domain.vault.core.credential.CredentialPort;
import io.fireflysoftware.domain.vault.core.credential.StoredCredential;
import io.fireflysoftware.domain.vault.core.credential.commands.UpdateCredentialCommand;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import reactor.core.publisher.Mono;

@CommandHandlerComponent
public class UpdateCredentialHandler extends CommandHandler<UpdateCredentialCommand, StoredCredential> {
    private final CredentialPort port;

    public UpdateCredentialHandler(CredentialPort port) {
        this.port = port;
    }

    @Override
    protected Mono<StoredCredential> doHandle(UpdateCredentialCommand command) {
        return port.update(command.id(), command.draft());
    }
}
