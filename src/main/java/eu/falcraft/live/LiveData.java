
package eu.falcraft.live;

public class LiveData {
    private boolean isLive;
    private String liveLink;
    private String originalNick;

    public LiveData(String nick) {
        this.originalNick = nick;
    }

    public LiveData(String nick, String link) {
        this.originalNick = nick;
        this.liveLink = link;
    }

    public boolean isLive() {
        return this.isLive;
    }

    public void setIsLive(boolean isLive) {
        this.isLive = isLive;
    }

    public String getLiveLink() {
        return this.liveLink;
    }

    public void setLiveLink(String liveLink) {
        this.liveLink = liveLink;
    }

    public String getOriginalNick() {
        return this.originalNick;
    }

    public void setOriginalNick(String originalNick) {
        this.originalNick = originalNick;
    }
}

