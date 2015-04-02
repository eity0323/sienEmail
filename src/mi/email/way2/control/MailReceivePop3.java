package mi.email.way2.control;
/**
 * @version 1.0
 * @author sien
 * @description 接收 and 解析邮件
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;

import android.text.TextUtils;
import mi.email.way2.control.MailEvent.connectMailServiceEvent;
import mi.email.way2.control.MailEvent.loadMailsEvent;
import mi.email.way2.control.MailEvent.searchMailsEvent;
import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import de.greenrobot.event.EventBus;

public class MailReceivePop3 {
	private static MailReceivePop3 instance;
	
	private Store mailStore;
	private Folder mailFolder;
	private Message[] mails;
	private Message currentMail;
	private List<MailDTO> mailBeans;
	
	private long lastLoadMailMillSeconds = -1;
	private String currentEmailFileName;
	private boolean isLoadByMessageId = false;
	private String messageId = "";
	private boolean need2SaveMail = false;
	private boolean need2ParseMailDetail = false;
	
	public static MailReceivePop3 getInstance(){
		if(instance == null){
			instance = new MailReceivePop3();
		}
		return instance;
	}
	
	public void receiveOrReadLocalMailByMessageId(String messageId){
		String fileName = MailConfig.emailDir + getInfoBetweenBrackets(messageId) + MailConfig.emailFileSuffix;
		File _file = new File(fileName);
		
		if(_file.exists()){
			readLocalEmlFile(fileName);
		}else{
			receiveMailByMessageId(messageId);
		}
	}
	
	private void receiveMailByMessageId(String messageId){
		isLoadByMessageId = true;
		this.messageId = messageId;
		
		need2ParseMailDetail = true;
		need2SaveMail = true;
		
		connectToServer();
	}
	
	private void readLocalEmlFile(String fileName){
		try {
			InputStream emlfis = new FileInputStream(fileName);
			Session mailSession = Session.getDefaultInstance(System.getProperties(), null);

			MimeMessage msg = new MimeMessage(mailSession, emlfis);
			
			if(msg != null){
				currentMail = msg;
				mails = new Message[1];
				mails[0] = msg;
				
				need2SaveMail = false;
				
				saveAndParseDetailMessage(currentMail);
			}
			EventBus.getDefault().post(new MailEvent.searchMailsEvent(searchMailsEvent.STATUS_SUCCESS, messages2MailBeans(mails)));
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void receiveAllMails(){
		isLoadByMessageId = false;
		connectToServer();
	}

	private void connectToServer() {
		boolean res = checkAndInitStore();
		
		if(res){
			connectAndOpenMailBox();
		}
	}
	
	private boolean checkAndInitStore(){
		boolean res = true;
		if(mailStore != null)	return res;
		
		Session session = Session.getInstance(getPopProperties(), getAuthenticator());
		try {
			mailStore = session.getStore(MailConfig.hostProtocolPop3);
			mailStore.addConnectionListener(mailConnectionListener);
			
		} catch (Exception e) {
			res = false;
			
			connectMailFailed();
		}
		return res;
	}
	
	private void connectAndOpenMailBox(){
		boolean res = connect();
		if(res){
			openInBoxFolder();
		}
	}
	
	private boolean connect(){
		boolean res = false;
		try {
			mailStore.connect();
			res = true;
			
		} catch (MessagingException e) {
			res = false;
			
			connectMailFailed();
			e.printStackTrace();
		}
		
		return res;
	}

	private void connectMailFailed() {
		connectMailFailed("连接服务器失败！");
	}
	
	private void connectMailFailed(String msg) {
		EventBus.getDefault().post(new MailEvent.connectMailServiceEvent(connectMailServiceEvent.STATUS_FAILED, msg));
	}

	private MailAuthenticator getAuthenticator(){
		MailAuthenticator authenticator = new MailAuthenticator();
		return authenticator;
	}
	
	private Properties getPopProperties() {
		Properties p = new Properties();
		p.put("mail.pop3.host", MailConfig.hostServicePop3);
		p.put("mail.pop3.port", MailConfig.hostPortPop3);
		return p;
	}
	
	private ConnectionListener mailConnectionListener = new ConnectionListener() {

		@Override
		public void opened(ConnectionEvent arg0) {
			System.out.println("mailConnectionListener----------------------------open");
			loadMailFromMailBox();
		}

		@Override
		public void disconnected(ConnectionEvent arg0) {
			System.out.println("mailConnectionListener----------------------------disconnected");
		}

		@Override
		public void closed(ConnectionEvent arg0) {
			System.out.println("mailConnectionListener----------------------------closed");
		}
	};
	
	private boolean openInBoxFolder() {
		boolean res = false;
		try {
			mailFolder = mailStore.getFolder("INBOX");
			mailFolder.open(Folder.READ_WRITE);
			
			res = true;
		} catch (MessagingException e) {
			
			e.printStackTrace();
			connectMailFailed("打开收件箱失败！");
		}
		return res;
	}

	private boolean closeConnection() {
		try {
			if (mailFolder != null && mailFolder.isOpen()) {
				mailFolder.close(true);
			}
			mailStore.close();
			return true;
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return false;
	}

	private void loadMailFromMailBox(){
		if(isLoadByMessageId)
			getMailDetailByMessageId();
		else
			getAllOrLastestMailList();
	}
	
	private void getMailDetailByMessageId(){
		getMailByMessageId(messageId);
		
		if(mails != null){
			currentMail = mails[0];
			saveAndParseDetailMessage(currentMail);
		}
		EventBus.getDefault().post(new MailEvent.searchMailsEvent(searchMailsEvent.STATUS_SUCCESS, messages2MailBeans(mails)));
		closeConnection();
	}
	
	private void getAllOrLastestMailList() {
		if (lastLoadMailMillSeconds == -1) {
			getAllMailist();
		} else {
			getLastestMailist();
		}

		if(mails != null){
			parseMails(mails);
			MailManager.getInstance().setLastRecevieMillSecond();
			
			EventBus.getDefault().post(new MailEvent.loadMailsEvent(loadMailsEvent.STATUS_SUCCESS, mailBeans));
		}
		closeConnection();
	}
	
	private void getMailByMessageId(String messageId){
		try{
			if(!TextUtils.isEmpty(messageId)){
				SearchTerm st = new MessageIDTerm(messageId);
				mails = mailFolder.search(st);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			connectMailFailed("邮件解析错误！");
		}
	}
	
	private void getLastestMailist(){
		getMailistFromMillSecond(lastLoadMailMillSeconds);
	}
	
	private void getMailistFromMillSecond(long millSec){
		if(mailFolder == null)	return;
		
		try{
			Date nowDate = new Date(millSec);
			SearchTerm st = new SentDateTerm(ComparisonTerm.GE, nowDate);
			mails = mailFolder.search(st);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void getAllMailist(){
		try{
			mails = mailFolder.getMessages();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void parseMails(Message[] array){
		try{
			if (array.length > 0) {
				mailBeans = new ArrayList<MailDTO>();
	
				System.out.println("总的邮件数目：" + array.length);
				System.out.println("新邮件数目：" + getNewMessageCount());
				System.out.println("未读邮件数目：" + getUnreadMessageCount());
				
				// 将要下载的邮件的数量。
				int mailArrayLength = getMessageCount();
				System.out.println("一共有邮件" + mailArrayLength + "封");
				
				for (int index = 0; index < mailArrayLength; index++) {
					saveMailBasicInfo(array[index]);
					
					if(need2SaveMail || need2ParseMailDetail){
						saveAndParseDetailMessage(array[index]); //获取当前message
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private void saveMailBasicInfo(Message message) {
		MailDTO item = message2MailDTO(message);
		MailBean bean = item.mailBean;
		if(bean != null){
			mailBeans.add(item);
			
			saveMail2DB(bean);
		}
	}

	private void saveMail2DB(MailBean bean) {
		MailManager.getInstance().saveMail2DB(bean);
	}
	
	private String getTOAddress(Message message) throws Exception {
		return getMailAddress("TO", message);
	}
	
	private String getCCAddress(Message message) throws Exception {
		return getMailAddress("CC", message);
	}

	private String getBCCAddress(Message message) throws Exception {
		return getMailAddress("BCC", message);
	}

	/**
	 * 获得邮件地址
	 * 
	 * @param type
	 *            类型，如收件人、抄送人、密送人
	 * @param mimeMessage
	 *            邮件消息
	 * @return
	 * @throws Exception
	 */
	private String getMailAddress(String type, Message mimeMessage) throws Exception {
		String mailaddr = "";
		String addtype = type.toUpperCase();
		InternetAddress[] address = null;
		if (addtype.equals("TO") || addtype.equals("CC") || addtype.equals("BCC")) {
			if (addtype.equals("TO")) {
				address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);
			} else if (addtype.equals("CC")) {
				address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);
			} else {
				address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);
			}
			if (address != null) {
				for (int i = 0; i < address.length; i++) {
					// 先获取邮件地址
					String email = address[i].getAddress();
					if (email == null) {
						email = "";
					} else {
						email = MimeUtility.decodeText(email);
					}
					// 再取得个人描述信息
					String personal = address[i].getPersonal();
					if (personal == null) {
						personal = "";
					} else {
						personal = MimeUtility.decodeText(personal);
					}
					// 将个人描述信息与邮件地址连起来
					String compositeto = personal + "<" + email + ">";
					// 多个地址时，用逗号分开
					mailaddr += "," + compositeto;
				}
				mailaddr = mailaddr.substring(1);
			}
		} else {
			throw new Exception("错误的地址类型！!");
		}
		return mailaddr;
	}
	
	private String getPersonal(Message mimeMessage) throws Exception{
		InternetAddress[] address = (InternetAddress[]) mimeMessage.getFrom();
		
		// 获得发件人的描述信息
		String personal = address[0].getPersonal();
		if (personal == null) {
			personal = "";
		}
		return personal;
	}

	/**
	 * 获得发件人的地址和姓名
	 * 
	 * @throws Exception
	 */
	private String getFrom(Message mimeMessage) throws Exception {
		InternetAddress[] address = (InternetAddress[]) mimeMessage.getFrom();
		// 获得发件人的邮箱
		String from = address[0].getAddress();
		if (from == null) {
			from = "";
		}
		// 获得发件人的描述信息
		String personal = address[0].getPersonal();
		if (personal == null) {
			personal = "";
		}
		// 拼成发件人完整信息
		String fromaddr = personal + "<" + from + ">";
		return fromaddr;
	}

	/**
	 * 获取messages中message的数量
	 * 
	 * @return
	 */
	private int getMessageCount() {
		return mails.length;
	}

	/**
	 * 获得收件箱中新邮件的数量
	 * 
	 * @return
	 * @throws MessagingException
	 */
	private int getNewMessageCount() throws MessagingException {
		return mailFolder.getNewMessageCount();
	}

	/**
	 * 获得收件箱中未读邮件的数量
	 * 
	 * @return
	 * @throws MessagingException
	 */
	private int getUnreadMessageCount() throws MessagingException {
		return mailFolder.getUnreadMessageCount();
	}

	/**
	 * 获得邮件主题
	 */
	private String getSubject(Message mimeMessage) throws MessagingException {
		String subject = "";
		try {
			// 将邮件主题解码
			subject = MimeUtility.decodeText(mimeMessage.getSubject());
			if (subject == null) {
				subject = "";
			}
		} catch (Exception exce) {
		}
		return subject;
	}

	/**
	 * 获得邮件发送日期
	 */
	private Date getSentDate(Message mimeMessage) throws Exception {
		return mimeMessage.getSentDate();
	}

	/**
	 * 判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false"
	 */
	private boolean getReplySign(Message mimeMessage) throws MessagingException {
		boolean replysign = false;
		String needreply[] = mimeMessage.getHeader("Disposition-Notification-To");
		if (needreply != null) {
			replysign = true;
		}
		return replysign;
	}

	/**
	 * 获得此邮件的Message-ID
	 */
	private String getMessageId(Message mimeMessage) throws MessagingException {
		return ((MimeMessage) mimeMessage).getMessageID();
	}
	
	private String getContent(){
		return "";
	}

	/**
	 * 判断此邮件是否已读，如果未读返回返回false,反之返回true
	 */
	private boolean isNew(Message mimeMessage) throws MessagingException {
		boolean isnew = false;
		Flags flags = mimeMessage.getFlags();
		Flags.Flag[] flag = flags.getSystemFlags();
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == Flags.Flag.SEEN) {
				isnew = true;
				break;
			}
		}
		return isnew;
	}

	/**
	 * 判断此邮件是否包含附件
	 */
	private boolean isContainAttach(Part part) throws Exception {
		boolean attachflag = false;
		if (part.isMimeType("multipart/*")) {
			// 如果邮件体包含多部分
			Multipart mp = (Multipart) part.getContent();
			// 遍历每部分
			for (int i = 0; i < mp.getCount(); i++) {
				// 获得每部分的主体
				BodyPart bodyPart = mp.getBodyPart(i);
				String disposition = bodyPart.getDisposition();
				if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE)))) {
					attachflag = true;
				} else if (bodyPart.isMimeType("multipart/*")) {
					attachflag = isContainAttach((Part) bodyPart);
				} else {
					String contype = bodyPart.getContentType();
					if (contype.toLowerCase().indexOf("application") != -1) {
						attachflag = true;
					}
					if (contype.toLowerCase().indexOf("name") != -1) {
						attachflag = true;
					}
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			attachflag = isContainAttach((Part) part.getContent());
		}
		return attachflag;
	}

	/**
	 * 获得当前邮件
	 */
	private void saveAndParseDetailMessage(Message message){
		if(need2SaveMail){
			saveMessageAsFile(message);
		}
		
		if(need2ParseMailDetail){
			parseMessage(message);
		}
	}

	/**
	 * 保存邮件源文件
	 */
	private void saveMessageAsFile(Message message) {
		try {
			// 将邮件的ID中尖括号中的部分做为邮件的文件名
			String oriFileName = getInfoBetweenBrackets(getMessageId(message).toString());
			
			if(TextUtils.isEmpty(oriFileName)){
				return;
			}
			// 设置文件后缀名。若是附件则设法取得其文件后缀名作为将要保存文件的后缀名，
			// 若是正文部分则用.htm做后缀名
			String emlName = oriFileName;
			String fileNameWidthExtension = MailConfig.emailDir + oriFileName + MailConfig.emailFileSuffix;
			File storeFile = new File(fileNameWidthExtension);
			for (int i = 0; storeFile.exists(); i++) {
				emlName = oriFileName + i;
				fileNameWidthExtension = MailConfig.emailDir + emlName + MailConfig.emailFileSuffix;
				storeFile = new File(fileNameWidthExtension);
			}
			currentEmailFileName = emlName;
			System.out.println("邮件消息的存储路径: " + fileNameWidthExtension);
			// 将邮件消息的内容写入ByteArrayOutputStream流中
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			message.writeTo(baos);
			// 读取邮件消息流中的数据
			StringReader in = new StringReader(baos.toString());
			// 存储到文件
			Helper.saveFile(fileNameWidthExtension, in);
			
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 解析邮件
	 */
	private void parseMessage(Message message){
		try{
			Object content = message.getContent();
			if (content instanceof Multipart) {
				handleMultipart((Multipart) content);
			} else {
				handlePart(message);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/*
	 * 解析Multipart
	 */
	private void handleMultipart(Multipart multipart) throws MessagingException, IOException {
		for (int i = 0, n = multipart.getCount(); i < n; i++) {
			handlePart(multipart.getBodyPart(i));
		}
	}

	/*
	 * 解析指定part,从中提取文件
	 */
	private void handlePart(Part part) throws MessagingException, IOException {
		String disposition = part.getDisposition();
		String contentType = part.getContentType();
		String fileNameWidthExtension = "";
		// 获得邮件的内容输入流
		InputStreamReader sbis = new InputStreamReader(part.getInputStream());
		// 没有附件的情况
		if (disposition == null) {
			if ((contentType.length() >= 10) && (contentType.toLowerCase().substring(0, 10).equals("text/plain"))) {
				fileNameWidthExtension = MailConfig.attachmentDir + currentEmailFileName + ".txt";
			} else if ((contentType.length() >= 9) // Check if html
					&& (contentType.toLowerCase().substring(0, 9).equals("text/html"))) {
				fileNameWidthExtension = MailConfig.attachmentDir + currentEmailFileName + ".html";
			} else if ((contentType.length() >= 9) // Check if html
					&& (contentType.toLowerCase().substring(0, 9).equals("image/gif"))) {
				fileNameWidthExtension = MailConfig.attachmentDir + currentEmailFileName + ".gif";
			} else if ((contentType.length() >= 11) && contentType.toLowerCase().substring(0, 11).equals("multipart/*")) {
				handleMultipart((Multipart) part.getContent());
			} else { // Unknown type
				fileNameWidthExtension = MailConfig.attachmentDir + currentEmailFileName + ".txt";
			}
			// 存储内容文件
			System.out.println("保存邮件内容到：" + fileNameWidthExtension);
			
			if(!TextUtils.isEmpty(currentEmailFileName)){
				Helper.saveFile(fileNameWidthExtension, sbis);
			}

			return;
		}

		// 各种有附件的情况
		String name = "";
		if (disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
			name = getFileName(part);
			fileNameWidthExtension = MailConfig.attachmentDir + name;
		} else if (disposition.equalsIgnoreCase(Part.INLINE)) {
			name = getFileName(part);
			fileNameWidthExtension = MailConfig.attachmentDir + name;
		} 
		
		// 存储各类附件
		if (!fileNameWidthExtension.equals("")) {
			System.out.println("保存邮件附件到：" + fileNameWidthExtension);
			Helper.saveFile(fileNameWidthExtension, sbis);
		}
	}
	
	/**
	 * 获得尖括号之间的字符
	 * 
	 * @param str
	 * @return
	 * @throws Exception
	 */
	private String getInfoBetweenBrackets(String str){
		int i, j; // 用于标识字符串中的"<"和">"的位置
		if (str == null) {
			str = "error";
			return str;
		}
		i = str.lastIndexOf("<");
		j = str.lastIndexOf(">");
		if (i != -1 && j != -1) {
			str = str.substring(i + 1, j);
		}
		
		str = str.replace(".", "___");
		str = str.replace("@", "--");
		return str;
	}
	
	private String getFileName(Part part) throws MessagingException, UnsupportedEncodingException {
		String fileName = part.getFileName();
		fileName = MimeUtility.decodeText(fileName);
		String name = fileName;
		if (fileName != null) {
			int index = fileName.lastIndexOf("/");
			if (index != -1) {
				name = fileName.substring(index + 1);
			}
		}
		return name;
	}
	
	private List<MailDTO> messages2MailBeans(Message[] messages){
		List<MailDTO> list = null;
		
		MailDTO _bean = null;
		if(messages != null){
			list = new ArrayList<MailDTO>();
			
			for(Message _msgitem : messages){
				_bean = message2MailDTO(_msgitem);
				
				if(_bean != null){
					list.add(_bean);
				}
			}
		}
		
		return list;
	}
	
	private MailDTO message2MailDTO(Message message){
		try{
			String from = getFrom(message);
			String messageId = getMessageId(message);
			String toAddress = getTOAddress(message);
			String ccAddress = getCCAddress(message);
			String bccAddress = getBCCAddress(message);
			String subject = getSubject(message);
			Date sentDate = getSentDate(message);
			
			boolean replySign = getReplySign(message);
			boolean isnew = isNew(message);
			
			System.out.println("-------- 邮件ID：" + messageId + " ---------");
			System.out.println("From：" + from);
			System.out.println("To：" + toAddress);
			System.out.println("CC：" + ccAddress);
			System.out.println("BCC：" + bccAddress);
			System.out.println("Subject：" + subject);
			System.out.println("发送时间：：" + sentDate);
			System.out.println("是新邮件？" + isnew);
			System.out.println("要求回执？" + replySign);
			// System.out.println("包含附件？" + isContainAttach());
			System.out.println("------------------------------");
	
			MailDTO item = new MailDTO();
			
			MailBean bean = new MailBean();
			bean.setMessageId(messageId);
			bean.setFrom(from);
			bean.setToAddress(toAddress);
			bean.setCcAddress(ccAddress);
			bean.setBccAddress(bccAddress);
			bean.setSubject(subject);
			bean.setReplySign(replySign);
			
			item.mailBean = bean;
			item.sendDate = sentDate;
			item.mailMessage = message;

			return item;
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	public void setLastLoadMailMillSeconds(long lastLoadMailMillSeconds) {
		this.lastLoadMailMillSeconds = lastLoadMailMillSeconds;
	}
}
