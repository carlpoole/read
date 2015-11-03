package codes.carl.read;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dean.jraw.models.Submission;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SubmissionView extends AppCompatActivity {

    @Bind(R.id.textPost) TextView textPost;
    @Bind(R.id.otherPost) WebView otherPost;

    Submission sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_view);
        ButterKnife.bind(this);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode submission = mapper.readTree(getIntent().getStringExtra("submission"));
            sub = new Submission(submission);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(sub.isSelfPost() != null && sub.isSelfPost().booleanValue()){
            // Todo: Make Text Scrollable
            textPost.setText(sub.getSelftext());
        }else {
            otherPost.setWebViewClient(new WebViewClient());
            otherPost.setVisibility(View.VISIBLE);
            otherPost.getSettings().setDomStorageEnabled(true);
            otherPost.getSettings().setJavaScriptEnabled(true);
            otherPost.getSettings().setLoadWithOverviewMode(true);
            otherPost.getSettings().setUseWideViewPort(true);
            otherPost.loadUrl(sub.getUrl());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.share) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("url", sub.getUrl());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(SubmissionView.this, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.comments) {
            // Todo: Load Comments
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
