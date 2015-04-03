package mi.email.way2.model;

import java.util.Date;
import java.util.HashMap;

import javax.mail.Message;

public class MailDTO {
	public MailBean mailBean;
	public Date sendDate;
	public Message mailMessage;
	public HashMap<String, String> attachmentMap;
}
