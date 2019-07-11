package com.yorma.common.utils.mail;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author zxh
 */
public class SimpleMail {
	private List<String> recipients;
	private String subject;
	private String content;
	private List<File> attachments;
	
	public List<String> getRecipients() {
		return recipients;
	}
	
	public void setRecipients(final List<String> recipients) {
		if (recipients.stream().noneMatch(s -> s.matches(MailAuthenticator.REGEX))) {
			throw new IllegalMailFormatException();
		}
		this.recipients = recipients;
	}
	
	public void setRecipient(final String recipient) {
		this.setRecipients(Collections.singletonList(recipient));
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(final String subject) {
		this.subject = subject;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(final String content) {
		this.content = content;
	}
	
	public List<File> getAttachments() {
		return attachments;
	}
	
	public void setAttachments(final List<File> attachments) {
		this.attachments = attachments;
	}
}
