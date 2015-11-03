package codes.carl.read;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import java.io.IOException;

public class SubmissionView extends AppCompatActivity {

    Picasso picasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_view);

        Submission sub = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode submission = mapper.readTree(getIntent().getStringExtra("submission"));
            final Submission sub1 = new Submission(submission);
            sub = sub1;
            picasso = new Picasso.Builder(this).listener(new Picasso.Listener() {
                @Override public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    exception.printStackTrace();
                    System.out.println(sub1.getUrl());
                }
            }).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Todo: rough test right now. Make more robust, handle more post types
        if(sub.isSelfPost() != null && sub.isSelfPost().booleanValue()){
            TextView textView = (TextView) findViewById(R.id.textPost);
            textView.setText(sub.getSelftext());
        }else {
            String data = sub.getDataNode().get("preview").get("images").elements().next().get("source").get("url").toString();
            data = data.substring(0,data.indexOf("?"));
            ImageView imagePost = (ImageView) findViewById(R.id.imagePost);
            picasso.load(data).into(imagePost);
        }

    }
}
