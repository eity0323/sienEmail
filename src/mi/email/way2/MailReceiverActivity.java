package mi.email.way2;
/**
 * @version 1.0
 * @author sien
 * @description 邮件列表
 */
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import de.greenrobot.event.EventBus;
import mi.email.way2.adapter.MailAdapter;
import mi.email.way2.control.MailManager;
import mi.email.way2.model.MailDTO;
import mi.email.way2.tools.MailEvent.loadMailsEvent;
import mi.email.way2.views.LoadDialog;
import mi.learn.com.R;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * 邮件接收器，目前支持pop3协议。 能够接收文本、HTML和带有附件的邮件
 */
public class MailReceiverActivity extends BaseActivity {

	List<MailDTO> mails = new ArrayList<MailDTO>();//邮件列表数据源
	private MailAdapter adapter;
	
	protected static final int Menu_About = Menu.FIRST;
	protected static final int Menu_Exit = Menu.FIRST+1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reclist_acitivity);
		
		mails = MailManager.getInstance().showMailsInDB(this);
		if(mails == null){
			mails = new ArrayList<MailDTO>();
		}

		ListView mailList = (ListView) findViewById(R.id.mailist);
		adapter = new MailAdapter(getApplicationContext(), mails);
		mailList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(mails == null || mails.size() <= arg2)	return;
				
				String messageId = mails.get(arg2).mailBean.getMessageId();
				
				if(!TextUtils.isEmpty(messageId)){
					Intent intent = new Intent();
					intent.putExtra("ID", messageId);
					intent.setClass(MailReceiverActivity.this, MailDetailActivity.class);
					startActivity(intent);
				}
			}
		});
		mailList.setAdapter(adapter);

		EventBus.getDefault().register(this);

		showLoadingPanel();

		try {
			getAllMail();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	
	/**
	 * 获取messages中的所有邮件
	 * 
	 * @throws MessagingException
	 */
	private void getAllMail() {
		MailManager.getInstance().loadAllMails(this);
	}

	/*加载邮件列表*/
	public void onEventMainThread(loadMailsEvent event) {
		if (event != null) {
			int status = event.getStatus();
			if (status == loadMailsEvent.STATUS_SUCCESS) {

				List<MailDTO> _mails = event.getDatas();
				if(_mails != null && adapter != null){
					mails = _mails;
					adapter.setData(mails);
				}

			} else {
				showToast("邮件加载失败");
			}
		}

		hideLoadingPanel();
	}

	private void showLoadingPanel() {
		LoadDialog.show(this);
	}

	private void hideLoadingPanel() {
		LoadDialog.dismiss(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0,Menu_About,0,"撰写").setIcon(R.drawable.ic_launcher);
		menu.add(0,Menu_Exit,0,"退出").setIcon(R.drawable.ic_launcher);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case Menu_About:
			go2NewMailActivity();
			break;
		case Menu_Exit:
			break;
		}
		return true;
	}
	
	/*发送新邮件*/
	private void go2NewMailActivity(){
		Intent t = new Intent();
		t.setClass(getApplicationContext(), MailSendActivity.class);
		startActivity(t);
	}
}
