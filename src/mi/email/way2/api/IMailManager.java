package mi.email.way2.api;

import mi.email.way2.model.MailDTO;
import android.content.Context;

public interface IMailManager {
	public void loadAllMails(Context context);
	
	public void loadMailDetail(String messageId);
	
	public void sendMail(MailDTO data);
	
	public void deleteMail(String messageId);
	
	public void replyMail(MailDTO data);
}
