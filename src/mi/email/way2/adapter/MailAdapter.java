package mi.email.way2.adapter;

import java.util.List;

import mi.email.way2.model.MailBean;
import mi.email.way2.model.MailDTO;
import mi.learn.com.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MailAdapter extends BaseAdapter {

	List<MailDTO> datasource;
	Context mcontext;
	private LayoutInflater minflater;
	
	public MailAdapter(Context context,List<MailDTO> data){
		this.mcontext = context;
		this.datasource = data;
		this.minflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return datasource.size();
	}

	@Override
	public Object getItem(int position) {
		return datasource.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vholder = null;
		if(convertView == null){
			vholder = new ViewHolder();
			convertView = minflater.inflate(R.layout.item, null);
			
			vholder.subjectTV = (TextView) convertView.findViewById(R.id.title);
			vholder.contentTV = (TextView) convertView.findViewById(R.id.info);
			convertView.setTag(vholder);
		}else{
			vholder = (ViewHolder)convertView.getTag();
		}
		
		if(datasource != null && datasource.size() >= position){
			MailDTO item = datasource.get(position);
			MailBean bean = item.mailBean;
			vholder.subjectTV.setText(bean.getFrom());
			vholder.contentTV.setText(bean.getSubject());
		}
		return convertView;
	}
	
	public void setData(List<MailDTO> datasource){
		this.datasource = datasource;
		notifyDataSetChanged();
	}
	
	class ViewHolder{
		TextView subjectTV;
		TextView contentTV;
	}

}
