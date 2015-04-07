package mi.email.way2;
/**
 * @version 1.0
 * @author sien
 * @description 邮件详情页面
 */
import java.util.Date;
import java.util.List;

import javax.mail.Message;

import de.greenrobot.event.EventBus;
import mi.email.way2.control.MailManager;
import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import mi.email.way2.tools.MailEvent.deleteMailEvent;
import mi.email.way2.tools.MailEvent.searchMailsEvent;
import mi.learn.com.R;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;

public class MailDetailActivity extends BaseActivity implements OnClickListener{
	private String toAddress = "";		//收件方地址
	String mailId = "";	//邮件id
	private Message currentMessage;		//当前邮件
	
	final String mimetype = "text/html";  
    final String encoding = "utf-8";  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_mail_activity);
		
		Intent tent = getIntent();
		mailId = tent.getStringExtra("ID");
		
		if(TextUtils.isEmpty(mailId)){
			finish();
		}
		
		EventBus.getDefault().register(this);
		initLayout();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	
	private void initLayout(){
		findViewById(R.id.reply_mail).setOnClickListener(this);
		findViewById(R.id.del_mail).setOnClickListener(this);
		
		loadMailDetail();
	}
	
	private void loadMailDetail(){
		MailManager.getInstance().loadMailDetail(mailId);
	}
	
	/*获取邮件详情*/
	public void onEventMainThread(searchMailsEvent event){
		if(event != null){
			int status = event.getStatus();
			
			if(status == searchMailsEvent.STATUS_SUCCESS){
				List<MailDTO> messages = event.getDatas();
				if(messages.size() <= 0)	return;
				
				TextView subjectTV = (TextView)findViewById(R.id.subject_mail);
				TextView sendtimeTV = (TextView)findViewById(R.id.senddate_mail);
				TextView fromTV = (TextView)findViewById(R.id.from_mail);
				WebView contentWV = (WebView)findViewById(R.id.content_mail);
				
				MailDTO mitem = messages.get(0);
				MailBean mbean = mitem.mailBean;
				
				currentMessage = mitem.mailMessage;
				
				if(mbean != null){
					subjectTV.setText(mbean.getSubject());
					Date date = mitem.sendDate;
					if(date != null){
						String dateStr = date.getYear() + "-" + date.getMonth()  + "-" + date.getDate();
						sendtimeTV.setText(dateStr);
					}
					
					// 获得发件人的描述信息
					String from = mbean.getFrom();
					String toUname = from;
					if(from.contains("<")){
						toUname = from.substring(0, from.indexOf("<"));
						toAddress = from.substring(from.indexOf("<") + 1,from.indexOf(">"));
					}
					String fromstr = "收件人："+ toUname;
					fromTV.setText(fromstr);
					
					String content = mbean.getContent();
					if(!TextUtils.isEmpty(content))
						contentWV.loadDataWithBaseURL(null, content, mimetype, encoding, null);
				}
			}else{
				showToast("邮件加载失败");
			}
		}
	}
	
	/*删除邮件*/
	public void onEventMainThread(deleteMailEvent event){
		if(event != null){
			int status = event.getStatus();
			if(status == deleteMailEvent.STATUS_SUCCESS){
				showToast("删除失败");
				
				finish();
			}else{
				showToast("删除失败");
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.reply_mail:
			replyMail();
			break;
		case R.id.del_mail:
			deleteMail();
			break;
		}
	}
	
	/*回复邮件*/
	private void replyMail(){
		if(TextUtils.isEmpty(toAddress))	return;
		
		Intent t = new Intent();
		t.setClass(getApplicationContext(), MailSendActivity.class);
		t.putExtra("model", "reply");
		startActivity(t);
	}
	
	private void deleteMail(){
		if( !TextUtils.isEmpty(mailId) ){
			MailManager.getInstance().deleteMail(mailId);
		}
	}
	
	/*回复邮件时关闭邮件详情页*/
	public void onEventMainThread(String value){
		if(!TextUtils.isEmpty(value) && value.equals("mailSendActivityInited")){
			EventBus.getDefault().postSticky(currentMessage);

			finish();
		}
	}
	
}
