package mi.email.way2;
/**
 * @version 1.0
 * @author sien
 * @description 邮件列表
 */
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;
import mi.email.way2.adapter.MailAdapter;
import mi.email.way2.control.MailManager;
import mi.email.way2.model.MailDTO;
import mi.email.way2.tools.MailEvent.loadMailsEvent;
import mi.email.way2.views.LoadDialog;
import mi.learn.com.R;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.ListView;

/**
 * 邮件接收器，目前支持pop3协议。 能够接收文本、HTML和带有附件的邮件
 */
@EActivity(R.layout.reclist_acitivity)
@OptionsMenu(R.menu.options_menu)
public class MailReceiverActivity extends BaseActivity {

	@ViewById(R.id.mailist)
	ListView mailList;
	
	private MailAdapter adapter;
	
	List<MailDTO> mails = new ArrayList<MailDTO>();//邮件列表数据源

	@AfterViews
	void initRes(){
		mails = MailManager.getInstance().showMailsInDB(this);
		if(mails == null){
			mails = new ArrayList<MailDTO>();
		}

		adapter = new MailAdapter(getApplicationContext(), mails);
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
	
	@ItemClick(R.id.mailist)
	void mailListItemClicked(MailDTO clickItem){
		if(clickItem != null){
			String messageId = clickItem.mailBean.getMessageId();
			
			if(!TextUtils.isEmpty(messageId)){
				Intent intent = new Intent();
				intent.putExtra("ID", messageId);
				intent.setClass(MailReceiverActivity.this, MailDetailActivity_.class);
				startActivity(intent);
			}
		}
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
	
	/*发送新邮件*/
	@OptionsItem(R.id.menuNew)
	void go2NewMailActivity(){
		Intent t = new Intent();
		t.setClass(getApplicationContext(), MailSendActivity.class);
		startActivity(t);
	}
	
	@OptionsItem(R.id.menuExit)
	void exitActivity(){
		
	}
}
