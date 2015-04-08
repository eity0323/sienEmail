package mi.email.way2.api;

import javax.mail.Message;

import mi.email.way2.model.MailDTO;

/**
 * @version 1.0
 * @author sien
 * @description 收邮件接口
 */
public interface IMailReceiver {
	/*加载邮件列表*/
	public void loadMails();
	
	/*加载邮件详情*/
	public void loadMailDetail(String messageId);
	
	/*删除全部邮件*/
	public void deleteMails();
	
	/*删除指定邮件*/
	public void deleteMailByMessageId(String messageId);
	
	/*设置上一次加载时间*/
	public void setLastLoadMailMillSeconds(long millsecond);
	
	/*邮件对象转换实体对象*/
	public MailDTO message2MailDTO(Message message);
}
