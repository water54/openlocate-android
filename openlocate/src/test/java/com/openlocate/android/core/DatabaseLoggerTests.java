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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DatabaseLoggerTests {

    private class LogList implements LoggerDataSource {

        private List<LogType> list;

        LogList() {
            list = new ArrayList<>();
        }

        @Override
        public void add(LogType log) {
            list.add(log);
        }

        @Override
        public List<LogType> popAll() {
            List<LogType> popped = new ArrayList<>();

            for (LogType log: list) {
                popped.add(log);
            }

            list.clear();
            return popped;
        }

        @Override
        public long size() {
            return list.size();
        }
    }

    @Test
    public void testAddErrorLogInDatabaseLogger() {
        // Given
        LoggerDataSource dataSource = new LogList();
        DatabaseLogger databaseLogger = new DatabaseLogger(dataSource, LogLevel.INFO);

        // When
        databaseLogger.e("Hi");

        // Then
        assertEquals(dataSource.size(), 1);
    }

    @Test
    public void testAddVerboseLogInDatabaseLogger() {
        // Given
        LoggerDataSource dataSource = new LogList();
        DatabaseLogger databaseLogger = new DatabaseLogger(dataSource, LogLevel.INFO);

        // When
        databaseLogger.v("Hi");

        // Then
        assertEquals(dataSource.size(), 0);
    }

}
