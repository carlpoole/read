package codes.carl.read;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
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

public class List extends AppCompatActivity {

    RedditClient reddit;
    SubredditPaginator frontPage;
    ListView posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Todo: Use Butterknife for view pairing
        posts = (ListView) findViewById(R.id.posts);
        posts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(List.this, SubmissionView.class);
                intent.putExtra(
                        "submission",
                        ((Submission) posts.getAdapter().getItem(position)).getDataNode().toString());
                startActivity(intent);
            }
        });

        Application.authenticate();

        new GetSubmissions().execute();

        // Todo: pull down to refresh

        // Todo: pull up to load more

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
            return frontPage.next();
        }

        @Override
        protected void onPostExecute(Listing<Submission> subs) {
            posts.setAdapter(new SubmissionAdapter(List.this, subs));
        }
    }

    private class SubmissionAdapter extends ArrayAdapter<Submission> {
        private LayoutInflater inflater;

        public SubmissionAdapter(Activity activity, Listing<Submission> submissions) {
            super(activity, R.layout.row, submissions);
            inflater = activity.getWindow().getLayoutInflater();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView;
            Submission sub;

            if (convertView == null) {
                rowView = inflater.inflate(R.layout.row, parent, false);
            } else {
                rowView = convertView;
            }

            sub = getItem(position);

            TextView title = (TextView) rowView.findViewById(R.id.title);
            TextView upvotes = (TextView) rowView.findViewById(R.id.upvotes);
            TextView user = (TextView) rowView.findViewById(R.id.user);
            ImageView thumb = (ImageView) rowView.findViewById(R.id.thumbnail);

            title.setText(sub.getTitle());
            upvotes.setText(String.valueOf(sub.getScore()));
            user.setText(sub.getAuthor());

            if (sub.getThumbnail() != null)
                Picasso.with(List.this).load(sub.getThumbnail()).into(thumb);
            else
                Picasso.with(List.this).load(R.drawable.defaultsub).into(thumb);

            return rowView;

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
