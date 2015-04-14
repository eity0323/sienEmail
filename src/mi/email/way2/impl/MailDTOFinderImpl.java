package mi.email.way2.impl;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import mi.email.way2.api.IMailDTOFinder;
import mi.email.way2.control.MailManager;
import mi.email.way2.model.MailDTO;

@EBean
public class MailDTOFinderImpl implements IMailDTOFinder {
	@RootContext
	Context mcontext;
	
	@Override
	public List<MailDTO> findAll() {
		List<MailDTO> mails = MailManager.getInstance().showMailsInDB(mcontext);
		
		if(mails == null){
			mails = new ArrayList<MailDTO>();
		}
		return mails;
	}

}
