package mi.email.way2.model;

import java.util.Date;
import java.util.HashMap;

import javax.mail.Message;

public class MailDTO {
	public MailBean mailBean;	//邮件基本属性
	public Date sendDate;		//发送日期
	public Message mailMessage;	//消息体
	public HashMap<String, String> attachmentMap;	//附件
}
