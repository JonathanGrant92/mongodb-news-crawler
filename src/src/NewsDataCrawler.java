/*
 * CS3810 - Principles of Database Systems - Spring 2021
 * Instructor: Thyago Mota
 * Description: DB 04 - News Data Crawler
 * Student(s) Name(s): Jonathan Grant
 */

import com.google.gson.*;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mongodb.client.*;
import org.bson.Document;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NewsDataCrawler {

    private static final int PREEMPTIVE_BACKOFF_TIME = 2; // minutes
    private static final int PAGE_SIZE               = 10;

    private MongoClient   mongoClient;
    private MongoDatabase db;
    private String        newsAPIKey;
    private Gson          gson;

    public NewsDataCrawler() throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream("config.properties"));
        final String USER        = prop.getProperty("user");
        final String PASSWORD    = prop.getProperty("password");
        final String SERVER      = prop.getProperty("server");
        final String DATABASE    = prop.getProperty("database");
        final String CONNECT_URL = "mongodb+srv://" + USER + ":" + PASSWORD + "@" + SERVER;
        this.mongoClient = MongoClients.create(CONNECT_URL);
        this.db = mongoClient.getDatabase(DATABASE);
        this.newsAPIKey = prop.getProperty("news_api_key");
        this.gson = new GsonBuilder().create();
    }

    // TO/DO: call the news search API to retrieve news articles of the given topic, using parameters page and pageSize; insert all articles returned into a MongoDB collection named "articles"
    public void searchArticles(final String topic, int pageSize, int page) throws UnirestException {
        String url = "https://newsapi.org/v2/everything?apiKey=" + newsAPIKey +
                "&sortBy=publishedAt&q=" + topic + "&page=" + page + "&pageSize=" + pageSize;
        String body = Unirest.get(url).asString().getBody();
        MongoCollection<Document> articlesCollection = db.getCollection("articles");

        Response response = gson.fromJson(body, Response.class);
        for (int i = 0; i < pageSize; i++) {
            articlesCollection.insertOne(response.getArticles().get(i).toDocument());
        }
    }

    public void done() {
        mongoClient.close();
    }

    // TO/DO: write an application that asks the user for a topic and retrieves 100 news articles from that topic, saving them in a MongoDB database; make sure to pause for 2m between API calls to avoid having your API key being locked out
    public static void main(String[] args) throws IOException, UnirestException {
        final int PAGES_LIMIT = 10;
        final Scanner MAIN_SCANNER = new Scanner(System.in);
        final NewsDataCrawler NEWS_CRAWLER = new NewsDataCrawler();

        System.out.println("News API Service");
        System.out.print("Enter keywords of the topic you would like to search for news: ");

        String topic = MAIN_SCANNER.nextLine().replace(" ", "%20");
        MAIN_SCANNER.close();

        for (int page_index = 1; page_index <= PAGES_LIMIT; page_index++) {
            NEWS_CRAWLER.searchArticles(topic, PAGE_SIZE, page_index);
            if (page_index < PAGES_LIMIT) {
                System.out.println("Waiting two minutes before crawling page: " + (page_index + 1));
                try {
                    TimeUnit.MINUTES.sleep(PREEMPTIVE_BACKOFF_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Unirest.shutdown();
        NEWS_CRAWLER.done();
        System.out.println("A total of " + PAGES_LIMIT * PAGE_SIZE + " stories were successfully crawled from News API.");
    }

}
