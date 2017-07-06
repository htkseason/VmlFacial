package pers.season.vmlfacial;

import android.app.Activity;
import android.os.Bundle;

import pers.season.vmlfacial.R;
import pers.season.vmlfacial.account.LoginFragment;

/**
 * Created by Iris on 24/05/2017.
 */

public class LoginActivity extends Activity {

    private LoginFragment f_login;
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setContentView(R.layout.activity_login);

        f_login = new LoginFragment();

        getFragmentManager().beginTransaction().replace(R.id.login_content,f_login).commit();


    }
}
