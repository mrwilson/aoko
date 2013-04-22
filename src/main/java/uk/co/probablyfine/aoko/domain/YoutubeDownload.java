package uk.co.probablyfine.aoko.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class YoutubeDownload implements Comparable<YoutubeDownload> {
	
	@Id
	@GeneratedValue
	private int id;
	
	@Column(nullable=false)
	private String url;
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private DownloadState state;
	
	@Column(nullable = false)
	private int bucket;
	
	@Column(nullable = false)
	private String queuedBy;

	private static final Pattern VIMEO_CODE = Pattern.compile(".*/([0-9]+)$");
	private static final Pattern YOUTUBE_CODE = Pattern.compile("(?<=v=).*?(?=&|$)");
	
	public YoutubeDownload() {}
	
	public YoutubeDownload(String url) {
		this.url = url;
		this.state = DownloadState.WAITING;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public DownloadState getState() {
		return state;
	}

	public void setState(DownloadState state) {
		this.state = state;
	}

	public void setQueuedBy(String queuedBy) {
		this.queuedBy = queuedBy;
	}

	public String getQueuedBy() {
		return queuedBy;
	}

	public int getBucket() {
		return bucket;
	}

	public void setBucket(int bucket) {
		this.bucket = bucket;
	}

	@Override
	public int compareTo(YoutubeDownload arg0) {
		if (arg0.getBucket() < this.getBucket()) {
			return 1;
		} else if (arg0.getBucket() > this.getBucket()) {
			return -1;
		} else if (arg0.getId() < this.getId()) {
			return 1;
		} else if (arg0.getId() > this.getId()) {
			return -1;
		} else {
			return 0;
		}
	}
	
	@Override
	public String toString() {
		return String.format("Url: %s, Bucket: %d, Id: %d", this.url,this.bucket,this.id);
	}
	
	public String getVideoCode() {
		if (getFileType() == FileType.VIMEO) {
			final Matcher m = VIMEO_CODE.matcher(this.getUrl());
			m.find();
			return m.group(1);
			
		} else {
			final Matcher m = YOUTUBE_CODE.matcher(this.getUrl());
			m.find();
			return m.group(0);
		}
		
	}

	public FileType getFileType() {
		if (this.getUrl().contains("vimeo")) {
			return FileType.VIMEO;
		} else {
			return FileType.YOUTUBE;
		}
	}
	
}