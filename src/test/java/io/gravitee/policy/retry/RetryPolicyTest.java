/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
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
package io.gravitee.policy.retry;

import static io.gravitee.gateway.reactive.api.context.ContextAttributes.ATTR_REQUEST_ENDPOINT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.gravitee.gateway.reactive.api.context.http.HttpExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpRequest;
import io.gravitee.gateway.reactive.api.invoker.HttpInvoker;
import io.gravitee.policy.retry.configuration.RetryPolicyConfiguration;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RetryPolicyTest {

    private static final String ORIGINAL_ENDPOINT = "bs:/path";
    private static final String STRIPPED_ENDPOINT = "/path";

    private HttpExecutionContext ctx;
    private HttpRequest request;
    private HttpInvoker delegate;
    private RetryPolicyConfiguration configuration;

    @BeforeEach
    void setUp() {
        ctx = mock(HttpExecutionContext.class);
        request = mock(HttpRequest.class);
        delegate = mock(HttpInvoker.class);

        configuration = new RetryPolicyConfiguration();
        configuration.setMaxRetries(2);
        configuration.setTimeout(10_000L);
        configuration.setDelay(0L);

        when(ctx.request()).thenReturn(request);
        when(request.body()).thenReturn(Maybe.empty());
        when(ctx.interruptWith(any())).thenReturn(Completable.complete());
    }

    @Test
    void should_restore_original_endpoint_attribute_on_each_retry_when_invoker_mutates_it() {
        // Stub get/set to round-trip through `stored` so the mock reflects state mutations.
        AtomicReference<String> stored = new AtomicReference<>(ORIGINAL_ENDPOINT);
        when(ctx.<String>getAttribute(ATTR_REQUEST_ENDPOINT)).thenAnswer(inv -> stored.get());
        doAnswer(inv -> {
                stored.set(inv.getArgument(1));
                return null;
            })
            .when(ctx)
            .setAttribute(eq(ATTR_REQUEST_ENDPOINT), any());

        // Delegate mimics HttpEndpointInvoker: strips the prefix from the attribute, then errors to trigger a retry.
        when(delegate.invoke(ctx))
            .thenAnswer(inv -> {
                ctx.setAttribute(ATTR_REQUEST_ENDPOINT, STRIPPED_ENDPOINT);
                return Completable.error(new RuntimeException("simulated upstream failure"));
            });

        new RetryPolicy.RetryInvoker(delegate, configuration).invoke(ctx).test().awaitDone(5, TimeUnit.SECONDS);

        // Each attempt must restore the original value before invoking the delegate; otherwise a later attempt reads the stripped value back.
        // InOrder guards against a refactor that invokes the delegate before restoring the attribute.
        InOrder inOrder = inOrder(ctx, delegate);
        for (int attempt = 1; attempt <= 3; attempt++) {
            inOrder.verify(ctx).setAttribute(eq(ATTR_REQUEST_ENDPOINT), eq(ORIGINAL_ENDPOINT));
            inOrder.verify(delegate).invoke(ctx);
        }
    }
}
