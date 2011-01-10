/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public class SubmitOrderedCompletionServiceTest extends TestCase {

    private ExecutorService executor;
    private SubmitOrderedCompletionService<Object> service;

    @Override
    protected void setUp() throws Exception {
        executor = Executors.newFixedThreadPool(5);
        service = new SubmitOrderedCompletionService<Object>(executor);
    }

    @Override
    protected void tearDown() throws Exception {
        executor.shutdownNow();
    }

    public void testSubmitOrdered() throws Exception {

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                return "A";
            }
        });

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                return "B";
            }
        });

        Object a = service.take().get();
        Object b = service.take().get();

        assertEquals("A", a);
        assertEquals("B", b);
    }

    public void testSubmitOrderedFirstTaskIsSlow() throws Exception {

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                // this task should be slower than B but we should still get it first
                Thread.sleep(200);
                return "A";
            }
        });

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                return "B";
            }
        });

        Object a = service.take().get();
        Object b = service.take().get();

        assertEquals("A", a);
        assertEquals("B", b);
    }

    public void testSubmitOrderedFirstTaskIsSlowUsingPollTimeout() throws Exception {

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                // this task should be slower than B but we should still get it first
                Thread.sleep(200);
                return "A";
            }
        });

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                return "B";
            }
        });

        Object a = service.poll(5, TimeUnit.SECONDS).get();
        Object b = service.poll(5, TimeUnit.SECONDS).get();

        assertEquals("A", a);
        assertEquals("B", b);
    }

    public void testSubmitOrderedFirstTaskIsSlowUsingPoll() throws Exception {

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                // this task should be slower than B but we should still get it first
                Thread.sleep(1000);
                return "A";
            }
        });

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                return "B";
            }
        });

        // poll should not get it the first time
        Object a = service.poll();
        assertNull(a);

        Thread.sleep(100);

        // and neither the 2nd time
        a = service.poll();
        assertNull(a);

        // okay take them
        a = service.take().get();
        Object b = service.take().get();

        assertEquals("A", a);
        assertEquals("B", b);
    }

    public void testSubmitOrderedSecondTaskIsSlow() throws Exception {

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                return "A";
            }
        });

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                Thread.sleep(200);
                return "B";
            }
        });

        Object a = service.take().get();
        Object b = service.take().get();

        assertEquals("A", a);
        assertEquals("B", b);
    }

    public void testSubmitOrderedSecondTaskIsSlowUsingPollTimeout() throws Exception {

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                return "A";
            }
        });

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                Thread.sleep(200);
                return "B";
            }
        });

        Object a = service.poll(5, TimeUnit.SECONDS).get();
        Object b = service.poll(5, TimeUnit.SECONDS).get();

        assertEquals("A", a);
        assertEquals("B", b);
    }

    public void testSubmitOrderedLastTaskIsSlowUsingPoll() throws Exception {

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                return "A";
            }
        });

        service.submit(new Callable<Object>() {
            public Object call() throws Exception {
                Thread.sleep(1000);
                return "B";
            }
        });

        // take a
        Object a = service.take().get();
        assertNotNull(a);

        // poll should not get it the first time
        Object b = service.poll();
        assertNull(b);

        Thread.sleep(100);

        // and neither the 2nd time
        b = service.poll();
        assertNull(b);

        // okay take it
        b = service.take().get();

        assertEquals("A", a);
        assertEquals("B", b);
    }

}
