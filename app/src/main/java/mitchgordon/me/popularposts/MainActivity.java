package mitchgordon.me.popularposts;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.UiLifecycleHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity {

    UiLifecycleHelper uiHelper;

    @InjectView(R.id.titleTV)
    TextView titleTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        uiHelper = new UiLifecycleHelper(this, (session, state, exception) -> {
            if(session.isOpened()) {
                // Grab the posts and display them.
            }
            else {
                // Show the title and hide the posts

            }
        });

        if (savedInstanceState != null)
            uiHelper.onCreate(savedInstanceState);

    }


    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}
