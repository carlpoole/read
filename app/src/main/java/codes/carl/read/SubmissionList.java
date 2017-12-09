package codes.carl.read;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirections;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubmissionList extends AppCompatActivity {

    @BindView(R.id.posts) ListView posts;
    @BindView(R.id.swiper) SwipeRefreshLayout swiper;
    @BindView(R.id.toolbar) Toolbar toolbar;

    RedditClient reddit;
    SubredditPaginator frontPage;
    SubmissionAdapter adapter;
    SwipeActionAdapter swipeAdapter;
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
            setupAdapter();
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
                Intent intent = new Intent(SubmissionList.this, SubmissionView.class);
                intent.putExtra("submission", sub.getDataNode().toString());
                startActivity(intent);
            }
        });

        posts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                Submission sub = ((Submission) posts.getAdapter().getItem(position));
                Intent intent = new Intent(SubmissionList.this, CommentsView.class);
                intent.putExtra("submission", sub.getDataNode().toString());
                startActivity(intent);

                return true;
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
            setupAdapter();
            swiper.setRefreshing(false);
            SubmissionList.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    private void setupAdapter(){
        swipeAdapter = new SwipeActionAdapter(adapter);
        swipeAdapter.setListView(posts);
        posts.setAdapter(swipeAdapter);

        swipeAdapter.addBackground(SwipeDirections.DIRECTION_NORMAL_LEFT, R.layout.row_bg_left_far)
                .addBackground(SwipeDirections.DIRECTION_NORMAL_RIGHT, R.layout.row_bg_right_far);

        swipeAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position) {
                // All items can be swiped
                return true;
            }

            @Override
            public boolean shouldDismiss(int position, int direction) {
                // Only dismiss an item when swiping normal left
                return false;
                //return direction == SwipeDirections.DIRECTION_NORMAL_LEFT;
            }

            @Override
            public void onSwipe(int[] positionList, int[] directionList) {
                for (int i = 0; i < positionList.length; i++) {
                    int direction = directionList[i];
                    int position = positionList[i];

                    switch (direction) {
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            Toast.makeText(SubmissionList.this, "Downvote", Toast.LENGTH_SHORT).show();
                            break;
                        case SwipeDirections.DIRECTION_FAR_RIGHT:
                        case SwipeDirections.DIRECTION_NORMAL_RIGHT:
                            Toast.makeText(SubmissionList.this, "Upvote", Toast.LENGTH_SHORT).show();
                            break;
                    }

                    //swipeAdapter.notifyDataSetChanged();
                }
            }
        });

        swipeAdapter.setDimBackgrounds(true);

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
            viewHolder.upVotes.setText("↑"+ String.valueOf(sub.getScore()));

            if(sub.isNsfw()){
                viewHolder.nsfw.setVisibility(View.VISIBLE);
                viewHolder.thumb.setImageResource(R.drawable.redditnsfw);

            }else{
                viewHolder.nsfw.setVisibility(View.GONE);
                viewHolder.thumb.setImageResource(R.drawable.defaultsub);
            }

            if (sub.getThumbnail() != null)
                Picasso.with(SubmissionList.this)
                        .load(sub.getThumbnail())

                        .into(viewHolder.thumb);

            return rowView;
        }

        class ViewHolder{
            @BindView(R.id.title) TextView title;
            @BindView(R.id.createdTime) TextView createdTime;
            @BindView(R.id.subreddit) TextView subReddit;
            @BindView(R.id.upvotes) TextView upVotes;
            @BindView(R.id.nsfw) TextView nsfw;
            @BindView(R.id.thumbnail) ImageView thumb;

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

}
