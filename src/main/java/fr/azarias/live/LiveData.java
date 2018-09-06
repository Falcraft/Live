package fr.azarias.live;








public class LiveData
{
  private boolean isLive;
  






  private String liveLink;
  






  private String originalNick;
  







  public LiveData(String nick)
  {
    originalNick = nick;
  }
  
  public LiveData(String nick, String link) {
    originalNick = nick;
    liveLink = link;
  }
  
  public boolean isLive() {
    return isLive;
  }
  
  public void setIsLive(boolean isLive) {
    this.isLive = isLive;
  }
  
  public String getLiveLink() {
    return liveLink;
  }
  
  public void setLiveLink(String liveLink) {
    this.liveLink = liveLink;
  }
  
  public String getOriginalNick() {
    return originalNick;
  }
  
  public void setOriginalNick(String originalNick) {
    this.originalNick = originalNick;
  }
}
