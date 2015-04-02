package mi.email.way2.control;
/**
 * @version 1.0
 * @author sien
 * @description 发送 and 回复邮件
 */
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import mi.email.way2.model.MailBean;
import android.os.Environment;

public class MailSendSmtp {
	private static MailSendSmtp instance;
	public static int SEND_PIC_INNER_MODEL = 1;
	public static int SEND_PIC_ATTACH_MODEL = 2;
	
	public static MailSendSmtp getInstance(){
		if(instance == null){
			instance = new MailSendSmtp();
		}
		return instance;
	}
	
	private Properties getSmtpProperties(boolean needAuth) {
		Properties p = new Properties();
		p.put("mail.smtp.host", MailConfig.hostServiceSmtp);
		p.put("mail.smtp.port",MailConfig.hostPortSmtp);
		p.put("mail.transport.protocol", MailConfig.hostProtocolSmtp);
		
		if(needAuth)
			p.put("mail.smtp.auth","true");//设置验证机制
		return p;
	}

	public void sendMailSimple(MailBean bean) {
		try {
			Session session = Session.getInstance(getSmtpProperties(false));
			Message message = mailBean2Message(session,bean);
			
			if(message != null){
				Transport tran = session.getTransport();
				tran.connect(MailConfig.hostServiceSmtp, MailConfig.hostPortSmtp, MailConfig.userName, MailConfig.password);// 连接到新浪邮箱服务器
				tran.sendMessage(message, new Address[] { new InternetAddress(bean.getToAddress()) });// 设置邮件接收人
				tran.close();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void sendMailWithAttachment(MailBean bean,int sendModel){
		try{
			MailAuthenticator authenticator = new MailAuthenticator(MailConfig.userName, MailConfig.password);
			Session session = Session.getInstance(getSmtpProperties(true),authenticator);
			session.setDebug(true);
			
			Message message = null;
			if(sendModel == SEND_PIC_ATTACH_MODEL)
				message = mailBean2MessageWithAttachment(session,bean);
			else
				message = mailBean2MessageWithInnerAttach(session, bean);
			
			Transport.send(message);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private Message mailBean2Message(Session session, MailBean bean){
		try {
			Message messgae = new MimeMessage(session);
			messgae.setFrom(new InternetAddress(bean.getFrom()));// 设置发送人
			messgae.setRecipients(RecipientType.TO,InternetAddress.parse(bean.getToAddress()));
			messgae.setText(bean.getContent());// 设置邮件内容
			messgae.setSubject(bean.getSubject());// 设置邮件主题
			
			return messgae;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private Message mailBean2MessageWithAttachment(Session session, MailBean bean){
		try{
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(MailConfig.userName));
			message.setSubject(bean.getSubject());
			message.setRecipients(RecipientType.TO,InternetAddress.parse(bean.getToAddress()));//接收人
			
			String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/log_raiyi.txt";
			String picpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa.jpg";
			MimeBodyPart bodyPartAttch = createAttachMent(filepath);//附件
			MimeBodyPart bodyPartContentAndPic = createContentAndAttachPic("hello world",picpath);//文本内容
			MimeMultipart mimeMuti = new MimeMultipart("mixed");
			mimeMuti.addBodyPart(bodyPartAttch);
			mimeMuti.addBodyPart(bodyPartContentAndPic);
			message.setContent(mimeMuti);
			
			message.saveChanges();
			
			return message;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
	
	private Message mailBean2MessageWithInnerAttach(Session session, MailBean bean){
		try{
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(MailConfig.userName));
			message.setSubject(bean.getSubject());
			message.setRecipients(RecipientType.TO,InternetAddress.parse(bean.getToAddress()));//接收人
			
			String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/log_raiyi.txt";
			String picpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa.jpg";
			MimeBodyPart bodyPartAttch = createAttachMent(filepath);//附件
			MimeBodyPart bodyPartContentAndPic = createContentAndInnerPic("hello world",picpath);
			
			MimeMultipart mimeMuti = new MimeMultipart("mixed");
			mimeMuti.addBodyPart(bodyPartAttch);
			mimeMuti.addBodyPart(bodyPartContentAndPic);
			message.setContent(mimeMuti);
			
			message.saveChanges();
			
			return message;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}

	// 创建附件
	private MimeBodyPart createAttachMent(String path) throws MessagingException {
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		FileDataSource dataSource = new FileDataSource(new File(path));
		mimeBodyPart.setDataHandler(new DataHandler(dataSource));
		mimeBodyPart.setFileName(dataSource.getName());
		return mimeBodyPart;
	}

	// 创建文本和附件形式图片
	private MimeBodyPart createContentAndAttachPic(String content, String path) throws MessagingException {
		MimeMultipart mimeMutiPart = new MimeMultipart("related");
		// 图片
		MimeBodyPart picBodyPart = new MimeBodyPart();
		FileDataSource fileDataSource = new FileDataSource(new File(path));
		picBodyPart.setDataHandler(new DataHandler(fileDataSource));
		picBodyPart.setFileName(fileDataSource.getName());
		mimeMutiPart.addBodyPart(picBodyPart);
		// 文本
		MimeBodyPart contentBodyPart = new MimeBodyPart();
		contentBodyPart.setContent(content, "text/html;charset=gbk");
		mimeMutiPart.addBodyPart(contentBodyPart);
		// 图片和文本结合
		MimeBodyPart allBodyPart = new MimeBodyPart();
		allBodyPart.setContent(mimeMutiPart);
		return allBodyPart;
	}
	
	//创建文本和内嵌图片
	public MimeBodyPart createContentAndInnerPic(String content,String path) throws MessagingException, UnsupportedEncodingException{
	    // 创建一个MIME子类型为“related”的MimeMultipart对象  
        MimeMultipart mp = new MimeMultipart("related");  
        // 创建一个表示正文的MimeBodyPart对象，并将它加入到前面创建的MimeMultipart对象中  
        MimeBodyPart htmlPart = new MimeBodyPart();  
        mp.addBodyPart(htmlPart);  
        // 创建一个表示图片资源的MimeBodyPart对象，将将它加入到前面创建的MimeMultipart对象中  
        MimeBodyPart imagePart = new MimeBodyPart();  
        mp.addBodyPart(imagePart);  

        // 设置内嵌图片邮件体  
        FileDataSource ds = new FileDataSource(new File(path));  
        DataHandler dh = new DataHandler(ds);  
        imagePart.setDataHandler(dh);  
        imagePart.setContentID(getUUIDNumber());  // 设置内容编号,用于其它邮件体引用  

        // 创建一个MIME子类型为"alternative"的MimeMultipart对象，并作为前面创建的htmlPart对象的邮件内容  
        MimeMultipart htmlMultipart = new MimeMultipart("alternative");  
        // 创建一个表示html正文的MimeBodyPart对象  
        MimeBodyPart htmlBodypart = new MimeBodyPart();  
        // 其中cid=androidlogo.gif是引用邮件内部的图片，即imagePart.setContentID("androidlogo.gif");方法所保存的图片  
        htmlBodypart.setContent(content+"<img src=\"cid:"+ getUUIDNumber() +"\" />","text/html;charset=utf-8");  
        htmlMultipart.addBodyPart(htmlBodypart);  
        htmlPart.setContent(htmlMultipart);  
        
        MimeBodyPart allBodyPart = new MimeBodyPart();
		allBodyPart.setContent(mp);
		return allBodyPart;
	}

	private String getUUIDNumber() {
		return UUID.randomUUID().toString();
	}
	
	public void replyMail(Message message){
//		try {
//	         InternetAddress address;     
//             address = (InternetAddress)message.getFrom()[0];     
//             if(address != null) {     
//                 System.out.println(address.getPersonal());     
//             }   
//             if (null != address) {     
//                 MimeMessage replyMessage = (MimeMessage) message.reply(false);     
//                 replyMessage.setFrom(new InternetAddress(MailConfig.userName));  
//                 replyMessage.setRecipients(MimeMessage.RecipientType.TO, address.getAddress());  
//                 replyMessage.setText("这是回复邮件，不知道能否成功！");    
//                 
////                 String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/log_raiyi.txt";
////     			 String picpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa.jpg";
////     			 MimeBodyPart bodyPartAttch = createAttachMent(filepath);//附件
////     			 MimeBodyPart bodyPartContentAndPic = createContentAndInnerPic("hello world",picpath);
////     			
////     			 MimeMultipart mimeMuti = new MimeMultipart("mixed");
////     			 mimeMuti.addBodyPart(bodyPartAttch);
////     			 mimeMuti.addBodyPart(bodyPartContentAndPic);
////     			replyMessage.setContent(mimeMuti);
//     			
//                 replyMessage.saveChanges();  
//                 
//                 Session session = Session.getInstance(getSmtpProperties(true));
//                 Transport tran = session.getTransport(MailConfig.hostProtocolSmtp);
//     			 tran.connect(MailConfig.hostServiceSmtp, MailConfig.hostPortSmtp, MailConfig.userName, MailConfig.password);// 连接到新浪邮箱服务器
//     			Transport.send(replyMessage, new Address[] { new InternetAddress(address.getAddress()) });
//             }     
//		} catch (Exception e) {
//			e.printStackTrace();
//		}    
		
		replySimple(message);
	}
	
	private void replySimple(Message message) {
		try {
			InternetAddress address;
			address = (InternetAddress) message.getFrom()[0];
			if (address != null) {
				System.out.println(address.getPersonal());
			}
			if (null != address) {
				MimeMessage replyMessage = (MimeMessage) message.reply(false);
				replyMessage.setFrom(new InternetAddress(MailConfig.userName));
				replyMessage.setRecipients(MimeMessage.RecipientType.TO, address.getAddress());
				replyMessage.setText("这是回复邮件，不知道能否成功！");
				replyMessage.saveChanges();

				Session session = Session.getInstance(getSmtpProperties(true));
				Transport tran = session.getTransport();
				tran.connect(MailConfig.hostServiceSmtp, 25, MailConfig.userName, MailConfig.password);// 连接到新浪邮箱服务器
				tran.sendMessage(replyMessage, new Address[] { new InternetAddress(address.getAddress()) });// 设置邮件接收人
				tran.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void replyWithAttachPic(Message message) {
		try {
			InternetAddress address;
			address = (InternetAddress) message.getFrom()[0];
			if (address != null) {
				System.out.println(address.getPersonal());
			}
			if (null != address) {
				MimeMessage replyMessage = (MimeMessage) message.reply(false);
				replyMessage.setFrom(new InternetAddress(MailConfig.userName));
				replyMessage.setRecipients(MimeMessage.RecipientType.TO, address.getAddress());

				String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/log_raiyi.txt";
				String picpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa.jpg";
				MimeBodyPart bodyPartAttch = createAttachMent(filepath);// 附件
				MimeBodyPart bodyPartContentAndPic = createContentAndAttachPic("hello world", picpath);

				MimeMultipart mimeMuti = new MimeMultipart("mixed");
				mimeMuti.addBodyPart(bodyPartAttch);
				mimeMuti.addBodyPart(bodyPartContentAndPic);
				replyMessage.setContent(mimeMuti);

				replyMessage.saveChanges();

				Session session = Session.getInstance(getSmtpProperties(true));
				Transport tran = session.getTransport();
				tran.connect(MailConfig.hostServiceSmtp, 25, MailConfig.userName, MailConfig.password);// 连接到新浪邮箱服务器
				tran.sendMessage(replyMessage, new Address[] { new InternetAddress(address.getAddress()) });// 设置邮件接收人
				tran.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
