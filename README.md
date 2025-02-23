# WEBCRAWLER

## Description

The project is a Java web crawler using Spring Boot and the Project Reactor library. 
The crawler collects links and keywords from web pages, processes them, 
stores them in a database, and uses RabbitMQ for URL queuing. 
There is also integration with Redis for deduplication and MongoDB for data storage.

Main features:
- Parallel URL processing with rate limiting
- Robots.txt support
- URL deduplication with Redis
- Keyword extraction and lemmatization with StanfordNLP
- Saving results to MongoDB
- Task queue on RabbitMQ

## Usage Example

After startup, the crawler will:
1. Retrieve URLs from RabbitMQ
2. Check robots.txt
3. Parsing HTML and extracting links
4. Save data to MongoDB
5. Send new URLs to the queue

## Running

1. Run RabbitMQ, Redis, and MongoDB
2. Specify the start URL in `CrawlerRunner.java`.
3. Run project

## The archetype of the project

[User] -> [CrawlerRunner]
                ↓
            [RabbitMQ]
                ↓
         [CrawlerService]
                | (parallel threads)
                ├── [PageLoader] → [RobotsTxtHandler]
                ├─── [HtmlParser] → [UrlExtractor]
                ├─── [Deduplicator] (Redis)
                └── [DatabaseService] (MongoDB)

To start using it, you need to enter a start reference into the CrawlerRunner class. 
Then crawler will extract links and keywords from 
links with the efficiency that will be specified in the fields:

 WebClient configs
loader.connect.timeout 
loader.response.timeout
loader.read.timeout
loader.write.timeout
loader.max.redirects

 Services
service.max.parse.count - number of references to be processed before the process is completed
service.max.concurrency - number of references to be processed in parallel

Components:
1. CrawlerRunner - initialization, entry point
2. CrawlerService - the core of the application, controls the processing threads
3. PageLoader:
    - load page with delay
    - handling HTTP statuses and redirects
    - checking robot.txt
4. HtmlParser:
    - parsing HTML with Jsoup
    - link and text extraction
    - Redis filtering
5. UrlExtractor:
    - URL validation
    - keywords extraction
    - save content in MongoDB
6. Deduplicator - deduplication service on Redis

Technologies:
- Spring Boot 3 with WebFlux
- Project Reactor (reactive core)
- RabbitMQ (queue)
- Redis (deduplication)
- MongoDB
- Stanford CoreNLP (lemmatization)
- Jsoup (HTML parsing)
- Crawler Commons (handle robot.txt)

## Prerequisites

- Java 23+
- Reactor RabbitMQ 1.5.6 +
- MongoDB 5.0+
- Redis 6.2+

## Install
```bash
git clone https://github.com/Maksym-Tokariev/Web-Crawler.git
cd Web-Crawler
./mvnw spring-boot:run 
