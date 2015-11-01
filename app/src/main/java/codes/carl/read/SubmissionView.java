package codes.carl.read;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_view);

        Submission sub = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode submission = mapper.readTree(getIntent().getStringExtra("submission"));
            sub = new Submission(submission);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Todo: rough test right now. Make more robust, handle more post types
        if(sub.isSelfPost() != null && sub.isSelfPost().booleanValue()){
            TextView textView = (TextView) findViewById(R.id.textPost);
            textView.setText(sub.getSelftext());
        }else {
            ImageView imagePost = (ImageView) findViewById(R.id.imagePost);
            Picasso.with(this).load(sub.getUrl()).into(imagePost);
        }

    }
}
