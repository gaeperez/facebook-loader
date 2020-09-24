package es.uvigo.ei.sing.facebook.controllers;

import com.restfb.*;
import com.restfb.json.JsonObject;
import com.restfb.types.*;
import es.uvigo.ei.sing.facebook.entities.*;
import es.uvigo.ei.sing.facebook.services.*;
import es.uvigo.ei.sing.facebook.utils.FacebookConfiguration;
import es.uvigo.ei.sing.facebook.utils.Functions;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.lang.Thread;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Log4j2
@Controller
public class AppController {

    private final FacebookConfiguration fbConf;
    private final CategoryListService categoryListService;
    private final EmailService emailService;
    private final InsightService insightService;
    private final CommentService commentService;
    private final PageService pageService;
    private final NodeService nodeService;
    private final PlaceService placeService;

    // Private controller variables
    private FacebookClient facebookClient;

    @Autowired
    public AppController(FacebookConfiguration fbConf, CategoryListService categoryListService, EmailService emailService, InsightService insightService, CommentService commentService, PageService pageService, NodeService nodeService, PlaceService placeService) {
        this.fbConf = fbConf;
        this.categoryListService = categoryListService;
        this.emailService = emailService;
        this.insightService = insightService;
        this.commentService = commentService;
        this.pageService = pageService;
        this.nodeService = nodeService;
        this.placeService = placeService;
    }

    @PostConstruct
    public void initialize() {
        // Create the facebook client. The never expiring page_access_token must be generated manually from the
        // Facebook online tools (https://developers.facebook.com/docs/marketing-api/access/)
        this.facebookClient = new DefaultFacebookClient(fbConf.getPageAccessToken(), Version.VERSION_7_0);
    }

    // https://developers.facebook.com/docs/graph-api/reference/page/
    public PageEntity getPage() {
        log.info("Requesting page information to Facebook...");

        // Check if the page is already inserted
        Optional<PageEntity> possiblePage = pageService.findByExternalId(fbConf.getPageId());

        PageEntity pageEntity;
        if (!possiblePage.isPresent()) {
            log.info("Parsing page information...");
            // Create facebook request
            Page page = facebookClient.fetchObject(fbConf.getPageId(), Page.class,
                    Parameter.with("fields", "id,about,checkins,category,category_list,description,emails," +
                            "engagement,general_info,hours,impressum,link,name,phone,website,single_line_address," +
                            "overall_star_rating,products,price_range"));

            String externalId = page.getId();
            pageEntity = new PageEntity();
            pageEntity.setExternalId(externalId);
            pageEntity.setName(page.getName());
            pageEntity.setLink(page.getLink());
            pageEntity.setAbout(page.getAbout());
            pageEntity.setCheckins(page.getCheckins());
            pageEntity.setCategory(page.getCategory());
            pageEntity.setDescription(page.getDescription());
            pageEntity.setEngagement(page.getEngagement().getCount());
            pageEntity.setGeneralInfo(page.getGeneralInfo());
            pageEntity.setImpressum(page.getImpressum());
            pageEntity.setPhone(page.getPhone());
            pageEntity.setWebsite(page.getWebsite());
            pageEntity.setSingleLineAddress(page.getSingleLineAddress());
            pageEntity.setProducts(page.getProducts());
            pageEntity.setPriceRange(page.getPriceRange());
            Double overallStarRating = page.getOverallStarRating();
            if (overallStarRating != null)
                pageEntity.setOverallStarRating(overallStarRating);

            // Set category list dependency
            log.info("Parsing category lists dependencies...");
            Set<CategoryListEntity> categories = new HashSet<>();
            List<Category> categoryList = page.getCategoryList();
            for (Category category : categoryList) {
                String categoryExternalId = category.getId();
                // Check if already exists in database
                Optional<CategoryListEntity> possibleCategoryListEntity = categoryListService.findByExternalId(categoryExternalId);
                if (!possibleCategoryListEntity.isPresent()) {
                    CategoryListEntity categoryListEntity = new CategoryListEntity();
                    categoryListEntity.setExternalId(categoryExternalId);
                    categoryListEntity.setName(category.getName());

                    // Add category
                    categories.add(categoryListEntity);
                } else
                    categories.add(possibleCategoryListEntity.get());
            }
            // Save category lists and assign them to the page
            categories = categoryListService.saveAll(categories);
            pageEntity.setCategoryLists(categories);
            log.info("Category lists saved...");

            // Set email dependency
            log.info("Parsing emails dependencies...");
            Set<EmailEntity> emails = new HashSet<>();
            List<String> emailList = page.getEmails();
            for (String email : emailList) {
                // Check if already exists in database
                Optional<EmailEntity> possibleEmail = emailService.findByName(email);
                if (!possibleEmail.isPresent()) {
                    EmailEntity emailEntity = new EmailEntity();
                    emailEntity.setName(email);

                    // Add email
                    emails.add(emailEntity);
                } else
                    emails.add(possibleEmail.get());
            }
            // Save emails and assign them to the page
            emails = emailService.saveAll(emails);
            pageEntity.setEmails(emails);
            log.info("Emails saved...");

            // Set hours dependency
            log.info("Parsing hours dependencies...");
            Set<HourEntity> hours = new HashSet<>();
            Map<Hours.DayOfWeek, Map<Integer, Hours.Hour>> mapDayHours = page.getHours().getHours();
            for (Hours.DayOfWeek day : mapDayHours.keySet()) {
                Map<Integer, Hours.Hour> mapHours = mapDayHours.get(day);
                for (Hours.Hour hour : mapHours.values()) {
                    HourEntity hourEntity = new HourEntity();
                    hourEntity.setDayOfWeek(day.name());
                    hourEntity.setOpenTime(LocalTime.parse(hour.getOpen()));
                    hourEntity.setCloseTime(LocalTime.parse(hour.getClose()));
                    hourEntity.setPage(pageEntity);

                    // Add hour
                    hours.add(hourEntity);
                }
            }
            pageEntity.setHours(hours);

            // Save page
            pageEntity = pageService.save(pageEntity);
            log.info("Page information saved...");
        } else {
            log.debug("Page {} is already inserted", possiblePage.get().getExternalId());
            pageEntity = possiblePage.get();
        }

        // Wait to do the next request
        sleep();

        return pageEntity;
    }

    // https://developers.facebook.com/docs/graph-api/reference/v7.0/insights
    public void getPageInsights(LocalDateTime startingDate, String pageId) {
        // Get beginning of day
        startingDate = Functions.getStartOfDay(startingDate);
        // Search until this date is reached
        LocalDateTime stopDate = Functions.getEndOfDay(LocalDateTime.now().minusDays(1));

        // Check if stop date is after starting date
        if (startingDate.isAfter(stopDate))
            startingDate = Functions.getStartOfDay(stopDate);

        // First possible date to start searching (limited to two previous years)
        LocalDateTime maxDate = Functions.getStartOfDay(LocalDateTime.now().minusYears(2));
        if (startingDate.isBefore(maxDate))
            startingDate = maxDate;

        // Convert date to unix to use it in the request
        long startingDateUnix = Functions.localDateTimeToUnix(startingDate);

        // Make the request
        log.info("Requesting page insights since {}...", startingDate);
        Connection<JsonObject> insightsConnection = facebookClient.fetchConnection(pageId + "/insights", JsonObject.class,
                Parameter.with("metric", "page_content_activity_by_action_type_unique," +
                        "page_content_activity_by_age_gender_unique,page_content_activity_by_country_unique," +
                        "page_impressions_unique,page_impressions_by_country_unique," +
                        "page_impressions_by_age_gender_unique,page_post_engagements,page_consumptions_unique," +
                        "page_negative_feedback_by_type_unique,page_positive_feedback_by_type_unique," +
                        "page_fans_online_per_day,page_actions_post_reactions_total,page_fans," +
                        "page_fans_locale,page_fans_country,page_fans_gender_age,page_fans_by_like_source_unique," +
                        "page_fans_by_unlike_source_unique,page_video_view_time,page_posts_impressions_unique"),
                Parameter.with("period", "day"),
                Parameter.with("since", startingDateUnix), Parameter.with("until", startingDateUnix));

        // Iterate until reach stop date
        for (List<JsonObject> insights : insightsConnection) {
            // Iterate each requested metric in the insight object
            LocalDateTime currentInsightTime = null;
            List<String> json = new ArrayList<>();
            for (JsonObject insight : insights) {
                // Merge all the metrics in a same object
                json.add(insight.toString());

                // Get current insight time
                if (currentInsightTime == null) {
                    // Transform Facebook end_date to LocalDateTime format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+SSSS",
                            Locale.ENGLISH);
                    // Get the date of this insight metric
                    currentInsightTime = LocalDateTime.parse(insight.get("values").asArray().get(0).asObject()
                            .get("end_time").asString(), formatter);
                    log.info("Parsing page insight for date {}...", currentInsightTime);
                }
            }

            // If the metric date is after the stop date, then the method must stop
            if (currentInsightTime.isBefore(stopDate)) {
                // Make the hash (created date + page id + json)
                String hash = Functions.doHash(currentInsightTime + fbConf.getPageId() + json.toString());

                // Check if the insight is already inserted
                Optional<InsightEntity> possibleInsight = insightService.findByHash(hash);
                if (!possibleInsight.isPresent()) {
                    InsightEntity insightEntity = new InsightEntity();
                    insightEntity.setExternalId(fbConf.getPageId());
                    insightEntity.setCreated(currentInsightTime);
                    insightEntity.setInserted(LocalDateTime.now());
                    insightEntity.setResponse(json.toString());
                    insightEntity.setHash(hash);

                    // Save insight
                    insightService.save(insightEntity);
                    log.info("Page insight saved...");
                } else
                    log.debug("Page insight {} is already inserted", possibleInsight.get().getHash());
            } else
                break;

            // Wait to do the next request
            sleep();
        }
    }

    // https://developers.facebook.com/docs/graph-api/reference/page/published_posts/
    public void getPostsFromPage(PageEntity page) {
        // If parsed is false, then information is missing (try to get all posts)
        log.info("Requesting posts from page {}...", page.getExternalId());
        Connection<Post> pageFeed;
        if (!page.isParsed()) {
            // Get oldest post (rest one minute to avoid comparing the saved post)
            Optional<PostEntity> oldestPost = nodeService.findOldestPost();
            long until;
            if (oldestPost.isPresent())
                until = Functions.localDateTimeToUnix(oldestPost.get().getCreated().minusMinutes(1));
            else
                until = Functions.localDateTimeToUnix(LocalDateTime.now());

            // Get posts by until date
            pageFeed = facebookClient.fetchConnection(page.getExternalId() + "/published_posts", Post.class,
                    Parameter.with("fields", "created_time,updated_time,message,message_tags,permalink_url,shares," +
                            "status_type,story,place"),
                    Parameter.with("limit", "100"),
                    Parameter.with("until", until));
        } else {
            // Get most recent posts first
            pageFeed = facebookClient.fetchConnection(page.getExternalId() + "/published_posts", Post.class,
                    Parameter.with("fields", "created_time,updated_time,message,message_tags,permalink_url,shares," +
                            "status_type,story,place"),
                    Parameter.with("limit", "100"));
        }

        // Iterate posts
        Set<String> externalIds = new HashSet<>();
        boolean stopSearching = false;
        Iterator<List<Post>> iterator = pageFeed.iterator();
        while (iterator.hasNext() && !stopSearching) {
            // Posts to be returned
            Set<NodeEntity> postEntities = new HashSet<>();

            // Get the posts
            List<Post> posts = iterator.next();
            for (Post post : posts) {
                // Get facebook id
                String externalId = post.getId();
                log.info("Parsing post {}...", externalId);

                // Check if the post already exists
                Optional<NodeEntity> possiblePost = nodeService.findByExternalId(externalId);
                if (!possiblePost.isPresent() && !externalIds.contains(externalId)) {
                    // Get post information
                    PostEntity postEntity = new PostEntity();
                    postEntity.setExternalId(externalId);
                    LocalDateTime created = Functions.convertToLocalDateTime(post.getCreatedTime());
                    postEntity.setCreated(created);
                    postEntity.setUpdated(Functions.convertToLocalDateTime(post.getUpdatedTime()));
                    postEntity.setPermaLink(post.getPermalinkUrl());
                    postEntity.setLink(post.getLink());
                    postEntity.setMessage(post.getMessage());
                    postEntity.setStatusType(post.getStatusType());
                    postEntity.setStory(post.getStory());
                    postEntity.setParsed(false);
                    // Set keep updating variable
                    LocalDateTime now = Functions.getStartOfDay(LocalDateTime.now());
                    // Check if the node is in range to be updated
                    postEntity.setKeepUpdating(now.isBefore(created.plusDays(fbConf.getUpdateCommentDays())));
                    // Get shares
                    Post.Shares shares = post.getShares();
                    if (shares != null)
                        postEntity.setShares(post.getShares().getCount());

                    // Get the message tags
                    log.info("Parsing message tags dependency...");
                    List<MessageTag> messageTags = post.getMessageTags();
                    for (MessageTag messageTag : messageTags) {
                        MessageTagEntity messageTagEntity = new MessageTagEntity();
                        messageTagEntity.setName(messageTag.getName());
                        messageTagEntity.setPost(postEntity);

                        // Set dependencies
                        postEntity.getMessageTags().add(messageTagEntity);
                    }

                    // Get place
                    Place place = post.getPlace();
                    if (place != null) {
                        if (place.getId() != null) {
                            log.info("Parsing place dependency...");
                            // Parse place
                            PlaceEntity placeEntity = parsePlace(place);

                            // Save place and set dependencies
                            placeEntity.getNodes().add(postEntity);
                            placeEntity = placeService.save(placeEntity);
                            postEntity.setPlace(placeEntity);
                        }
                    }

                    // Set dependencies
                    postEntity.setPage(page);

                    // Add post entity
                    postEntities.add(postEntity);
                    externalIds.add(postEntity.getExternalId());
                } else if (possiblePost.isPresent()) {
                    log.debug("Post {} is already inserted", possiblePost.get().getExternalId());

                    // Get post
                    PostEntity postEntity = (PostEntity) possiblePost.get();

                    if (postEntity.isKeepUpdating()) {
                        // Get created date and the date of today
                        LocalDateTime created = postEntity.getCreated();
                        LocalDateTime now = Functions.getStartOfDay(LocalDateTime.now());

                        // Check if the object should keep updating
                        if (now.isBefore(created.plusDays(fbConf.getUpdateCommentDays()))) {
                            // Update shares
                            Post.Shares shares = post.getShares();
                            if (shares != null)
                                postEntity.setShares(post.getShares().getCount());
                        } else
                            // Post is too old, stop updating it
                            postEntity.setKeepUpdating(false);

                        // Add post entity
                        postEntities.add(postEntity);
                        externalIds.add(postEntity.getExternalId());
                    } else {
                        // Stop retrieving feed because the next ones are already inserted and they should not be updated
                        stopSearching = true;
                        break;
                    }
                }
            }

            // Save nodes
            nodeService.saveAll(postEntities);
            log.info("Posts saved...");

            // Wait to do the next request
            sleep();
        }
    }

    // https://developers.facebook.com/docs/graph-api/reference/v7.0/insights
    public void getPostInsights(String postId) {
        log.info("Requesting lifetime post insights...");
        // Get the lifetime insights for the input post
        Connection<JsonObject> insightsConnection = facebookClient.fetchConnection(postId + "/insights", JsonObject.class,
                Parameter.with("metric", "post_impressions_unique,post_impressions_fan_unique,post_engaged_users," +
                        "post_negative_feedback_unique,post_engaged_fan,post_reactions_by_type_total,post_video_avg_time_watched," +
                        "post_video_length,post_video_views_clicked_to_play,post_video_views_10s_unique," +
                        "post_video_view_time,post_video_view_time_by_age_bucket_and_gender,post_video_view_time_by_country_id"),
                Parameter.with("period", "lifetime"));
        this.getObjectInsights(postId, insightsConnection);
    }

    // https://developers.facebook.com/docs/graph-api/reference/v7.0/comment
    public Set<CommentEntity> getCommentsFromNode(NodeEntity nodeEntity) {
        // Posts to be returned
        Set<CommentEntity> commentEntities = new HashSet<>();

        // Get comments connection from the post
        log.info("Requesting comments from node {}...", nodeEntity.getExternalId());
        Connection<Comment> commentConnection = facebookClient.fetchConnection(nodeEntity.getExternalId() + "/comments", Comment.class,
                Parameter.with("fields", "message,created_time,like_count,comment_count,attachment,comments.summary(true)"),
                Parameter.with("limit", 500),
                Parameter.with("summary", 1));

        // Iterate until reach stop date
        for (List<Comment> comments : commentConnection) {
            // Iterate comments
            for (Comment comment : comments) {
                String externalId = comment.getId();
                log.info("Parsing node comments and replies {}...", externalId);

                // Check if the comment is already inserted in database
                Optional<CommentEntity> possibleComment = commentService.findByExternalId(externalId);
                if (!possibleComment.isPresent()) {
                    // Parse the comment information
                    CommentEntity commentEntity = parseComment(comment);
                    // Set dependencies
                    commentEntity.getNodes().add(nodeEntity);

                    // Create the possible replies to this comment
                    List<Comment> replies = comment.getComments().getData();
                    Set<CommentEntity> replyEntities = new HashSet<>();
                    for (Comment reply : replies) {
                        // Parse the reply information
                        CommentEntity replyEntity = parseComment(reply);

                        // Set dependencies
                        replyEntity.getNodes().add(nodeEntity);
                        replyEntity.setParent(commentEntity);

                        replyEntities.add(replyEntity);
                    }

                    // Set dependencies
                    commentEntity.setComments(replyEntities);

                    // Add comment to be returned
                    commentEntities.add(commentEntity);
                } else {
                    // Update comment information and add possible replies
                    CommentEntity savedCommentEntity = possibleComment.get();
                    log.info("Comment {} is already inserted, updating...", savedCommentEntity.getExternalId());

                    // Check the difference of days
                    LocalDateTime commentCreated = savedCommentEntity.getCreated();
                    LocalDateTime now = Functions.getStartOfDay(LocalDateTime.now());
                    // If the comment is in range (today < creation + updating days), keep updating its information
                    if (now.isBefore(commentCreated.plusDays(fbConf.getUpdateCommentDays()))) {
                        // Add new replies if there are any
                        Long value = comment.getCommentCount();
                        if (value > savedCommentEntity.getCommentCount()) {
                            // Get the replies and process them
                            Set<CommentEntity> savedReplyEntities = savedCommentEntity.getComments();
                            List<Comment> replies = comment.getComments().getData();
                            for (Comment reply : replies) {
                                // Check if the reply already exists in the comment
                                boolean exists = savedReplyEntities.stream()
                                        .anyMatch(r -> r.getExternalId().equals(reply.getId()));
                                if (!exists) {
                                    // Parse the reply information
                                    CommentEntity replyEntity = parseComment(reply);

                                    // Set dependencies
                                    replyEntity.getNodes().add(nodeEntity);
                                    replyEntity.setParent(savedCommentEntity);

                                    // Add the new reply to the saved comment
                                    savedCommentEntity.getComments().add(replyEntity);
                                }
                            }
                        }

                        // Update counts and message
                        savedCommentEntity.setMessage(comment.getMessage());
                        value = comment.getLikeCount();
                        if (value != null)
                            savedCommentEntity.setLikeCount(value);
                        value = comment.getCommentCount();
                        if (value != null)
                            savedCommentEntity.setCommentCount(value);

                        // Check if the comment is assigned to the current node
                        boolean containsNode = savedCommentEntity.getNodes().stream()
                                .anyMatch(n -> n.getExternalId().equals(nodeEntity.getExternalId()));
                        if (!containsNode)
                            savedCommentEntity.getNodes().add(nodeEntity);
                    }

                    // Add comment to be returned
                    commentEntities.add(savedCommentEntity);
                }
            }

            // Save comments
            commentEntities = commentService.saveAll(commentEntities);
            log.info("Node comments saved...");

            // Wait to do the next request
            sleep();
        }

        return commentEntities;
    }

    // https://developers.facebook.com/docs/graph-api/reference/video/
    public void getVideosFromPage(PageEntity page) {
        // If parsed is false, then information is missing (try to get all posts)
        log.info("Requesting videos from page {}...", page.getExternalId());
        Connection<Video> pageVideos;
        if (!page.isParsed()) {
            // Get oldest video (rest one minute to avoid comparing the saved video)
            Optional<VideoEntity> oldestVideo = nodeService.findOldestVideo();
            long until;
            if (oldestVideo.isPresent())
                until = Functions.localDateTimeToUnix(oldestVideo.get().getCreated().minusMinutes(1));
            else
                until = Functions.localDateTimeToUnix(LocalDateTime.now());

            // Get videos connection from page (most recent goes first)
            pageVideos = facebookClient.fetchConnection(page.getExternalId() + "/videos", Video.class,
                    Parameter.with("fields", "created_time,description,custom_labels,length,permalink_url," +
                            "place,title,source,updated_time"),
                    Parameter.with("limit", "100"),
                    Parameter.with("until", until));
        } else {
            // Get most recent videos first
            pageVideos = facebookClient.fetchConnection(page.getExternalId() + "/videos", Video.class,
                    Parameter.with("fields", "created_time,description,custom_labels,length,permalink_url," +
                            "place,title,source,updated_time"),
                    Parameter.with("limit", "100"));
        }

        // Iterate videos
        Set<String> externalIds = new HashSet<>();
        boolean stopSearching = false;
        Iterator<List<Video>> iterator = pageVideos.iterator();
        while (iterator.hasNext() && !stopSearching) {
            // Posts to be saved
            Set<NodeEntity> videoEntities = new HashSet<>();

            // Get the videos
            List<Video> videos = iterator.next();

            // Iterate over the list of contained data to access the individual object
            for (Video video : videos) {
                // Get external id from Facebook
                String externalId = video.getId();
                log.info("Parsing video {}...", externalId);

                // Check if the post already exists
                Optional<NodeEntity> possibleVideo = nodeService.findByExternalId(externalId);
                if (!possibleVideo.isPresent() && !externalIds.contains(externalId)) {
                    // Get video information
                    VideoEntity videoEntity = new VideoEntity();
                    videoEntity.setExternalId(externalId);
                    LocalDateTime created = Functions.convertToLocalDateTime(video.getCreatedTime());
                    videoEntity.setCreated(created);
                    videoEntity.setUpdated(Functions.convertToLocalDateTime(video.getUpdatedTime()));
                    videoEntity.setPermaLink(video.getPermalinkUrl());
                    videoEntity.setTitle(video.getTitle());
                    videoEntity.setSource(video.getSource());
                    videoEntity.setLength(video.getLength());
                    videoEntity.setDescription(video.getDescription());
                    videoEntity.setParsed(false);
                    // Set keep updating variable
                    LocalDateTime now = Functions.getStartOfDay(LocalDateTime.now());
                    // Check if the node is in range to be updated
                    videoEntity.setKeepUpdating(now.isBefore(created.plusDays(fbConf.getUpdateCommentDays())));
                    videoEntity.setKeepUpdating(true);

                    // Get the custom labels
                    log.info("Parsing custom labels dependency...");
                    List<String> customLabels = video.getCustomLabels();
                    for (String customLabel : customLabels) {
                        CustomLabelEntity customLabelEntity = new CustomLabelEntity();
                        customLabelEntity.setName(customLabel);
                        customLabelEntity.setVideo(videoEntity);

                        videoEntity.getCustomLabels().add(customLabelEntity);
                    }

                    // Get place
                    Place place = video.getPlace();
                    if (place != null) {
                        log.info("Parsing place dependency...");
                        // Parse place
                        PlaceEntity placeEntity = parsePlace(place);

                        // Save place and set dependencies
                        placeEntity.getNodes().add(videoEntity);
                        placeEntity = placeService.save(placeEntity);
                        videoEntity.setPlace(placeEntity);
                    }

                    // Set dependencies
                    videoEntity.setPage(page);

                    // Add post entity
                    videoEntities.add(videoEntity);
                    externalIds.add(externalId);
                } else if (possibleVideo.isPresent()) {
                    log.debug("Video {} is already inserted", possibleVideo.get().getExternalId());

                    // Get video
                    VideoEntity videoEntity = (VideoEntity) possibleVideo.get();

                    if (videoEntity.isKeepUpdating()) {
                        // Get created date and the date of today
                        LocalDateTime created = videoEntity.getCreated();
                        LocalDateTime now = Functions.getStartOfDay(LocalDateTime.now());

                        // Check if the object should keep updating
                        if (now.isAfter(created.plusDays(fbConf.getUpdateCommentDays())))
                            // Video is too old, stop updating it
                            videoEntity.setKeepUpdating(false);

                        // Add video entity
                        videoEntities.add(possibleVideo.get());
                        externalIds.add(externalId);
                    } else {
                        // Stop retrieving feed because the next ones are already inserted and they should not be updated
                        stopSearching = true;
                        break;
                    }
                }
            }

            // Save videos
            nodeService.saveAll(videoEntities);
            log.info("Videos saved...");

            // Wait to do the next request
            sleep();
        }
    }

    // https://developers.facebook.com/docs/graph-api/reference/video/video_insights/
    public void getVideoInsights(String videoId) {
        log.info("Requesting lifetime video insights...");
        // Get the lifetime insights for the input video
        Connection<JsonObject> insightsConnection = facebookClient.fetchConnection(videoId + "/video_insights", JsonObject.class);
        this.getObjectInsights(videoId, insightsConnection);
    }

    private void getObjectInsights(String objectId, Connection<JsonObject> insightsConnection) {
        // Get the beginning of day
        LocalDateTime dayStart = Functions.getStartOfDay(LocalDateTime.now());

        // Make the hash for the current insight (created date + object id)
        String hash = Functions.doHash(dayStart + objectId);

        // Check if the insight for today is already inserted for this video
        Optional<InsightEntity> possibleInsight = insightService.findByHash(hash);
        if (!possibleInsight.isPresent()) {
            log.info("Parsing insight...");
            // Get insights connection data (it is not necessary an iterator due to it only return one value)
            List<JsonObject> insights = insightsConnection.getData();
            List<String> json = new ArrayList<>();
            for (JsonObject insight : insights)
                // Merge all the metrics in a same object
                json.add(insight.toString());

            InsightEntity insightEntity = new InsightEntity();
            insightEntity.setExternalId(objectId);
            insightEntity.setCreated(dayStart);
            insightEntity.setInserted(LocalDateTime.now());
            insightEntity.setResponse(json.toString());
            insightEntity.setHash(hash);

            // Save insights
            insightService.save(insightEntity);
            log.info("Insight saved...");
        } else
            log.debug("Insight {} is already inserted", possibleInsight.get().getHash());

        // Wait to do the next request
        sleep();
    }

    private CommentEntity parseComment(Comment comment) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setExternalId(comment.getId());
        commentEntity.setCreated(Functions.convertToLocalDateTime(comment.getCreatedTime()));
        commentEntity.setMessage(comment.getMessage());
        Long value = comment.getLikeCount();
        if (value != null)
            commentEntity.setLikeCount(value);
        value = comment.getCommentCount();
        if (value != null)
            commentEntity.setCommentCount(value);

        // Create the possible attachment
        StoryAttachment attachment = comment.getAttachment();
        if (attachment != null) {
            AttachmentEntity attachmentEntity = new AttachmentEntity();
            attachmentEntity.setType(attachment.getType());
            attachmentEntity.setUrl(attachment.getUrl());
            attachmentEntity.setTitle(attachment.getTitle());
            attachmentEntity.setDescription(attachment.getDescription());
            attachmentEntity.setMediaType(attachment.getMediaType());

            // Set dependencies
            attachmentEntity.setComment(commentEntity);
            commentEntity.setAttachment(attachmentEntity);
        }

        return commentEntity;
    }

    private PlaceEntity parsePlace(Place place) {
        // Get place
        String placeExternalId = place.getId();

        // Check if the place is already inserted
        Optional<PlaceEntity> possiblePlace = placeService.findByExternalId(placeExternalId);
        PlaceEntity placeEntity;
        if (!possiblePlace.isPresent()) {
            placeEntity = new PlaceEntity();
            placeEntity.setName(place.getName());
            placeEntity.setExternalId(place.getId());

            // Get associated location
            Location location = place.getLocation();
            if (location != null) {
                LocationEntity locationEntity = new LocationEntity();
                locationEntity.setCity(location.getCity());
                locationEntity.setCountry(location.getCountry());
                locationEntity.setStreet(location.getStreet());
                locationEntity.setLatitude(location.getLatitude());
                locationEntity.setLongitude(location.getLongitude());
                locationEntity.setZip(location.getZip());

                // Set dependencies
                locationEntity.setPlace(placeEntity);
                placeEntity.setLocation(locationEntity);
            }
        } else {
            log.debug("The place {} is already inserted", place.getName());
            placeEntity = possiblePlace.get();
        }

        return placeEntity;
    }

    private void sleep() {
        try {
            Thread.sleep(fbConf.getWaitingBetweenRequests());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
