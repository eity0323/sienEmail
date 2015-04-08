package mi.email.way2.tools;

import javax.mail.Folder;
import javax.mail.Store;

public interface MailReceiveListener {
	public void onSuccess(Folder folder,Store store, int openModel);
	public void onFail(String msg,int eventype);
}
