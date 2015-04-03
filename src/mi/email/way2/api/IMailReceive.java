package mi.email.way2.api;
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
}
