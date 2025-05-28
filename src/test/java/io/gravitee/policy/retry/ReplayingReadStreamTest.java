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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.handler.Handler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplayingReadStreamTest {

    private ReplayingReadStream replayingReadStream;
    private Buffer buffer;

    @BeforeEach
    void setUp() {
        buffer = Buffer.buffer("test-data");
        replayingReadStream = new ReplayingReadStream(buffer, true);
    }

    @Test
    void testBodyHandlerIsCalledWithBufferedData() {
        Handler<Buffer> bodyHandler = mock(Handler.class);
        replayingReadStream.bodyHandler(bodyHandler);
        verify(bodyHandler).handle(buffer);
    }

    @Test
    void testEndHandlerIsCalled() {
        Handler<Void> endHandler = mock(Handler.class);
        replayingReadStream.endHandler(endHandler);
        verify(endHandler).handle(null);
    }

    @Test
    void testMultipleChunksReplayedCorrectly() {
        Buffer combined = Buffer.buffer();
        combined.appendBuffer(Buffer.buffer("chunk1"));
        combined.appendBuffer(Buffer.buffer("chunk2"));
        replayingReadStream = new ReplayingReadStream(combined, true);

        Handler<Buffer> bodyHandler = mock(Handler.class);
        replayingReadStream.bodyHandler(bodyHandler);
        verify(bodyHandler).handle(combined);
    }
}
