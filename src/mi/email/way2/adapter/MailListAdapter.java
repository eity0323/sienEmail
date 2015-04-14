package mi.email.way2.adapter;

import java.util.List;

import mi.email.way2.api.IMailDTOFinder;
import mi.email.way2.impl.MailDTOFinderImpl;
import mi.email.way2.model.MailDTO;
import mi.email.way2.views.itemviews.MailItemViews;
import mi.email.way2.views.itemviews.MailItemViews_;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

@EBean
public class MailListAdapter extends BaseAdapter {

	List<MailDTO> datasource;
	
	@Bean(MailDTOFinderImpl.class)
	IMailDTOFinder mailDTOFinder;
	
	@RootContext
	Context mcontext;
	
	@AfterInject
	void initAdapter(){
		datasource = mailDTOFinder.findAll();
	}
	
	@Override
	public int getCount() {
		return datasource.size();
	}

	@Override
	public MailDTO getItem(int position) {
		return datasource.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MailItemViews mailItemViews;
		if(convertView == null){
			mailItemViews = MailItemViews_.build(mcontext);
		}else{
			mailItemViews = (MailItemViews) convertView;
		}
		
		mailItemViews.bind(getItem(position));
		
		return mailItemViews;
	}
	
	public void setData(List<MailDTO> datasource){
		this.datasource = datasource;
		notifyDataSetChanged();
	}

}
