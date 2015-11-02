package codes.carl.read;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.List;

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

        posts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount != 0){
                    if(!loadingFlag){
                        loadingFlag = true;
                        loadMoreSubmissions();
                    }
                }
            }
        });

        Application.authenticate();

        new GetSubmissions().execute();

        // Todo: pull down to refresh

        // Todo: pull up to load more

    }

    private void loadMoreSubmissions(){
        new GetMoreSubmissions().execute();
    }

    private class GetSubmissions extends AsyncTask<String, Void, Listing<Submission>> {

        @Override
        protected Listing<Submission> doInBackground(String... params) {

            // Todo: add a loading indicator

            // Todo: Do this part better...
            while (!Application.reddit.isAuthenticated()) {
                Application.reddit.isAuthenticated();
            }

            reddit = Application.reddit;
            frontPage = new SubredditPaginator(reddit);
            frontPage.setLimit(50);
            return frontPage.next();
        }

        @Override
        protected void onPostExecute(Listing<Submission> subs) {
            adapter = new SubmissionAdapter(SubmissionList.this, subs.getChildren());
            posts.setAdapter(adapter);
            swiper.setRefreshing(false);
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

        public SubmissionAdapter(Activity activity, List<Submission> submissions) {
            super(activity, R.layout.row, submissions);
            this.inflater = activity.getWindow().getLayoutInflater();
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
            viewHolder.upvotes.setText(String.valueOf(sub.getScore()));
            viewHolder.subReddit.setText(sub.getSubredditName());

            if (sub.getThumbnail() != null)
                Picasso.with(SubmissionList.this).load(sub.getThumbnail()).into(viewHolder.thumb);
            else
                Picasso.with(SubmissionList.this).load(R.drawable.defaultsub).into(viewHolder.thumb);

            return rowView;

        }

        class ViewHolder{
            @Bind(R.id.title) TextView title;
            @Bind(R.id.upvotes) TextView upvotes;
            @Bind(R.id.subreddit) TextView subReddit;
            @Bind(R.id.thumbnail) ImageView thumb;

            public ViewHolder(View view){
                ButterKnife.bind(this,view);
            }

        }
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
