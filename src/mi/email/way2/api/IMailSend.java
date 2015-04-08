package mi.email.way2.api;

import javax.mail.Message;
import javax.mail.Session;

import mi.email.way2.tools.MailSendListener;

public interface IMailSend {
	public void send(Message message);
	public void sendMessage(Message message,String toAddress);
	
	public Session getMailSession();
	public void setMailListener(MailSendListener listener);
}
