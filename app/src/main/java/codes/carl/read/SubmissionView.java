package codes.carl.read;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.meta.SubmissionSerializer;

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

        if(sub.isSelfPost() != null && sub.isSelfPost().booleanValue()){
            TextView textView = (TextView) findViewById(R.id.textPost);
            textView.setText(sub.getSelftext());
        }else {
            ImageView imagePost = (ImageView) findViewById(R.id.imagePost);
            Picasso.with(this).load(sub.getUrl()).into(imagePost);
        }

    }
}
