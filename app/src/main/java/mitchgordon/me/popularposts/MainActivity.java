package mitchgordon.me.popularposts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.schedulers.Schedulers;


public class MainActivity extends Activity {

    UiLifecycleHelper uiHelper;

    @InjectView(R.id.titleTV)
    TextView titleTV;

    @InjectView(R.id.loginButton)
    LoginButton authButton;

    @InjectView(R.id.postsLV)
    ListView postsLV;

    public class Post implements Comparable<Post> {
        int likes;
        String message;

        public Post(int likes, String message) {
            this.likes = likes;
            this.message = message;
        }

        public int compareTo(Post other) {
            return other.likes - this.likes;
        }

        public String toString() {
            return message + "\n\n" + likes;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        authButton.setReadPermissions("read_stream");

        final AlertDialog progressDialog =
                new AlertDialog.Builder(this)
                .setTitle("Please Wait")
                .setMessage("Retrieving posts...")
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create();

        uiHelper = new UiLifecycleHelper(this, (session, state, exception) -> {
            if(session.isOpened()) {
                // Grab the posts from the server and put them in a stream
                progressDialog.show();

                Observable<Post> postsStream = Observable.create( subscriber -> {
                    Request request = Request.newGraphPathRequest(session, "/me/feed", response -> {});
                    Bundle params = new Bundle();
                    params.putString("fields", "likes.limit(1).summary(true),message");
                    request.setParameters(params);
                    while(request != null) {
                        Response resp = Request.executeBatchAndWait(request).get(0);
                        if(resp.getError() != null)
                            subscriber.onError((Throwable)resp.getError().getException());

                        GraphObjectList<GraphObject> posts =
                                resp.getGraphObject().getPropertyAsList("data", GraphObject.class);

                        for (GraphObject post : posts ) {
                            JSONObject postJSON = post.getInnerJSONObject();

                            int likes;
                            try {
                                likes = postJSON.getJSONObject("likes").getJSONObject("summary")
                                        .getInt("total_count");
                            }
                            catch(Exception e) { likes = 0;};
                            String msg;
                            try {
                                msg = postJSON.getString("message");
                            }
                            catch(Exception e) { msg = ""; }
                            subscriber.onNext(new Post(likes, msg));
                        }

                        request = resp.getRequestForPagedResults(Response.PagingDirection.NEXT);
                    }

                    Log.d("PopularPosts", "Finished");
                    subscriber.onCompleted();
                });

                // Accumulate posts, sort and display
                postsStream
                .toSortedList()
                .subscribeOn(Schedulers.newThread())
                .subscribe(sortedPosts -> {
                    runOnUiThread( () -> {
                                titleTV.setVisibility(View.GONE);
                                postsLV.setVisibility(View.VISIBLE);
                                postsLV.setAdapter(new ArrayAdapter<Post>(getApplicationContext(),
                                        R.layout.simple_list_item,
                                        sortedPosts.toArray(new Post[sortedPosts.size()])));
                                progressDialog.dismiss();
                            } );
                },
                e -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_LONG)));
            }
            else {
                // Show the title and hide the posts
                titleTV.setVisibility(View.VISIBLE);
                postsLV.setVisibility(View.INVISIBLE);
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
