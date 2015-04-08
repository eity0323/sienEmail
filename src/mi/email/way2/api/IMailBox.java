package mi.email.way2.api;
/**
 * @version 1.0
 * @author sien
 * @description 邮箱打开 、 关闭接口
 */
import javax.mail.Folder;
import javax.mail.Store;

import mi.email.way2.tools.MailReceiveListener;

public interface IMailBox {
	public void connectToServer(int openModel);
	
	public boolean closeConnection(Store store);
	
	public void setMailListener(MailReceiveListener listener);
	
	public void removeMailListener();
	
	public Folder getFolder();
	
}
