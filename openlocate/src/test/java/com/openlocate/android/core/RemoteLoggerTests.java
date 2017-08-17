/*
 * Copyright (c) 2017 OpenLocate
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.openlocate.android.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemoteLoggerTests {

    @Test
    public void testInfoRemoteLog() {
        // Given
        SuccessTcpClient tcpClient = new SuccessTcpClient();
        RemoteLogger logger = new RemoteLogger(tcpClient, LogLevel.INFO);

        // When
        logger.i("Test");

        // Then
        assertTrue(tcpClient.isDidWrite());
    }

    @Test
    public void testVerboseRemoteLog() {
        // Given
        SuccessTcpClient tcpClient = new SuccessTcpClient();
        RemoteLogger logger = new RemoteLogger(tcpClient, LogLevel.INFO);

        // When
        logger.v("Test");

        // Then
        assertFalse(tcpClient.isDidWrite());
    }

    @Test
    public void testWarnRemoteLog() {
        // Given
        SuccessTcpClient tcpClient = new SuccessTcpClient();
        RemoteLogger logger = new RemoteLogger(tcpClient, LogLevel.INFO);

        // When
        logger.w("Test");

        // Then
        assertTrue(tcpClient.isDidWrite());
    }

    @Test
    public void testErrorRemoteLog() {
        // Given
        SuccessTcpClient tcpClient = new SuccessTcpClient();
        RemoteLogger logger = new RemoteLogger(tcpClient, LogLevel.INFO);

        // When
        logger.e("Test");

        // Then
        assertTrue(tcpClient.isDidWrite());
    }

    @Test
    public void testNoneRemoteLog() {
        // Given
        SuccessTcpClient tcpClient = new SuccessTcpClient();
        RemoteLogger logger = new RemoteLogger(tcpClient, LogLevel.NONE);

        // When
        logger.e("Test");

        // Then
        assertFalse(tcpClient.isDidWrite());
    }

    @Test
    public void testInfoOnNoneRemoteLog() {
        // Given
        SuccessTcpClient tcpClient = new SuccessTcpClient();
        RemoteLogger logger = new RemoteLogger(tcpClient, LogLevel.NONE);

        // When
        logger.i("Test");

        // Then
        assertFalse(tcpClient.isDidWrite());
    }

    @Test
    public void testWarnOnNoneRemoteLog() {
        // Given
        SuccessTcpClient tcpClient = new SuccessTcpClient();
        RemoteLogger logger = new RemoteLogger(tcpClient, LogLevel.NONE);

        // When
        logger.w("Test");

        // Then
        assertFalse(tcpClient.isDidWrite());
    }

    @Test
    public void testVerboseOnVerboseRemoteLog() {
        // Given
        SuccessTcpClient tcpClient = new SuccessTcpClient();
        RemoteLogger logger = new RemoteLogger(tcpClient, LogLevel.VERBOSE);

        // When
        logger.v("Test");

        // Then
        assertTrue(tcpClient.isDidWrite());
    }
}
