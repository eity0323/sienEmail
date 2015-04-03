package mi.email.way2.api;
/**
 * @version 1.0
 * @author sien
 * @description 发邮件接口
 */
import mi.email.way2.model.MailDTO;

public interface IMailSend {
	public void sendMail(MailDTO data);
	
	public void replyMail(MailDTO data);
}
