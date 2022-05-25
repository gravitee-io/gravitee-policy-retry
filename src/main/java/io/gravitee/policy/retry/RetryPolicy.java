/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.retry;

import io.gravitee.common.http.HttpHeadersValues;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Invoker;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.context.MutableExecutionContext;
import io.gravitee.gateway.api.el.EvaluableResponse;
import io.gravitee.gateway.api.handler.Handler;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.api.proxy.ProxyConnection;
import io.gravitee.gateway.api.proxy.ProxyResponse;
import io.gravitee.gateway.api.stream.ReadStream;
import io.gravitee.gateway.api.stream.WriteStream;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.retry.configuration.RetryPolicyConfiguration;
import io.gravitee.policy.retry.el.ProxyResponseWrapper;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RetryPolicy {

    private final RetryPolicyConfiguration configuration;

    private static final String CIRCUIT_BREAKER_NAME = "retry-policy";
    private static final String TEMPLATE_RESPONSE_VARIABLE = "response";

    public RetryPolicy(RetryPolicyConfiguration configuration) {
        this.configuration = configuration;
    }

    @OnRequest
    public void onRequest(ExecutionContext context, PolicyChain chain) {
        final Invoker defaultInvoker = (Invoker) context.getAttribute(ExecutionContext.ATTR_INVOKER);
        context.setAttribute(ExecutionContext.ATTR_INVOKER, new RetryInvoker(defaultInvoker, configuration));

        ((MutableExecutionContext) context).request(new RetryRequest(context.request()));
        chain.doNext(context.request(), context.response());
    }

    public static class RetryInvoker implements Invoker {

        private final Invoker invoker;
        private final RetryPolicyConfiguration configuration;

        RetryInvoker(final Invoker invoker, final RetryPolicyConfiguration configuration) {
            this.invoker = invoker;
            this.configuration = configuration;
        }

        @Override
        public void invoke(ExecutionContext context, ReadStream<Buffer> readStream, Handler<ProxyConnection> handler) {
            Vertx vertx = context.getComponent(Vertx.class);

            CircuitBreaker circuitBreaker = CircuitBreaker.create(
                CIRCUIT_BREAKER_NAME,
                vertx,
                new CircuitBreakerOptions()
                    .setMaxRetries(configuration.getMaxRetries()) // number of failure before opening the circuit
                    .setTimeout(configuration.getTimeout()) // consider a failure if the operation does not succeed in time
                    .setNotificationAddress(null)
                    .setNotificationPeriod(0)
            );

            if (configuration.getDelay() > 0) {
                circuitBreaker.retryPolicy(integer -> configuration.getDelay());
            }

            // The circuit breaker is used for the first real call to the backend, before the retries.
            // Setting it to -1 prevent to count this first call as a retry.
            final AtomicInteger counter = new AtomicInteger(-1);
            final AtomicReference<ProxyResponse> proxyResponseRef = new AtomicReference<>();

            circuitBreaker.execute(
                event -> {
                    counter.incrementAndGet();

                    // Listen for the response from backend
                    invoker.invoke(
                        context,
                        readStream,
                        connection -> {
                            connection
                                .exceptionHandler(event::fail)
                                .responseHandler(proxyResponse -> {
                                    // cancel the previous proxyResponse if it exists
                                    cancelProxyResponse(proxyResponseRef.getAndSet(proxyResponse));
                                    context
                                        .getTemplateEngine()
                                        .getTemplateContext()
                                        .setVariable(
                                            TEMPLATE_RESPONSE_VARIABLE,
                                            // Note: we must create a EvaluableResponse and a ProxyResponseWrapper to make sure classloader will be well released when the api is undeployed.
                                            new EvaluableResponse(new ProxyResponseWrapper(proxyResponse))
                                        );
                                    boolean retry = context.getTemplateEngine().getValue(configuration.getCondition(), boolean.class);

                                    if (retry) {
                                        if (configuration.isLastResponse() && counter.get() == configuration.getMaxRetries()) {
                                            event.complete(new RetryProxyConnection(connection, proxyResponse));
                                        } else {
                                            // Cleanup by cancelling the proxyResponse (request tracker, ...).
                                            proxyResponse.cancel();
                                            event.fail("");
                                        }
                                    } else {
                                        event.complete(new RetryProxyConnection(connection, proxyResponse));
                                    }
                                });
                        }
                    );
                },
                (io.vertx.core.Handler<AsyncResult<RetryProxyConnection>>) event -> {
                    circuitBreaker.close();

                    if (event.succeeded()) {
                        RetryProxyConnection connection = event.result();
                        handler.handle(connection);
                        connection.sendResponse();
                    } else {
                        // failure: we have to cancel the proxyResponse if it's in timeout.
                        cancelProxyResponse(proxyResponseRef.get());
                        DirectProxyConnection connection = new DirectProxyConnection(HttpStatusCode.BAD_GATEWAY_502);
                        handler.handle(connection);
                        connection.sendResponse();
                    }
                }
            );
        }

        private void cancelProxyResponse(ProxyResponse previous) {
            if (previous != null) {
                previous.cancel();
            }
        }
    }

    public static class RetryProxyConnection implements ProxyConnection {

        private final ProxyConnection wrapped;
        private final ProxyResponse response;
        private Handler<ProxyResponse> responseHandler;

        RetryProxyConnection(ProxyConnection connection, ProxyResponse response) {
            this.wrapped = connection;
            this.response = response;
        }

        @Override
        public ProxyConnection cancel() {
            return wrapped.cancel();
        }

        @Override
        public ProxyConnection exceptionHandler(Handler<Throwable> exceptionHandler) {
            return wrapped.exceptionHandler(exceptionHandler);
        }

        @Override
        public ProxyConnection responseHandler(Handler<ProxyResponse> responseHandler) {
            this.responseHandler = responseHandler;
            return this;
        }

        @Override
        public WriteStream<Buffer> write(Buffer buffer) {
            return wrapped.write(buffer);
        }

        @Override
        public void end() {
            wrapped.end();
        }

        @Override
        public void end(Buffer buffer) {
            wrapped.end(buffer);
        }

        @Override
        public WriteStream<Buffer> drainHandler(Handler<Void> drainHandler) {
            return wrapped.drainHandler(drainHandler);
        }

        @Override
        public boolean writeQueueFull() {
            return wrapped.writeQueueFull();
        }

        void sendResponse() {
            this.responseHandler.handle(response);
        }
    }

    public static class DirectProxyConnection implements ProxyConnection {

        private Handler<ProxyResponse> responseHandler;

        private final ProxyResponse response;

        DirectProxyConnection(int statusCode) {
            this.response = new EmptyProxyResponse(statusCode);
        }

        @Override
        public WriteStream<Buffer> write(Buffer content) {
            throw new IllegalStateException();
        }

        @Override
        public void end() {
            // Nothing to do here...
        }

        @Override
        public ProxyConnection responseHandler(Handler<ProxyResponse> responseHandler) {
            this.responseHandler = responseHandler;
            return this;
        }

        void sendResponse() {
            this.responseHandler.handle(response);
        }
    }

    public static class EmptyProxyResponse implements ProxyResponse {

        private Handler<Buffer> bodyHandler;
        private Handler<Void> endHandler;

        private final HttpHeaders httpHeaders = HttpHeaders.create();

        private final int statusCode;

        EmptyProxyResponse(int statusCode) {
            this.statusCode = statusCode;

            httpHeaders.set(HttpHeaderNames.CONNECTION, HttpHeadersValues.CONNECTION_CLOSE);
        }

        @Override
        public int status() {
            return statusCode;
        }

        @Override
        public HttpHeaders headers() {
            return httpHeaders;
        }

        @Override
        public ProxyResponse bodyHandler(Handler<Buffer> bodyHandler) {
            this.bodyHandler = bodyHandler;
            return this;
        }

        @Override
        public ProxyResponse endHandler(Handler<Void> endHandler) {
            this.endHandler = endHandler;
            return this;
        }

        @Override
        public ReadStream<Buffer> resume() {
            endHandler.handle(null);
            return this;
        }

        @Override
        public boolean connected() {
            return false;
        }
    }
}
