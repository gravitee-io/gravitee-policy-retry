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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.handler.Handler;
import io.gravitee.gateway.api.stream.ReadStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetryReadStreamTest {

    private FakeReadStream originalStream;
    private RetryReadStream retryStream;

    @BeforeEach
    void setUp() {
        originalStream = new FakeReadStream();
        retryStream = new RetryReadStream(originalStream);
    }

    @Test
    void shouldBufferAndReplayBody() {
        AtomicReference<Buffer> received = new AtomicReference<>();
        AtomicBoolean endCalled = new AtomicBoolean(false);

        retryStream.bodyHandler(received::set);
        retryStream.endHandler(v -> endCalled.set(true));

        Buffer chunk = Buffer.buffer("test-body");
        originalStream.emit(chunk);
        originalStream.complete();

        assertTrue(endCalled.get(), "End handler should be triggered");
        assertEquals("test-body", received.get().toString(), "Body should match");
    }

    static class FakeReadStream implements ReadStream<Buffer> {

        private Handler<Buffer> bodyHandler;
        private Handler<Void> endHandler;

        @Override
        public ReadStream<Buffer> bodyHandler(Handler<Buffer> handler) {
            this.bodyHandler = handler;
            return this;
        }

        @Override
        public ReadStream<Buffer> endHandler(Handler<Void> handler) {
            this.endHandler = handler;
            return this;
        }

        @Override
        public ReadStream<Buffer> pause() {
            return this;
        }

        @Override
        public ReadStream<Buffer> resume() {
            return this;
        }

        void emit(Buffer chunk) {
            if (bodyHandler != null) bodyHandler.handle(chunk);
        }

        void complete() {
            if (endHandler != null) endHandler.handle(null);
        }
    }
}
