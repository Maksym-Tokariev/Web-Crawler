package com.webcrawler.service.loader;

import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/*
* Processes the robot.txt file and method isAllowed returns permission to parse the site.
*/

@Slf4j
@Component
@Data
public class RobotsTxtHandler {

    private Map<String, SimpleRobotRules> robotsCache = new HashMap<>();
    private final String userAgent = "MyWebCrawler";

    public boolean isAllowed(String url) {
        try {
          URL urlObj = new URL(url);
          String host = urlObj.getHost();
          String robotsProtocol = urlObj.getProtocol() + "://" + host + "/robots.txt";

            SimpleRobotRules rules = robotsCache.get(host);
            if (rules == null) {
                byte[] content = WebClient.create()
                        .get()
                        .uri(robotsProtocol)
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block();

                SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
                rules = parser.parseContent(robotsProtocol, content, "text/plain", userAgent);

                robotsCache.put(host, rules);
            }
            return rules.isAllowed(url);
        } catch (Exception e) {
            log.error("Error while checking robots.txt: {}", e.getMessage(), e);
            return true;
        }
    }
}
