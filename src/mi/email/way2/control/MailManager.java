package mi.email.way2.control;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

import mi.email.way2.db.DBManager;
import mi.email.way2.db.MailBeanDao;
import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import android.content.Context;
import android.text.TextUtils;
import de.greenrobot.dao.query.QueryBuilder;

public class MailManager {
	private static MailManager instance;

	private List<MailDTO> mailBeans;
	private Context mcontext;

	private long lastLoadMailMillSeconds = -1;

	private final String LAST_RECEIVE_MAIL_TIME = "lastReceiveMailMillSeconds";
	
	public static int SEND_SIMPLE_MAIL = 1;
	public static int SEND_COMPLEX_MAIL = 2;

	public static MailManager getInstance() {
		if (instance == null) {
			instance = new MailManager();
		}
		return instance;
	}

	/**
	 * 收邮件
	 */
	public void receiveAllMailInThread(Context context){
		mcontext = context;

		lastLoadMailMillSeconds = -1;
		String secondstr = PreferencesManager.getInstance(mcontext).get(LAST_RECEIVE_MAIL_TIME);
		if (!TextUtils.isEmpty(secondstr)) {
			secondstr = secondstr.substring(0, secondstr.length() - 2);
			lastLoadMailMillSeconds = Long.valueOf(secondstr);
		}
		
		MailReceivePop3.getInstance().setLastLoadMailMillSeconds(lastLoadMailMillSeconds);
		
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				MailReceivePop3.getInstance().receiveAllMails();
			}
		});
		thread.start();
	}
	
	public void loadMailDetailByMessageIdInThread(final String messageId){
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				MailReceivePop3.getInstance().receiveOrReadLocalMailByMessageId(messageId);
			}
		});
		thread.start();
	}
	
	public List<MailDTO> showMailsInDB(Context context) {
		MailBeanDao dao = DBManager.getInstance(context).getDaoSession().getMailBeanDao();
		QueryBuilder<MailBean> qb = dao.queryBuilder();
		List<MailBean> list = qb.list();

		mailBeans = new ArrayList<MailDTO>();
		MailDTO mitem;
		for(MailBean _item: list){
			mitem = new MailDTO();
			mitem.mailBean = _item;
			mailBeans.add(mitem);
		}
		return mailBeans;
	}

	public void saveMail2DB(MailBean bean) {
		MailBeanDao dao = DBManager.getInstance(mcontext).getDaoSession().getMailBeanDao();
		dao.insert(bean);

		System.out.println("save mail 2 db --------------" + dao.count());
	}

	public void setLastRecevieMillSecond(){
		String secondstr = System.currentTimeMillis() + "ms";
		PreferencesManager.getInstance(mcontext).put(LAST_RECEIVE_MAIL_TIME, secondstr);
	}
	
	public List<MailDTO> getMailBeans() {
		return mailBeans;
	}

	public void sendMailInThread(final MailBean bean,final int type) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				if(type == SEND_SIMPLE_MAIL){
					MailSendSmtp.getInstance().sendMailSimple(bean);
				}else{
					MailSendSmtp.getInstance().sendMailWithAttachment(bean,MailSendSmtp.SEND_PIC_INNER_MODEL);
				}
			}
		});
		thread.start();
	}

	public void deleteMailInThread(Message message){
		try {
			message.setFlag(Flags.Flag.DELETED, true);
			message.saveChanges();  
			
		} catch (MessagingException e) {
			e.printStackTrace();
		}  
	}
	
	public void replyMailInThread(final Message message) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				MailSendSmtp.getInstance().replyMail(message);
			}
		});
		thread.start();
	}
}
