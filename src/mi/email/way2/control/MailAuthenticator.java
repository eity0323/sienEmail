package mi.email.way2.control;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MailAuthenticator extends Authenticator {
	private String username;
	private String password;
	
	public MailAuthenticator(){
		this.username = MailConfig.userName;
		this.password = MailConfig.password;
	}
	
	public MailAuthenticator(String username,String password){
		this.username = username;
		this.password = password;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.username,this.password);
	}
	
}
