package mi.email.way2.impl;
/**
 * @version 1.0
 * @author sien
 * @description 发送 and 回复邮件
 */
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

import mi.email.way2.api.IMailSend;
import mi.email.way2.api.MailConfig;
import mi.email.way2.tools.MailSendListener;

public class MailSendSmtp implements IMailSend{
	private MailSendListener listener;
	private Session session;
	
	public static int SEND_PIC_INNER_MODEL = 1;
	public static int SEND_PIC_ATTACH_MODEL = 2;
	
	private String mailAccount,mailPwd,mailHost;
	
	public MailSendSmtp(){
		this.mailAccount = MailConfig.userName;
		this.mailPwd = MailConfig.password;
		this.mailHost = MailConfig.hostServiceSmtp;
	}
	
	public MailSendSmtp(String uname,String upwd,String uhost){
		this.mailAccount = uname;
		this.mailPwd = upwd;
		this.mailHost = uhost;
	}
	
	public void setMailListener(MailSendListener listener) {
		this.listener = listener;
	}
	
	public Session getMailSession(){
		if(session == null){
			MailAuthenticator authenticator = new MailAuthenticator(mailAccount, mailPwd);
			session = Session.getInstance(getSmtpProperties(true),authenticator);
		}
		return session;
	}

	@Override
	public void send(Message message) {
		
	}
	
	@Override
	public void sendMessage(Message message,String toAddress){
		Transport tran;
		try {
			tran = getMailSession().getTransport();
			tran.connect(mailHost, MailConfig.hostPortSmtp, mailAccount, mailPwd);// 连接到新浪邮箱服务器
			tran.sendMessage(message, new Address[] { new InternetAddress(toAddress) });// 设置邮件接收人
			tran.close();
			
			connectSuccess();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			connectFail();
		}
	}
	
	private void connectSuccess(){
		if(listener != null){
			listener.onSuccess();
		}
	}
	
	private void connectFail(){
		if(listener != null){
			listener.onFail();
		}
	}
	
	private Properties getSmtpProperties(boolean needAuth) {
		Properties p = new Properties();
		p.put("mail.smtp.host", mailHost);
		p.put("mail.smtp.port",MailConfig.hostPortSmtp);
		p.put("mail.transport.protocol", MailConfig.hostProtocolSmtp);
		
		if(needAuth){
			p.put("mail.smtp.auth","true");//设置验证机制
		}
		return p;
	}
	
}
