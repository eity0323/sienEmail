package mi.email.way2;

import java.util.List;

import javax.mail.Message;

import de.greenrobot.event.EventBus;
import mi.email.way2.control.MailConfig;
import mi.email.way2.control.MailEvent.loadMailsEvent;
import mi.email.way2.control.MailManager;
import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import mi.learn.com.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class MailSendActivity extends Activity implements OnClickListener{
	EditText fromET,subjectET,contentET;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_send_way2);
		
		EventBus.getDefault().register(this);
		
		initLayout();
		
		EventBus.getDefault().post("mailSendActivityInited");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	
	private void initLayout(){
		fromET = (EditText)findViewById(R.id.send_from);
		subjectET = (EditText)findViewById(R.id.send_subject);
		contentET = (EditText)findViewById(R.id.send_content);
		
		findViewById(R.id.send_btn).setOnClickListener(this);
		findViewById(R.id.cancel_btn).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.send_btn:
			checkAndSend();
			break;
		case R.id.cancel_btn:
			cancel();
			break;
			default:
				break;
		}
	}
	
	public void onEventMainThread(Message message){
		if(message != null){
			MailManager.getInstance().replyMailInThread(message);
		}
	}
	
	public void onEventMainThread(loadMailsEvent event){
		if(event != null){
			List<MailDTO> messages = event.getDatas();
			if(messages.size() <= 0)	return;
			
			MailDTO mitem = messages.get(0);
			MailBean mbean = mitem.mailBean;
			
			if(mbean != null){
				subjectET.setText("Re: " + mbean.getSubject());
				
				// 获得发件人的描述信息
				String from = mbean.getFrom();
				String toUname = from;
				if(from.contains("<")){
					toUname = from.substring(0, from.indexOf("<"));
				}
				String fromstr = "收件人："+ toUname;
				fromET.setText(fromstr);
			}
		}
	}
	
	private void checkAndSend(){
		String from = fromET.getText().toString();
		
		if(TextUtils.isEmpty(from)){
			Toast.makeText(getApplicationContext(), "请输入发送者", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String subject = subjectET.getText().toString();
		
		if(TextUtils.isEmpty(subject)){
			subject = "";
		}
		
		String content = contentET.getText().toString();
		
		if(TextUtils.isEmpty(content)){
			content = "";
		}
		
		MailBean bean = new MailBean();
		bean.setFrom(MailConfig.userName);
		bean.setToAddress(from);
		bean.setSubject(subject);
		bean.setContent(content);
		send(bean);
		
		finish();
	}
	
	private void send(MailBean bean){
		MailManager.getInstance().sendMailInThread(bean, MailManager.SEND_SIMPLE_MAIL);
	}
	
	private void cancel(){
		finish();
	}
	
}
