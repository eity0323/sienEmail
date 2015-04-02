package mi.email.way2;

import java.util.Date;
import java.util.List;

import javax.mail.Message;

import de.greenrobot.event.EventBus;
import mi.email.way2.control.MailConfig;
import mi.email.way2.control.MailEvent.searchMailsEvent;
import mi.email.way2.control.MailManager;
import mi.email.way2.control.MailSendSmtp;
import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import mi.learn.com.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;

public class MailDetailActivity extends Activity implements OnClickListener{
	private String toAddress = "";
	String mailId = "";
	private Message currentMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_way2);
		
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
		MailManager.getInstance().loadMailDetailByMessageIdInThread(mailId);
	}
	
	public void onEventMainThread(searchMailsEvent event){
		if(event != null){
			List<MailDTO> messages = event.getDatas();
			if(messages.size() <= 0)	return;
			
			TextView subjectTV = (TextView)findViewById(R.id.subject_mail);
			TextView sendtimeTV = (TextView)findViewById(R.id.senddate_mail);
			TextView fromTV = (TextView)findViewById(R.id.from_mail);
			WebView content = (WebView)findViewById(R.id.content_mail);
			
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
			
			break;
		}
	}
	
	private void replyMail(){
		if(TextUtils.isEmpty(toAddress))	return;
		
		Intent t = new Intent();
		t.setClass(getApplicationContext(), MailSendActivity.class);
		t.putExtra("model", "reply");
		startActivity(t);
	}
	
	public void onEventMainThread(String value){
		if(!TextUtils.isEmpty(value) && value.equals("mailSendActivityInited")){
			EventBus.getDefault().postSticky(currentMessage);

			finish();
		}
	}
	
}
