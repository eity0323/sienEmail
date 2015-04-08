package mi.email.way2.control;
/**
 * @version 1.0
 * @author sien
 * @description 邮箱管理类
 */
import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;

import mi.email.way2.api.IMailManager;
import mi.email.way2.api.IMailReceiver;
import mi.email.way2.api.IMailSender;
import mi.email.way2.db.DBManager;
import mi.email.way2.db.MailBeanDao;
import mi.email.way2.impl.MailReceiver;
import mi.email.way2.impl.MailSender;
import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import android.content.Context;
import android.text.TextUtils;
import de.greenrobot.dao.query.QueryBuilder;

public class MailManager implements IMailManager{
	private static MailManager instance;
	
	private IMailSender sendInstance; 
	private IMailReceiver receiveInstance;

	private List<MailDTO> mailBeans;
	private Context mcontext;

	private long lastLoadMailMillSeconds = -1;

	private final String LAST_RECEIVE_MAIL_TIME = "lastReceiveMailMillSeconds";

	public static MailManager getInstance() {
		if (instance == null) {
			instance = new MailManager();
		}
		return instance;
	}
	
	@Override
	public void loadAllMails(Context context){
		receiveAllMailInThread(context);
	}
	
	@Override
	public void loadMailDetail(String messageId){
		loadMailDetailByMessageIdInThread(messageId);
	}
	
	@Override
	public void sendMail(MailDTO data) {
		sendMailInThread(data);
	}

	@Override
	public void deleteMail(String messageId) {
		deleteMailInThread(messageId);
	}

	@Override
	public void replyMail(MailDTO data) {
		replyMailInThread(data);
	}
	
	public MailDTO message2MailDTO(Message message){
		MailDTO mitem = getReceiveInstance().message2MailDTO(message);
		return mitem;
	}
	
	private IMailSender getSendInstance(){
		if(sendInstance == null){
			sendInstance = new MailSender();
		}
		
		return sendInstance;
	}
	
	private IMailReceiver getReceiveInstance(){
		if(receiveInstance == null){
			receiveInstance = new MailReceiver();
		}
		
		return receiveInstance;
	}
	
	/**
	 * 收邮件
	 */
	private void receiveAllMailInThread(Context context){
		mcontext = context;

		lastLoadMailMillSeconds = -1;
		String secondstr = PreferencesManager.getInstance(mcontext).get(LAST_RECEIVE_MAIL_TIME);
		if (!TextUtils.isEmpty(secondstr)) {
			secondstr = secondstr.substring(0, secondstr.length() - 2);
			lastLoadMailMillSeconds = Long.valueOf(secondstr);
		}
		
		getReceiveInstance().setLastLoadMailMillSeconds(lastLoadMailMillSeconds);
		
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				getReceiveInstance().loadMails();
			}
		});
		thread.start();
	}
	
	private void loadMailDetailByMessageIdInThread(final String messageId){
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				getReceiveInstance().loadMailDetail(messageId);
			}
		});
		thread.start();
	}
	
	private void sendMailInThread(final MailDTO data) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				getSendInstance().sendMail(data);
			}
		});
		thread.start();
	}

	private void deleteMailInThread(final String messageId){
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				getReceiveInstance().deleteMailByMessageId(messageId);
			}
		});
		thread.start();
	}
	
	private void replyMailInThread(final MailDTO data) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				getSendInstance().replyMail(data);
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
}
