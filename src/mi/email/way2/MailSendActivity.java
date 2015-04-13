package mi.email.way2;
/**
 * @version 1.0
 * @author sien
 * @description 发送or回复页面
 */
import java.util.HashMap;

import javax.mail.Message;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;
import mi.email.way2.api.MailConfig;
import mi.email.way2.control.MailManager;
import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import mi.email.way2.tools.MailEvent.sendMailEvent;
import mi.learn.com.R;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

@EActivity(R.layout.send_activity)
public class MailSendActivity extends BaseActivity{
	@ViewById(R.id.send_from)
	EditText fromET;
	@ViewById(R.id.send_subject)
	EditText subjectET;
	@ViewById(R.id.send_content)
	EditText contentET;
	
	private boolean isReply = false;		//是否为邮件回复
	private Message curMessage = null;		//当前邮件
	
	@AfterViews
	void initRes(){
		EventBus.getDefault().register(this);
		
		isReply = false;
		
		EventBus.getDefault().post("mailSendActivityInited");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	
	public void onEventMainThread(sendMailEvent event){
		if(event != null){
			int status = event.getStatus();
			String err = event.getDatas();
			if(status == sendMailEvent.STATUS_SUCCESS){
				cancel();
			}else{
				showToast(err);
			}
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
	
	@Click(R.id.send_btn)
	void sendOrReply(){
		if(isReply){
			checkAndReply();
		}else{
			checkAndSend();
		}
	}
	
	@Click(R.id.cancel_btn)
	void cancel(){
		finish();
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
	}
	
	private void send(MailDTO data){
		MailManager.getInstance().sendMail(data);
	}
}
