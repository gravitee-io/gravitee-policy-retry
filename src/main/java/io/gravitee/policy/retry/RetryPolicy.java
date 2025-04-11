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
import static io.gravitee.gateway.reactive.api.context.InternalContextAttributes.ATTR_INTERNAL_EXECUTION_FAILURE;

import io.gravitee.common.utils.RxHelper;
import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.gravitee.gateway.reactive.api.context.InternalContextAttributes;
import io.gravitee.gateway.reactive.api.context.http.HttpExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainExecutionContext;
import io.gravitee.gateway.reactive.api.invoker.HttpInvoker;
import io.gravitee.gateway.reactive.api.policy.http.HttpPolicy;
import io.gravitee.policy.retry.configuration.RetryPolicyConfiguration;
import io.reactivex.rxjava3.core.Completable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryPolicy extends RetryPolicyV3 implements HttpPolicy {

    private static final long DEFAULT_TIMEOUT = 1000L;
    private static final int DEFAULT_MAX_RETRIES = 1;

    public RetryPolicy(final RetryPolicyConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String id() {
        return "retry";
    }

    @Override
    public Completable onRequest(HttpPlainExecutionContext ctx) {
        return Completable.defer(() -> {
            HttpInvoker defaultInvoker = ctx.getInternalAttribute(InternalContextAttributes.ATTR_INTERNAL_INVOKER);
            HttpInvoker retryInvoker = new RetryInvoker(defaultInvoker, configuration);
            ctx.setInternalAttribute(InternalContextAttributes.ATTR_INTERNAL_INVOKER, retryInvoker);

            return Completable.complete();
        });
    }

    static class RetryInvoker implements HttpInvoker {

        private final HttpInvoker invoker;
        private final RetryPolicyConfiguration configuration;

        public RetryInvoker(HttpInvoker invoker, RetryPolicyConfiguration configuration) {
            this.invoker = invoker;
            this.configuration = configuration;
        }

        @Override
        public String getId() {
            return "retry-invoker";
        }

        @Override
        public Completable invoke(HttpExecutionContext ctx) {
            final int maxRetries = configuration.getMaxRetries() == 0 ? DEFAULT_MAX_RETRIES : configuration.getMaxRetries();
            final var timeout = configuration.getTimeout() == 0 ? DEFAULT_TIMEOUT : configuration.getTimeout();
            final long delay = configuration.getDelay();
            final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

            // Setting it to -1 prevent counting this first call as a retry.
            final AtomicInteger counter = new AtomicInteger(-1);

            return Completable
                .defer(() -> {
                    counter.incrementAndGet();
                    var originalEndpoint = ctx.getAttribute(ATTR_REQUEST_ENDPOINT);
                    // EndpointInvoker overrides the request endpoint. We need to set it back to the original state to retry properly
                    ctx.setAttribute(ATTR_REQUEST_ENDPOINT, originalEndpoint);
                    // Entrypoint connectors skip response handling if there is an error. In the case of a retry, we need to reset the failure.
                    ctx.removeInternalAttribute(ATTR_INTERNAL_EXECUTION_FAILURE);
                    // Consume the body and ignore it. Consuming it with .body() method internally enables caching of chunks, which is mandatory to retry the request in case of failure.
                    return ctx.request().body().ignoreElement().andThen(invoker.invoke(ctx));
                })
                .andThen(
                    Completable.defer(() -> {
                        ctx.getTemplateEngine().getTemplateContext().setVariable(TEMPLATE_RESPONSE_VARIABLE, ctx.response());
                        return ctx
                            .getTemplateEngine()
                            .eval(configuration.getCondition(), Boolean.class)
                            .flatMapCompletable(retry -> {
                                if (retry && (!configuration.isLastResponse() || counter.get() != maxRetries)) {
                                    return Completable.error(new RetryPolicy.RetryNeededException("Retry needed"));
                                }
                                return Completable.complete();
                            });
                    })
                )
                .timeout(timeout, timeUnit)
                .compose(RxHelper.retryCompletable(maxRetries, (int) delay, timeUnit))
                .onErrorResumeNext(t -> {
                    log.info("Retry failed after {} retries", configuration.getMaxRetries());
                    return ctx.interruptWith(new ExecutionFailure(502));
                });
        }
    }

    static class RetryNeededException extends RuntimeException {

        public RetryNeededException(String message) {
            super(message);
        }
    }
}
