package VolumeTile;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.QSHost;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.Dependency;
import com.android.systemui.R;

public class VolumeTile extends QSTileImpl<BooleanState>{
//	 private final LocationController mController;
//	 private final KeyguardMonitor mKeyguard;
//	 private final AnimationIcon mEnable =
//	            new AnimationIcon(com.android.internal.R.drawable.ic_audio_vol);
	public VolumeTile(QSHost host) {
		super(host);
//		mController = host.getLocationController();
//        mKeyguard = host.getKeyguardMonitor();
	}


	@Override
	public QSTile.BooleanState newTileState() {
		return new BooleanState();
	}

	@Override
	public Intent getLongClickIntent() {
		return new Intent();
	}
	

    @Override
    protected void handleSetListening(boolean listening) {

    }
    
    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_volume_label);
    }

	@Override
	protected void handleClick() {
//		getHost().startActivityDismissingKeyguard(new Intent());
		 Dependency.get(ActivityStarter.class).postStartActivityDismissingKeyguard(new Intent(), 0);
		AudioManager mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		int flags = AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND;
		 try {
			 mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_SAME,
                     AudioManager.USE_DEFAULT_STREAM_TYPE, flags);
         } catch (Exception e) {
        	 e.printStackTrace();
             Log.e(TAG, "Error dispatching volume up in dispatchTvAudioEvent.", e);
         }
		
	}

	@Override
	protected void handleUpdateState(
		   QSTile.BooleanState state, Object arg) {
		  // state.visible = true;
	       state.value = true;
	       state.icon = ResourceIcon.get(com.android.internal.R.drawable.ic_audio_vol);
	       state.label = mContext.getString(R.string.quick_settings_volume_label);
	       state.contentDescription = mContext.getString(
	                    R.string.accessibility_quick_settings_location_on);
		
	}

	@Override
	public int getMetricsCategory() {
		// TODO Auto-generated method stub
		return 888;
	}


}
