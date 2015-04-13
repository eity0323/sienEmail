package mi.email.way2;

import mi.learn.com.R;

import com.googlecode.androidannotations.annotations.EActivity;

import android.app.Activity;
import android.widget.Toast;

@EActivity(R.layout.base_activity)
public class BaseActivity extends Activity{

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
}
