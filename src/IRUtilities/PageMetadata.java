package IRUtilities;

import java.io.Serializable;

public class PageMetadata implements Serializable{
	public String title;
	public long size;
	public long lastModified;
	public PageMetadata(String title, long size, long lastModified) {
		this.title = title;
		this.size = size;
		this.lastModified = lastModified;
	}
}