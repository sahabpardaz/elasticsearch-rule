package ir.sahab.elasticsearchrule.common;

public class Tweet {
    private String user;
    private String postDate;
    private String message;

    public Tweet(String user, String postDate, String message) {
        this.user = user;
        this.postDate = postDate;
        this.message = message;
    }

    public Tweet() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
