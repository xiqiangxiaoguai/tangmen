package com.umeng.example.analytics;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.umeng.analytics.MobclickAgent;
import com.umeng.example.R;
import com.umeng.ui.BaseSinglePaneActivity;

public class AnalyticsHome extends BaseSinglePaneActivity {	
	private Context mContext;
	private final  String mPageName = "AnalyticsHome";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;	
		MobclickAgent.setDebugMode(true);
//      SDK在统计Fragment时，需要关闭Activity自带的页面统计，
//		然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
		MobclickAgent.openActivityDurationTrack(false);
//		MobclickAgent.setAutoLocation(true);
//		MobclickAgent.setSessionContinueMillis(1000);
		
		MobclickAgent.updateOnlineConfig(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart( mPageName );
		MobclickAgent.onResume(mContext);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd( mPageName );
		MobclickAgent.onPause(mContext);
	}
	
	/**
	 * android:onClick="onButtonClick"
	 * @param view
	 */
	public void onButtonClick(View view){
		int id = view.getId();
		switch( id){
		case R.id.umeng_example_analytics_online_config:
			
			String onlineParams= MobclickAgent.getConfigParams(mContext, "abc");//the demo param's key is 'abc'
			if(onlineParams.equals("")){
				Toast.makeText(mContext, "Get No Online Params", Toast.LENGTH_SHORT).show();
			}else
				Toast.makeText(mContext, "Online Params:"+ onlineParams, Toast.LENGTH_SHORT).show();
			break;
		case R.id.umeng_example_analytics_event:
			
			MobclickAgent.onEvent(mContext, "click");
			MobclickAgent.onEvent(mContext, "click", "button");
			break;
		case R.id.umeng_example_analytics_ekv:
			
			HashMap<String, String> map_ekv = new HashMap<String, String>();
			map_ekv.put("type", "popular");
			map_ekv.put("artist", "JJLin");
			
			MobclickAgent.onEvent(mContext, "music", map_ekv);
			break;
		case R.id.umeng_example_analytics_duration:
			
			// We need manual to compute the Events duration 
			MobclickAgent.onEventDuration(mContext, "book", 12000);
			MobclickAgent.onEventDuration(mContext, "book", "chapter1", 23000);
			
			HashMap<String, String> map_du = new HashMap<String, String>();
			map_du.put("type", "popular");
			map_du.put("artist", "JJLin");
			
			MobclickAgent.onEventDuration(mContext, "music", map_du, 2330000);
			
			break;
		case R.id.umeng_example_analytics_event_begin:
			//Log.i("duration", "start");
			//when the events start
			MobclickAgent.onEventBegin(mContext, "music");
			
			MobclickAgent.onEventBegin(mContext, "music", "one");
			
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("type", "popular");
			map.put("artist", "JJLin");
			
			MobclickAgent.onKVEventBegin(mContext, "music", map, "flag0");
			break;
		case R.id.umeng_example_analytics_event_end:
			
			MobclickAgent.onEventEnd(mContext, "music");
			MobclickAgent.onEventEnd(mContext, "music", "one");	
			
			MobclickAgent.onKVEventEnd(mContext, "music", "flag0");
			break;
		case R.id.umeng_example_analytics_make_crash:
			"123".substring(10);
			break;
		case R.id.umeng_example_analytics_js_analytic:
			startActivity( new Intent( this, WebviewAnalytic.class) );
			break;
		case R.id.umeng_example_analytics_flush:
			MobclickAgent.flush(this);
			break;
		case R.id.umeng_example_analytics_fragment_stack:
			startActivity( new Intent(this, FragmentStack.class));
			break;
		case R.id.umeng_example_analytics_fragment_tabs:
			startActivity( new Intent(this, FragmentTabs.class));
			break;
		}
	}

	@Override
	protected Fragment onCreatePane() {
		return new AnalyticsHomeDashboardFragment();	
	}
	
	public static class AnalyticsHomeDashboardFragment extends Fragment {
		
		@Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	    }
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.umeng_example_analytics, container,false);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			Hook();
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	///对于好多应用，会在程序中杀死 进程，这样会导致我们统计不到此时Activity结束的信息，
	///对于这种情况需要调用 'MobclickAgent.onKillProcess( Context )'
	///方法，保存一些页面调用的数据。正常的应用是不需要调用此方法的。
	private void Hook(){
		 AlertDialog.Builder builder = new AlertDialog.Builder( mContext );    
	        builder.setPositiveButton("退出应用",  
	                new DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int whichButton) {  
	                    	MobclickAgent.onKillProcess( mContext );
	                    	
	                    	int pid = android.os.Process.myPid();
	            			android.os.Process.killProcess(pid);
	                    }  
	                });  
	        builder.setNeutralButton("后退一下",  
	                new DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int whichButton) {  
	                        finish();
	                    }  
	                });  
	        builder.setNegativeButton("点错了",  
	                new DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int whichButton) {  
	                    }  
	                });  
	        builder.show();  
	}
}