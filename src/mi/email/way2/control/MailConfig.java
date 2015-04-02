package mi.email.way2.control;

import java.io.File;

import android.os.Environment;

public class MailConfig {
	public static String hostServiceSmtp = "mail.suneee.com";//"smtp.suneee.com";
	public static String hostProtocolSmtp = "smtp";
	public static int hostPortSmtp = 25;
	
	public static String hostServicePop3 = "mail.suneee.com";//;//"pop3.suneee.com";
	public static String hostProtocolPop3 = "pop3";
	public static int hostPortPop3 = 110;
	
	public static String userName = "sien@suneee.com";
	public static String password = "eity0323";
	
	// 保存邮件的路径  
	public static String attachmentDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;//  
	public static String emailDir =  Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;//Environment.getExternalStorageDirectory().getAbsolutePath();  
	public static String emailFileSuffix = ".eml";  
}
