package mi.email.way2.impl;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;

import android.text.TextUtils;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import de.greenrobot.event.EventBus;
import mi.email.way2.api.IMailReceive;
import mi.email.way2.api.MailConfig;
import mi.email.way2.tools.MailEvent;
import mi.email.way2.tools.MailEvent.connectMailServiceEvent;
import mi.email.way2.tools.MailEvent.deleteMailEvent;
import mi.email.way2.tools.MailEvent.loadMailsEvent;
import mi.email.way2.tools.MailEvent.searchMailsEvent;

public class MailReceiveImap implements IMailReceive {
	private static MailReceiveImap instance;
	
	private IMAPFolder mailFolder;
	
	private final int EVENT_STATE_LOAD = 0;
	private final int EVENT_STATE_CONNECT = 1;
	private final int EVENT_STATE_DELETE = 2;
	
	private final int OPEN_MODEL_DELETE = 2;
	
	private Message[] mails;
	private String messageId;
	
	public static MailReceiveImap getInstance(){
		if(instance == null){
			instance = new MailReceiveImap();
		}
		
		return instance;
	}

	@Override
	public void loadMails() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadMailDetail(String messageId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteMails() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteMailByMessageId(String messageId) {
		this.messageId = messageId;
		connectToServer(OPEN_MODEL_DELETE);
	}
	
	private void connectToServer(int openModel) {
		IMAPStore store = checkAndInitStore();
		
		if(store != null){
			connectAndOpenMailBox(store,openModel);
		}
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
			store.connect(MailConfig.userName,MailConfig.password);
			
			mailFolder = (IMAPFolder) store.getFolder("INBOX");
			mailFolder.open(Folder.READ_WRITE);
			
			loadMailFromMailBox(store,openModel);
			
		} catch (Exception e) {
			e.printStackTrace();
			connectMailFailed("打开收件箱失败！",EVENT_STATE_CONNECT);
		}
	}
	
	private void loadMailFromMailBox(IMAPStore store, int openModel){
		deleteMailByMessageId(store);
	}
	
	private void connectMailFailed(String msg,int eventype) {
		if(eventype == EVENT_STATE_CONNECT){
			EventBus.getDefault().post(new MailEvent.connectMailServiceEvent(connectMailServiceEvent.STATUS_FAILED, msg));
		}else if(eventype == EVENT_STATE_LOAD){
			EventBus.getDefault().post(new MailEvent.loadMailsEvent(loadMailsEvent.STATUS_FAILED, null));
			EventBus.getDefault().post(new MailEvent.searchMailsEvent(searchMailsEvent.STATUS_FAILED, null));
		}else if(eventype == EVENT_STATE_DELETE){
			EventBus.getDefault().post(new MailEvent.deleteMailEvent(deleteMailEvent.STATUS_FAILED, msg));
		}
	}

	private MailAuthenticator getAuthenticator(){
		MailAuthenticator authenticator = new MailAuthenticator();
		return authenticator;
	}
	
	private Properties getPopProperties() {
		Properties p = new Properties();
		p.put("mail.store.protocol", MailConfig.hostProtocolImap);
		p.put("mail.imap.host", MailConfig.hostServicePop3);
		p.put("mail.imap.port", MailConfig.hostPortImap);
		return p;
	}

	private boolean closeConnection(IMAPStore store) {
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
	
	private void deleteMailByMessageId(IMAPStore store){
		try{
			getMailByMessageId(messageId);
			
			if(mails != null){
				Message message = mails[0];
				message.setFlag(Flags.Flag.DELETED, true);
				message.saveChanges();
			}
		}catch(Exception ex){
			ex.printStackTrace();
			connectMailFailed("邮件删除失败",EVENT_STATE_DELETE);
		}
		
		EventBus.getDefault().post(new MailEvent.deleteMailEvent(deleteMailEvent.STATUS_SUCCESS, "删除"));
		
		closeConnection(store);
	}
	
	private void getMailByMessageId(String messageId){
		try{
			if(!TextUtils.isEmpty(messageId)){
				SearchTerm st = new MessageIDTerm(messageId);
				mails = mailFolder.search(st);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			connectMailFailed("邮件加载错误！",EVENT_STATE_LOAD);
		}
	}
}
