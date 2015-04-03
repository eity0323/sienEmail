package mi.email.way2.tools;

import java.util.List;
import mi.email.way2.model.MailDTO;

public class MailEvent {
	/*加载邮件*/
	public static class loadMailsEvent{
		List<MailDTO> items;
    	int status;
    	public static int STATUS_SUCCESS = 0;
    	public static int STATUS_FAILED = 1;
  
        public loadMailsEvent(int status,List<MailDTO> items)  
        {  
            this.items = items;  
            this.status = status;
        }  
        
        public List<MailDTO> getDatas()  
        {  
            return items;  
        }  
        
        public int getStatus()  
        {  
            return status;  
        } 
	}
	
	/*搜索邮件*/
	public static class searchMailsEvent{
		List<MailDTO> items;
    	int status;
    	public static int STATUS_SUCCESS = 0;
    	public static int STATUS_FAILED = 1;
  
        public searchMailsEvent(int status,List<MailDTO> items)  
        {  
            this.items = items;  
            this.status = status;
        }  
        
        public List<MailDTO> getDatas()  
        {  
            return items;  
        }  
        
        public int getStatus()  
        {  
            return status;  
        } 
	}
	
	/*连接邮件服务器*/
	public static class connectMailServiceEvent{
		int status;
		String items;
		public static int STATUS_SUCCESS = 0;
    	public static int STATUS_FAILED = 1;
  
    	public connectMailServiceEvent(int status,String items)
    	{
    		this.items = items;
    		this.status = status;
    	}
    	
    	public String getDatas(){
    		return items;
    	}
    	
    	public int getStatus()
    	{
    		return status;
    	}
	}
	
	/*删除邮件*/
	public static class deleteMailEvent{
		int status;
		String items;
		public static int STATUS_SUCCESS = 0;
    	public static int STATUS_FAILED = 1;
  
    	public deleteMailEvent(int status,String items)
    	{
    		this.items = items;
    		this.status = status;
    	}
    	
    	public String getDatas(){
    		return items;
    	}
    	
    	public int getStatus()
    	{
    		return status;
    	}
	}
	
	/*回复邮件*/
	public static class replyMailEvent{
		int status;
		String items;
		public static int STATUS_SUCCESS = 0;
    	public static int STATUS_FAILED = 1;
  
    	public replyMailEvent(int status,String items)
    	{
    		this.items = items;
    		this.status = status;
    	}
    	
    	public String getDatas(){
    		return items;
    	}
    	
    	public int getStatus()
    	{
    		return status;
    	}
	}
}
