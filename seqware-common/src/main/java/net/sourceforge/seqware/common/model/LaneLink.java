package net.sourceforge.seqware.common.model;

//default package
//Generated 09.12.2011 15:01:20 by Hibernate Tools 3.2.4.GA

/**
 * LaneLink generated by hbm2java
 */
public class LaneLink implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  private int laneLinkId;
  private Lane lane;
  private String label;
  private String url;
  private String db;
  private String id;

  public LaneLink() {
  }

  public LaneLink(int laneLinkId, Lane lane, String label, String url) {
    this.laneLinkId = laneLinkId;
    this.lane = lane;
    this.label = label;
    this.url = url;
  }

  public LaneLink(int laneLinkId, Lane lane, String label, String url, String db, String id) {
    this.laneLinkId = laneLinkId;
    this.lane = lane;
    this.label = label;
    this.url = url;
    this.db = db;
    this.id = id;
  }

  public int getLaneLinkId() {
    return this.laneLinkId;
  }

  public void setLaneLinkId(int laneLinkId) {
    this.laneLinkId = laneLinkId;
  }

  public Lane getLane() {
    return this.lane;
  }

  public void setLane(Lane lane) {
    this.lane = lane;
  }

  public String getLabel() {
    return this.label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDb() {
    return this.db;
  }

  public void setDb(String db) {
    this.db = db;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

}