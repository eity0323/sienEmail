package mi.email.way2.api;

import mi.email.way2.model.MailDTO;
import android.content.Context;

public interface IMailManager {
	/*加载邮件列表 or 最新邮件 */
	public void loadAllMails(Context context);
	
	/*加载邮件详情*/
	public void loadMailDetail(String messageId);
	
	/*发送邮件*/
	public void sendMail(MailDTO data);
	
	/*删除邮件*/
	public void deleteMail(String messageId);
	
	/*回复邮件*/
	public void replyMail(MailDTO data);
}
