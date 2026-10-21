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

package io.fireflysoftware.domain.vault.core.credential.workflows;

import io.fireflysoftware.domain.vault.core.credential.StoredCredential;
import io.fireflysoftware.domain.vault.core.credential.commands.RegisterCredentialCommand;
import io.fireflysoftware.domain.vault.core.credential.commands.RemoveCredentialCommand;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** Creates the record and deletes it if a later step fails. */
@Saga(name = RegisterCredentialSaga.NAME)
@Service
public class RegisterCredentialSaga {
    public static final String NAME = "RegisterCredentialSaga";
    public static final String STEP = "register";
    public static final String COMPENSATE = "remove";

    private final CommandBus commandBus;

    public RegisterCredentialSaga(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @SagaStep(id = STEP, compensate = COMPENSATE)
    @StepEvent(type = "vault.credential.registered")
    public Mono<StoredCredential> register(RegisterCredentialCommand command, ExecutionContext ctx) {
        return commandBus.<StoredCredential>send(command)
                .doOnNext(created -> ctx.putVariable("created", created));
    }

    public Mono<Void> remove(StoredCredential created) {
        if (created == null || created.id() == null) {
            return Mono.empty();
        }
        return commandBus.send(new RemoveCredentialCommand(created.id()));
    }
}
