package mi.email.way2.views.itemviews;

import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import mi.learn.com.R;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.list_item)
public class MailItemViews extends LinearLayout {
	@ViewById(R.id.title)
	TextView subjectTV;
	
	@ViewById(R.id.info)
	TextView contentTV;
	
	public MailItemViews(Context context) {
		super(context);
	}
	
	public void bind(MailDTO itemData){
		MailBean bean = itemData.mailBean;
		subjectTV.setText(bean.getFrom());
		contentTV.setText(bean.getSubject());
	}
}
