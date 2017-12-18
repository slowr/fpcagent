/*
 * Copyright 2017-present Open Networking Foundation
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

package org.onosproject.fpcagent.workers;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class HTTPNotifier implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(HTTPNotifier.class);
    private static HTTPNotifier _instance = null;
    private final BlockingQueue<Map.Entry<String, String>> blockingQueue;
    private boolean run;

    protected HTTPNotifier() {
        this.run = true;
        this.blockingQueue = new LinkedBlockingQueue<>();
    }

    public static HTTPNotifier getInstance() {
        if (_instance == null) {
            _instance = new HTTPNotifier();
        }
        return _instance;
    }

    public void send(Map.Entry<String, String> buf) {
        try {
            blockingQueue.put(buf);
        } catch (InterruptedException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    public void open() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            while ((!Thread.currentThread().isInterrupted()) && run) {
                try {
                    Map.Entry<String, String> entry = blockingQueue.take();

                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost httpPost = new HttpPost(entry.getKey());
                    httpPost.addHeader("User-Agent", "ONOS Notification Agent");
                    httpPost.addHeader("Charset", "utf-8");
                    httpPost.addHeader("Content-type", "application/json");
                    StringEntity params = new StringEntity(entry.getValue());
                    httpPost.setEntity(params);
                    HttpResponse response = client.execute(httpPost);

                    log.info("Response {}", response);
                } catch (Exception e) {
                    log.error(ExceptionUtils.getFullStackTrace(e));
                }
            }
        });
    }

    @Override
    public void close() {
        run = false;
    }
}
