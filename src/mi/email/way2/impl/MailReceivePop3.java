package mi.email.way2.impl;
/**
 * @version 1.0
 * @author sien
 * @description pop3方式 接收
 */
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import mi.email.way2.api.IMailBox;
import mi.email.way2.api.MailConfig;
import mi.email.way2.tools.MailReceiveListener;

public class MailReceivePop3 implements IMailBox{
	private Folder mailFolder;
	private MailReceiveListener listener;
	
	private final int EVENT_STATE_CONNECT = 1;
	
	private String mailAccount,mailPwd,mailHost;
	
	public MailReceivePop3(){
		this.mailAccount = MailConfig.userName;
		this.mailPwd = MailConfig.password;
		this.mailHost = MailConfig.hostServicePop3;
	}
	
	public MailReceivePop3(String uname,String upwd,String uhost){
		this.mailAccount = uname;
		this.mailPwd = upwd;
		this.mailHost = uhost;
	}
	
	@Override
	public void setMailListener(MailReceiveListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void removeMailListener(){
		this.listener = null;
	}
	
	@Override
	public Folder getFolder(){
		return mailFolder;
	}
	
	@Override
	public void connectToServer(int openModel) {
		Store store = checkAndInitStore();
		
		if(store != null){
			connectAndOpenMailBox(store,openModel);
		}
	}
	
	private Store checkAndInitStore(){
		Session session = Session.getInstance(getPopProperties(), getAuthenticator());
		try {
			Store mailStore = session.getStore(MailConfig.hostProtocolPop3);
			
			return mailStore;
		} catch (Exception e) {
			e.printStackTrace();
			connectMailFailed("邮箱协议错误",EVENT_STATE_CONNECT);
		}
		return null;
	}
	
	private void connectAndOpenMailBox(Store store ,int openModel){
		try {
			store.connect();
			
			mailFolder = store.getFolder("INBOX");
			mailFolder.open(Folder.READ_WRITE);
			
			loadMailFromMailBox(store,openModel);
			
		} catch (Exception e) {
			e.printStackTrace();
			connectMailFailed("打开收件箱失败！",EVENT_STATE_CONNECT);
		}
	}
	
	private void loadMailFromMailBox(Store store, int openModel){
		if(listener != null){
			listener.onSuccess(mailFolder, store, openModel);
		}
	}
	
	private void connectMailFailed(String msg,int eventype) {
		if(listener != null){
			listener.onFail(msg,eventype);
		}
	}
	
	private MailAuthenticator getAuthenticator(){
		MailAuthenticator authenticator = new MailAuthenticator(mailAccount,mailPwd);
		return authenticator;
	}
	
	private Properties getPopProperties() {
		Properties p = new Properties();
		p.put("mail.pop3.host", mailHost);
		p.put("mail.pop3.port", MailConfig.hostPortPop3);
		return p;
	}

	@Override
	public boolean closeConnection(Store store) {
		try {
			if (mailFolder != null && mailFolder.isOpen()) {
				mailFolder.close(true);
			}
			store.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
