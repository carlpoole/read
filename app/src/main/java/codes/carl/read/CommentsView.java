package codes.carl.read;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentsView extends AppCompatActivity {

    @BindView(R.id.comments) ListView comments;
    Submission sub;
    CommentNode commentNode;
    CommentsAdapter commentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_view);
        ButterKnife.bind(this);

        setTitle("Comments");

        commentsAdapter = new CommentsAdapter(this,new ArrayList<CommentNode>());
        comments.setAdapter(commentsAdapter);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode submission = mapper.readTree(getIntent().getStringExtra("submission"));
            sub = new Submission(submission);
            new GetComments().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class GetComments extends AsyncTask<String, Void, CommentNode> {

        @Override
        protected CommentNode doInBackground(String... params) {
            Submission full = Application.reddit.getSubmission(sub.getId());
            return full.getComments();
        }

        @Override
        protected void onPostExecute(CommentNode commentNode) {
            CommentsView.this.commentNode = commentNode;

            Iterable<CommentNode> iterable = commentNode.walkTree();
            for (CommentNode node : iterable) {
                commentsAdapter.add(node);
            }
        }
    }

    public class CommentsAdapter extends ArrayAdapter<CommentNode> {
        private LayoutInflater inflater;
        private ArrayList<CommentNode> items;

        public CommentsAdapter(Activity activity, List<CommentNode> comments) {
            super(activity, R.layout.row_comment, comments);
            this.inflater = activity.getWindow().getLayoutInflater();
            items = new ArrayList<>();
            items.addAll(comments);
        }

        public ArrayList<CommentNode> comments(){
            return this.items;
        }

        @Override
        public void add(CommentNode object) {
            super.add(object);
            items.add(object);
        }

        @Override
        public void addAll(Collection<? extends CommentNode> collection) {
            super.addAll(collection);
            items.addAll(collection);
        }

        @Override
        public void addAll(CommentNode... items) {
            super.addAll(items);
            Collections.addAll(this.items, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView;
            ViewHolder viewHolder;
            CommentNode commentNode;

            if (convertView == null) {
                rowView = inflater.inflate(R.layout.row_comment, parent, false);
                viewHolder = new ViewHolder(rowView);
                rowView.setTag(viewHolder);
            } else {
                rowView = convertView;
                viewHolder = (ViewHolder)rowView.getTag();
            }

            commentNode = getItem(position);
            Comment comment = commentNode.getComment();

            int dp5 = (int)TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    5.0f,
                    getResources().getDisplayMetrics());

            int dp10 = (int)TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    5.0f,
                    getResources().getDisplayMetrics());

            // Todo: Make comment tree/child handling better - just indents right now
            viewHolder.author.setPadding(dp10 + (commentNode.getDepth() * dp5), 0, 0, 0);
            viewHolder.body.setPadding(dp10 + (commentNode.getDepth() * dp5), 0, 0, 0);

            viewHolder.author.setText(comment.getAuthor());
            viewHolder.body.setText(comment.getBody());

            return rowView;
        }

        class ViewHolder{
            @BindView(R.id.comment_author) TextView author;
            @BindView(R.id.comment_body) TextView body;

            public ViewHolder(View view){
                ButterKnife.bind(this,view);
            }
        }
    }
}
