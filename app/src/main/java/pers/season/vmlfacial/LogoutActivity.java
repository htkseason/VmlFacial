package pers.season.vmlfacial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

/**
 * Created by Iris on 28/05/2017.
 */

public class LogoutActivity extends Activity {

    private String username = null;
    private TextView t1;
    private TextView t2;

    private Button btn_logout;

    public LogoutActivity() {

    }


    @Override
    protected void onStart() {
        super.onStart();
        t1.setText("Welcome, " + WorldVar.userName);
        int photoCount = 0;
        if (WorldVar.userName != null) {
            File cloudPicFolder = new File(VfUtils.PicDataPath + "/" + WorldVar.userName + "/");
            if (cloudPicFolder.exists()) {
                File[] cloudPicFiles = cloudPicFolder.listFiles();
                photoCount = cloudPicFiles.length;
            }
        }
        t2.setText(photoCount +" photo(s) synchronized!");
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setContentView(R.layout.activity_logout);

        t1 = (TextView) findViewById(R.id.logout_title);
        t2 = (TextView) findViewById(R.id.logout_msg);
        btn_logout = (Button) findViewById(R.id.logout);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorldVar.userName = null;
                Intent it = new Intent();
                it.setClass(LogoutActivity.this, LoginActivity.class);
                startActivity(it);
                LogoutActivity.this.finish();
            }
        });

    }
}
