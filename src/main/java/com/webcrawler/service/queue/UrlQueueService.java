package com.webcrawler.service.queue;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The queue to store a links.
 */

@Slf4j
@Service
@Data
public class UrlQueueService {

    private int QUEUE_SIZE;
    private final BlockingQueue<String> queue;

    public UrlQueueService(@Value("${service.queue.size}") int queueSize) {
        this.QUEUE_SIZE = queueSize;
        this.queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
    }

    public void addUrls(List<String> links) {
        for (String link : links) {
            try {
                queue.put(link);
                System.out.println("Queue content: "+ Arrays.toString(queue.toArray()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while adding URL: {}", link, e);
            }
        }
    }

    public void addUrl(String link) {
        try {
            queue.put(link);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while adding URL: {}", link, e);
        }
    }

    public String takeUrl() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while taking URL", e);
            return null;
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
