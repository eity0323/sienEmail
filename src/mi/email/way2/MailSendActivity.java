package mi.email.way2;
/**
 * @version 1.0
 * @author sien
 * @description 发送or回复页面
 */
import java.util.HashMap;

import javax.mail.Message;

import de.greenrobot.event.EventBus;
import mi.email.way2.api.MailConfig;
import mi.email.way2.control.MailManager;
import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import mi.learn.com.R;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class MailSendActivity extends BaseActivity implements OnClickListener{
	EditText fromET,subjectET,contentET;
	
	private boolean isReply = false;		//是否为邮件回复
	private Message curMessage = null;		//当前邮件
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.send_activity);
		
		EventBus.getDefault().register(this);
		
		isReply = false;
		
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
			sendOrReply();
			break;
		case R.id.cancel_btn:
			cancel();
			break;
			default:
				break;
		}
	}
	
	/*若为邮件回复，则接收当前邮件*/
	public void onEventMainThread(Message message){
		if(message != null){
			
			isReply = true;
			curMessage = message;
			
			MailDTO mitem = MailManager.getInstance().message2MailDTO(message);
			if(mitem == null){
				finish();
				return;
			}
			
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
	
	private void sendOrReply(){
		if(isReply){
			checkAndReply();
		}else{
			checkAndSend();
		}
	}
	
	private void checkAndReply(){
		if(curMessage != null){
			String content = contentET.getText().toString();
			
			if(TextUtils.isEmpty(content)){
				content = "";
			}
			
			MailBean bean = new MailBean();
			bean.setContent(content);
			
			MailDTO data = new MailDTO();
			data.mailBean = bean;
			data.mailMessage = curMessage;
			MailManager.getInstance().replyMail(data);
			
			cancel();
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
		
		MailDTO data = new MailDTO();
		
		MailBean bean = new MailBean();
		bean.setFrom(MailConfig.userName);
		bean.setToAddress(from);
		bean.setSubject(subject);
		bean.setContent(content);
		
		String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/log_raiyi.txt";
		String picpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa.jpg";
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("file1", filepath);
		map.put("pic1", picpath);
		
//		data.attachmentMap = map;
		data.mailBean = bean;
		
		send(data);
		
		cancel();
	}
	
	private void send(MailDTO data){
		MailManager.getInstance().sendMail(data);
	}
	
	private void cancel(){
		finish();
	}
	
}
