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
import io.fireflysoftware.domain.vault.core.credential.commands.LoadCredentialCommand;
import io.fireflysoftware.domain.vault.core.credential.commands.UpdateCredentialCommand;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** Updates the record and writes the previous body back if a later step fails. */
@Saga(name = UpdateCredentialSaga.NAME)
@Service
public class UpdateCredentialSaga {
    public static final String NAME = "UpdateCredentialSaga";
    public static final String LOAD = "load";
    public static final String STEP = "update";
    public static final String COMPENSATE = "restore";

    private final CommandBus commandBus;

    public UpdateCredentialSaga(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @SagaStep(id = LOAD)
    @StepEvent(type = "vault.credential.loaded")
    public Mono<StoredCredential> load(LoadCredentialCommand command, ExecutionContext ctx) {
        return commandBus.<StoredCredential>send(command)
                .doOnNext(current -> ctx.putVariable("previous", current));
    }

    @SagaStep(id = STEP, dependsOn = LOAD, compensate = COMPENSATE)
    @StepEvent(type = "vault.credential.updated")
    public Mono<StoredCredential> update(UpdateCredentialCommand command, ExecutionContext ctx) {
        return commandBus.send(command);
    }

    public Mono<Void> restore(StoredCredential updated, ExecutionContext ctx) {
        StoredCredential previous = ctx.getVariableAs("previous", StoredCredential.class);
        if (previous == null || previous.id() == null || previous.body() == null) {
            return Mono.empty();
        }
        return commandBus.send(new UpdateCredentialCommand(previous.id(), previous.body())).then();
    }
}
