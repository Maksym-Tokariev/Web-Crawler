package com.webcrawler.service.queue;

import com.webcrawler.service.CrawlerService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * The queue to store a links.
 */

@Slf4j
@Service
@Data
public class UrlQueueService {

    private final Queue<String> queue = new LinkedList<>();
    private CrawlerService crawlerService;
    private static final int QUEUE_SIZE = 10;

    public boolean isOvercrowded() {
        return queue.size() > QUEUE_SIZE;
    }

    public void addUrls(List<String> links) {
        if (links.size() <= QUEUE_SIZE) {
            queue.addAll(links);
        } else {
            log.info("List of links contain more than " + QUEUE_SIZE + " elements. " +
                    "Only the first ten links out of " + links.size() + " will be processed");
            for (int i = 0; i < QUEUE_SIZE; i++) {
                queue.add(links.get(i));
            }
        }
    }

    public void addUrl(String link) {
        queue.add(link);
    }

    public String pushUrl() {
        return queue.poll();
    }

    /**
    *  This method calls CrawlerService to handle links from the queue. If the queue is full, method stops it's work.
    **/
    public void processQueue(List<String> links) {
        addUrls(links);
        while (!queue.isEmpty()) {
            System.out.println("Queue: " + queue.stream().toList());
            String link = queue.poll();
            crawlerService.startCrawling(link);
            if (isOvercrowded()) {
                break;
            }
        }
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

}
