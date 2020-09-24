package es.uvigo.ei.sing.facebook.utils;

import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookOAuthException;
import es.uvigo.ei.sing.facebook.controllers.AppController;
import es.uvigo.ei.sing.facebook.entities.InsightEntity;
import es.uvigo.ei.sing.facebook.entities.NodeEntity;
import es.uvigo.ei.sing.facebook.entities.PageEntity;
import es.uvigo.ei.sing.facebook.services.InsightService;
import es.uvigo.ei.sing.facebook.services.NodeService;
import es.uvigo.ei.sing.facebook.services.PageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Configuration
public class Scheduler {

    private final AppController appController;
    private final InsightService insightService;
    private final PageService pageService;
    private final NodeService nodeService;

    @Autowired
    public Scheduler(AppController appController, InsightService insightService, PageService pageService, NodeService nodeService) {
        this.appController = appController;
        this.insightService = insightService;
        this.pageService = pageService;
        this.nodeService = nodeService;
    }

    // Retry the method every hour (app rate limit refresh)
    @Retryable(
            include = {FacebookOAuthException.class},
            maxAttempts = 23,
            backoff = @Backoff(delay = 3600000))
    // Execute at the beginning of each day
    @Scheduled(cron = "0 0 0 * * *")
    @SuppressWarnings("Duplicates")
    public void runController() {
        log.info("Starting the retrieval of information...");

        try {
            // Get the information of the page
            PageEntity pageEntity = appController.getPage();
            String pageExternalId = pageEntity.getExternalId();

            // Get the recent insight (page insights are retrieved from older to newer)
            Optional<InsightEntity> savedPageInsight = insightService.findMostRecentInsight(pageExternalId);
            LocalDateTime startDay;
            if (savedPageInsight.isPresent())
                startDay = savedPageInsight.get().getCreated();
            else
                startDay = LocalDateTime.now().minusYears(2);
            // This method should be executed the first time with the maximum allowed starting date to retrieve all possible information
            // Then it should only get the insights from yesterday
            appController.getPageInsights(startDay, pageEntity.getExternalId());

            // Retrieve posts from page feed
            appController.getPostsFromPage(pageEntity);
            Set<NodeEntity> posts;
            if (!pageEntity.isParsed()) {
                // If page is not parsed, retrieve not parsed posts
                posts = nodeService.findNotParsedPosts(pageExternalId);
            } else {
                // If page is parsed, get insights and comments only for posts with keep_updating to true
                // Get also the non parsed nodes that may be left
                posts = nodeService.findByKeepUpdatingAndNotParsedPosts(pageEntity.getExternalId());
            }
            posts.forEach(post -> {
                boolean parsed = true;

                try {
                    appController.getPostInsights(post.getExternalId());
                } catch (FacebookNetworkException e) {
                    log.error("Cannot retrieve comments from video {}. See error: {}", post.getExternalId(), e);
                    parsed = false;
                } catch (Exception e) {
                    log.error("Cannot retrieve comments from video {}. See error: {}", post.getExternalId(), e);
                }

                try {
                    appController.getCommentsFromNode(post);
                } catch (FacebookNetworkException e) {
                    log.error("Cannot retrieve comments from video {}. See error: {}", post.getExternalId(), e);
                    parsed = false;
                } catch (Exception e) {
                    log.error("Cannot retrieve comments from video {}. See error: {}", post.getExternalId(), e);
                }

                if (!post.isParsed()) {
                    // Save post with parsed true
                    post.setParsed(parsed);
                    nodeService.save(post);
                }
            });

            // Retrieve videos from page
            appController.getVideosFromPage(pageEntity);
            Set<NodeEntity> videos;
            if (!pageEntity.isParsed()) {
                // If page is not parsed, retrieve not parsed videos
                videos = nodeService.findNotParsedVideos(pageExternalId);
            } else {
                // If page is parsed, get insights and comments only for videos with keep_updating to true
                // Get also the non parsed nodes that may be left
                videos = nodeService.findByKeepUpdatingVideosAndNotParsed(pageEntity.getExternalId());
            }
            videos.forEach(video -> {
                boolean parsed = true;

                try {
                    appController.getVideoInsights(video.getExternalId());
                } catch (FacebookNetworkException e) {
                    log.error("Cannot retrieve comments from video {}. See error: {}", video.getExternalId(), e);
                    parsed = false;
                } catch (Exception e) {
                    log.error("Cannot retrieve comments from video {}. See error: {}", video.getExternalId(), e);
                }

                try {
                    appController.getCommentsFromNode(video);
                } catch (FacebookNetworkException e) {
                    log.error("Cannot retrieve comments from video {}. See error: {}", video.getExternalId(), e);
                    parsed = false;
                } catch (Exception e) {
                    log.error("Cannot retrieve comments from video {}. See error: {}", video.getExternalId(), e);
                }

                if (!video.isParsed()) {
                    // Save video with parsed true
                    video.setParsed(parsed);
                    nodeService.save(video);
                }
            });

            if (!pageEntity.isParsed()) {
                // Set page parsed to true
                pageEntity.setParsed(true);
                pageService.save(pageEntity);
            }
        } catch (FacebookOAuthException e) {
            // TODO: 27/05/2020 Handle errors based on Facebook error codes = https://developers.facebook.com/docs/graph-api/using-graph-api/error-handling/?locale=es_ES
            // TODO: 26/05/2020 Put this in a while(finish) and set a sleep time based on the error code of this exception (remove @retry)
            log.error("Application request limit reached, waiting one hour... FacebookOAuthException: {}", e);
            throw e;
        }

        log.info("Finishing the retrieval of information...");
    }
}
