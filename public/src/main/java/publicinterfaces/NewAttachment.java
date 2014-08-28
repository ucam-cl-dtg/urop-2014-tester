package publicinterfaces;

import java.util.LinkedList;
import java.util.List;

/**
 * class that stores all information to do with one aspect/possible "problem" with
 * the code being tested
 * @author kls82
 */
public class NewAttachment {
	//name given to the attachment
	private String attachmentName;
    //content
    private String content;
	//mime type
	private String mimeType;
	
	/**
	 * Constructor for Attachment use in code
	 */
	public NewAttachment(String name, String content, String mimeType) {
		this.setAttachmentName(name);
		this.setContent(content);
		this.setMimeType(mimeType);
	}
	
	public NewAttachment() {}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}	
}
