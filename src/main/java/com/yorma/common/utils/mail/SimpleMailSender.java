package com.yorma.common.utils.mail;


import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static com.yorma.common.utils.object.ObjectUtil.ifNotEmpty;
import static java.lang.System.getProperties;
import static javax.mail.Session.getInstance;
import static javax.mail.internet.MimeUtility.encodeWord;

/**
 * @author zxh
 */
public class SimpleMailSender {
	private static final String REGEX = "^[A-Za-z0-9\\u4e00-\\u9fa5][A-Za-z0-9\\-_\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
	/**
	 * 发送邮件的props文件
	 */
	private final transient Properties props = getProperties();
	/**
	 * 邮件服务器登录验证
	 */
	private transient MailAuthenticator authenticator;
	
	/**
	 * 邮箱session
	 */
	private transient Session session;
	
	/**
	 * 初始化邮件发送器
	 *
	 * @param smtpHostName
	 * 		SMTP邮件服务器地址
	 * @param senderMail
	 * 		发送邮件的用户名(地址)
	 * @param password
	 * 		发送邮件的密码
	 */
	public SimpleMailSender(final String smtpHostName, final String senderMail,
	                        final String password) {
		init(senderMail, password, smtpHostName);
	}
	
	public SimpleMailSender() {
	}
	
	/**
	 * 初始化邮件发送器
	 *
	 * @param senderMail
	 * 		发送邮件的用户名(地址)，并以此解析SMTP服务器地址
	 * @param password
	 * 		发送邮件的密码
	 */
	public SimpleMailSender(final String senderMail, final String password) {
		if (senderMail.matches(REGEX)) {
			//通过邮箱地址解析出smtp服务器，对大多数邮箱都管用
			final String smtpHostName = "smtp." + senderMail.split("@")[1];
			init(senderMail, password, smtpHostName);
		} else {
			throw new IllegalMailFormatException();
		}
	}
	
	/**
	 * 初始化
	 *
	 * @param senderMail
	 * 		发送邮件的用户名(地址)
	 * @param password
	 * 		密码
	 * @param smtpHostName
	 * 		SMTP主机地址
	 */
	private void init(String senderMail, String password, String smtpHostName) {
		// 初始化props
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", smtpHostName);
		// 验证
		authenticator = new MailAuthenticator(senderMail, password);
		// 创建session
		session = getInstance(props, authenticator);
	}
	
	private InternetAddress getInternetAddress(final String address) {
		try {
			return new InternetAddress(address);
		} catch (AddressException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 发送邮件
	 *
	 * @param mail
	 * 		邮件对象
	 */
	public void send(SimpleMail mail) throws MessagingException {
		// 创建mime类型邮件
		final MimeMessage message = new MimeMessage(session);
		// 设置收件人
		final InternetAddress[] addresses = mail.getRecipients().stream().map(this::getInternetAddress).toArray(InternetAddress[]::new);
		message.setRecipients(MimeMessage.RecipientType.TO, addresses);
		// 设置发信人
		message.setFrom(new InternetAddress(authenticator.getSenderMail()));
		// 设置主题
		message.setSubject(mail.getSubject());
		final Multipart multipart = new MimeMultipart();
		final BodyPart contentPart = new MimeBodyPart();
		contentPart.setContent(mail.getContent().replace("\n", "<br/>"), "text/html;charset=UTF-8");
		multipart.addBodyPart(contentPart);
		ifNotEmpty(mail.getAttachments(), attachments -> {
			for (File attachment : attachments) {
				final BodyPart attachmentBodyPart = new MimeBodyPart();
				final DataSource source = new FileDataSource(attachment);
				try {
					attachmentBodyPart.setDataHandler(new DataHandler(source));
					attachmentBodyPart.setFileName(encodeWord(attachment.getName()));
					multipart.addBodyPart(attachmentBodyPart);
				} catch (MessagingException | UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}
		});
		message.setContent(multipart);
		Transport.send(message);
	}
}
