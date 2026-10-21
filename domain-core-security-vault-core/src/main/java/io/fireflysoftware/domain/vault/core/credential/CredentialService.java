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
import io.fireflysoftware.domain.vault.core.credential.commands.LoadCredentialCommand;
import io.fireflysoftware.domain.vault.core.credential.commands.RegisterCredentialCommand;
import io.fireflysoftware.domain.vault.core.credential.commands.UpdateCredentialCommand;
import io.fireflysoftware.domain.vault.core.credential.workflows.RegisterCredentialSaga;
import io.fireflysoftware.domain.vault.core.credential.workflows.UpdateCredentialSaga;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class CredentialService {
    private final SagaEngine engine;
    private final CredentialPort port;

    public CredentialService(SagaEngine engine, CredentialPort port) {
        this.engine = engine;
        this.port = port;
    }

    public Mono<CredentialView> read(UUID id) {
        return port.read(id);
    }

    public Mono<StoredCredential> register(CredentialDTO draft) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(RegisterCredentialSaga.STEP, new RegisterCredentialCommand(draft))
                .build();
        return engine.execute(RegisterCredentialSaga.NAME, inputs)
                .flatMap(result -> outcome(result, RegisterCredentialSaga.STEP, StoredCredential.class));
    }

    public Mono<StoredCredential> update(UUID id, CredentialDTO draft) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(UpdateCredentialSaga.LOAD, new LoadCredentialCommand(id))
                .forStepId(UpdateCredentialSaga.STEP, new UpdateCredentialCommand(id, draft))
                .build();
        return engine.execute(UpdateCredentialSaga.NAME, inputs)
                .flatMap(result -> outcome(result, UpdateCredentialSaga.STEP, StoredCredential.class));
    }

    private static <T> Mono<T> outcome(SagaResult result, String stepId, Class<T> type) {
        if (result.isFailed()) {
            Throwable error = result.error().orElseGet(() ->
                    new IllegalStateException("Saga " + result.sagaName() + " failed"));
            return Mono.error(error);
        }
        return Mono.justOrEmpty(result.resultOf(stepId, type))
                .switchIfEmpty(Mono.error(new IllegalStateException("Saga step " + stepId + " produced no result")));
    }
}
