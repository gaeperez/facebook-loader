package es.uvigo.ei.sing.facebook.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Validated
@ConfigurationProperties("loader.facebook")
public class FacebookConfiguration {
    @NotEmpty
    // Access token to use the Graph API methods
    private String appId;
    @NotEmpty
    private String appSecret;
    @NotEmpty
    private String clientToken;
    private String profileId;
    @NotEmpty
    private String pageId;
    @NotEmpty
    private String pageAccessToken;
    @Min(value = 1, message = "The minimum value for updating objects is 1 day.")
    private int updateCommentDays;
    @Min(value = 1000, message = "The minimum value for waiting between requests is 1000 ms.")
    private int waitingBetweenRequests;
}
