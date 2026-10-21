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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SdkCredentialAdapterTest {
    @Test
    void sandboxIsTheDefaultAndDoesNotOpenASocket() {
        new ApplicationContextRunner()
                .withUserConfiguration(CredentialAdapterConfiguration.class)
                .run(context -> {
                    CredentialPort port = context.getBean(CredentialPort.class);
                    assertThat(port).isInstanceOf(SandboxCredentialAdapter.class);
                    assertThat(port.read(null).block().label()).isEqualTo("primary");
                });
    }

    @Test
    void corePropertySelectsTheSdkAdapter() {
        new ApplicationContextRunner()
                .withUserConfiguration(CredentialAdapterConfiguration.class, io.fireflysoftware.domain.vault.infra.CredentialClientFactory.class)
                .withPropertyValues("firefly.vault.credential.adapter=core")
                .run(context -> assertThat(context.getBean(CredentialPort.class)).isInstanceOf(SdkCredentialAdapter.class));
    }

    @Test
    void liveReadCallsTheCoreSdk() {
        UUID id = UUID.randomUUID();
        CredentialsApi api = mock(CredentialsApi.class);
        CredentialDTO dto = mock(CredentialDTO.class);
        when(dto.getId()).thenReturn(id);
        when(api.getCredentialById(id, null)).thenReturn(Mono.just(dto));
        CredentialView view = new SdkCredentialAdapter(api).read(id).block();
        assertThat(view.id()).isEqualTo(id);
        verify(api).getCredentialById(id, null);
    }
}
