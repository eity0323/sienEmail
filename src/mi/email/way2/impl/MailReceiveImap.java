package mi.email.way2.impl;
/**
 * @version 1.0
 * @author sien
 * @description imap 接收
 */
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import mi.email.way2.api.IMailBox;
import mi.email.way2.api.MailConfig;
import mi.email.way2.tools.MailReceiveListener;

public class MailReceiveImap implements IMailBox{
	
	private IMAPFolder mailFolder = null;
	private MailReceiveListener listener;
	private final int EVENT_STATE_CONNECT = 1;
	
	private String mailAccount,mailPwd,mailHost;
	
	public MailReceiveImap(){
		this.mailAccount = MailConfig.userName;
		this.mailPwd = MailConfig.password;
		this.mailHost = MailConfig.hostServiceImap;
	}
	
	public MailReceiveImap(String uname,String upwd,String uhost){
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
		IMAPStore store = checkAndInitStore();
		
		if(store != null){
			connectAndOpenMailBox(store,openModel);
		}
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
	
	private IMAPStore checkAndInitStore(){
		Session session = Session.getInstance(getPopProperties());
		try {
			IMAPStore mailStore = (IMAPStore) session.getStore(MailConfig.hostProtocolImap);
			
			return mailStore;
		} catch (Exception e) {
			e.printStackTrace();
			connectMailFailed("邮箱协议错误",EVENT_STATE_CONNECT);
		}
		return null;
	}
	
	private void connectAndOpenMailBox(IMAPStore store ,int openModel){
		try {
			store.connect(mailAccount,mailPwd);
			
			mailFolder = (IMAPFolder) store.getFolder("INBOX");
			mailFolder.open(Folder.READ_WRITE);
			
			loadMailFromMailBox(store,openModel);
			
		} catch (Exception e) {
			e.printStackTrace();
			connectMailFailed("打开收件箱失败！",EVENT_STATE_CONNECT);
		}
	}
	
	private void loadMailFromMailBox(IMAPStore store, int openModel){
		if(listener != null){
			listener.onSuccess(mailFolder, store, openModel);
		}
	}
	
	private void connectMailFailed(String msg,int eventype) {
		if(listener != null){
			listener.onFail(msg,eventype);
		}
	}
	
	private Properties getPopProperties() {
		Properties p = new Properties();
		p.put("mail.store.protocol", MailConfig.hostProtocolImap);
		p.put("mail.imap.host", mailHost);
		p.put("mail.imap.port", MailConfig.hostPortImap);
		return p;
	}
}
