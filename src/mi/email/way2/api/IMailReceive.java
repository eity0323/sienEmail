package mi.email.way2.api;

import javax.mail.Message;

import mi.email.way2.model.MailDTO;

/**
 * @version 1.0
 * @author sien
 * @description 收邮件接口
 */
public interface IMailReceive {
	public void loadMails();
	
	public void loadMailDetail(String messageId);
	
	public void deleteMails();
	
	public void deleteMailByMessageId(String messageId);
	
	public void setLastLoadMailMillSeconds(long millsecond);
	
	public MailDTO message2MailDTO(Message message);
}
