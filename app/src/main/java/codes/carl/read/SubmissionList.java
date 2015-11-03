package codes.carl.read;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.Duration;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SubmissionList extends AppCompatActivity {

    @Bind(R.id.posts) ListView posts;
    @Bind(R.id.swiper) SwipeRefreshLayout swiper;
    @Bind(R.id.toolbar) Toolbar toolbar;

    RedditClient reddit;
    SubredditPaginator frontPage;
    SubmissionAdapter adapter;
    boolean loadingFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        toolbar.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(SubmissionList.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    posts.setSelectionAfterHeaderView();
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        if(savedInstanceState != null){
            adapter = new SubmissionAdapter(this,Application.currentPage);
            posts.setAdapter(adapter);
            reddit = Application.reddit;
            frontPage = Application.subredditPaginator;
        }else{
            Application.authenticate();
            new GetSubmissions().execute();
        }

        posts.addFooterView(View.inflate(SubmissionList.this, R.layout.list_loading_footer_view, null));

        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetSubmissions().execute();
            }
        });

        posts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Submission sub = ((Submission) posts.getAdapter().getItem(position));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sub.getUrl()));
                startActivity(browserIntent);
//                Intent intent = new Intent(SubmissionList.this, SubmissionView.class);
//                intent.putExtra("submission", sub.getDataNode().toString());
//                startActivity(intent);
            }
        });

        posts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Submission sub = ((Submission) posts.getAdapter().getItem(position));
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("url", sub.getUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(SubmissionList.this, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        posts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    if (!loadingFlag) {
                        loadingFlag = true;
                        loadMoreSubmissions();
                    }
                }
            }
        });

    }

    private void loadMoreSubmissions(){
        new GetMoreSubmissions().execute();
    }

    private class GetSubmissions extends AsyncTask<String, Void, Listing<Submission>> {

        @Override
        protected Listing<Submission> doInBackground(String... params) {

            // Todo: Do this part better...
            while (!Application.reddit.isAuthenticated()) {
                Application.reddit.isAuthenticated();
            }

            reddit = Application.reddit;
            frontPage = new SubredditPaginator(reddit);
            frontPage.setLimit(50);
            frontPage.setSubreddit("ALL");
            return frontPage.next();
        }

        @Override
        protected void onPostExecute(Listing<Submission> subs) {
            adapter = new SubmissionAdapter(SubmissionList.this, subs.getChildren());
            posts.setAdapter(adapter);
            swiper.setRefreshing(false);
            SubmissionList.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    private class GetMoreSubmissions extends AsyncTask<String, Void, Listing<Submission>> {

        @Override
        protected Listing<Submission> doInBackground(String... params) {
            return frontPage.next();
        }

        @Override
        protected void onPostExecute(Listing<Submission> subs) {
            adapter.addAll(subs.getChildren());
            loadingFlag = false;
            swiper.setRefreshing(false);
        }
    }

    public class SubmissionAdapter extends ArrayAdapter<Submission> {
        private LayoutInflater inflater;
        private ArrayList<Submission> items;

        public SubmissionAdapter(Activity activity, List<Submission> submissions) {
            super(activity, R.layout.row, submissions);
            this.inflater = activity.getWindow().getLayoutInflater();
            items = new ArrayList<>();
            items.addAll(submissions);
        }

        public ArrayList<Submission> getItems(){
            return this.items;
        }

        @Override
        public void add(Submission object) {
            super.add(object);
            items.add(object);
        }

        @Override
        public void addAll(Collection<? extends Submission> collection) {
            super.addAll(collection);
            items.addAll(collection);
        }

        @Override
        public void addAll(Submission... items) {
            super.addAll(items);
            Collections.addAll(this.items, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView;
            ViewHolder viewHolder;
            Submission sub;

            if (convertView == null) {
                rowView = inflater.inflate(R.layout.row, parent, false);
                viewHolder = new ViewHolder(rowView);
                rowView.setTag(viewHolder);
            } else {
                rowView = convertView;
                viewHolder = (ViewHolder)rowView.getTag();
            }

            sub = getItem(position);

            viewHolder.title.setText(sub.getTitle());
            viewHolder.createdTime.setText((new PrettyTime()).format(sub.getCreatedUtc()));
            viewHolder.subReddit.setText(sub.getSubredditName());

            if(sub.isNsfw()){
                viewHolder.nsfw.setVisibility(View.VISIBLE);
            }else{
                viewHolder.nsfw.setVisibility(View.INVISIBLE);
            }

            if (sub.getThumbnail() != null)
                Picasso.with(SubmissionList.this).load(sub.getThumbnail()).into(viewHolder.thumb);
            else
                Picasso.with(SubmissionList.this).load(R.drawable.defaultsub).into(viewHolder.thumb);

            return rowView;
        }

        class ViewHolder{
            @Bind(R.id.title) TextView title;
            @Bind(R.id.createdTime) TextView createdTime;
            @Bind(R.id.subreddit) TextView subReddit;
            @Bind(R.id.nsfw) TextView nsfw;
            @Bind(R.id.thumbnail) ImageView thumb;

            public ViewHolder(View view){
                ButterKnife.bind(this,view);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Application.currentPage = adapter.getItems();
        Application.subredditPaginator = frontPage;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
