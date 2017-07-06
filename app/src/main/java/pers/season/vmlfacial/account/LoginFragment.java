package pers.season.vmlfacial.account;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pers.season.vmlfacial.LogoutActivity;
import pers.season.vmlfacial.MainActivity;
import pers.season.vmlfacial.R;
import pers.season.vmlfacial.VfUtils;

/**
 * Created by Iris on 25/05/2017.
 */

public class LoginFragment extends Fragment {

    private FragmentManager manager;
    private FragmentTransaction ft;

    private Button btn_register;
    private Button btn_login;

    private EditText txt_uname;
    private EditText txt_pwd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        manager = getFragmentManager();

    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);

        btn_register = (Button) getActivity().findViewById(R.id.register);
        btn_login = (Button) getActivity().findViewById(R.id.login);
        txt_uname = (EditText) getActivity().findViewById(R.id.uname);
        txt_pwd = (EditText) getActivity().findViewById(R.id.pwd);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Login " + txt_uname.getText().toString() + " / " + txt_pwd.getText().toString());
                String result = VfUtils.login(txt_uname.getText().toString(), txt_pwd.getText().toString());
                if (result != null)
                    Toast.makeText(LoginFragment.this.getActivity().getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                else {
                    Intent it = new Intent();
                    it.setClass(getActivity(), LogoutActivity.class);
                    startActivity(it);
                    LoginFragment.this.getActivity().finish();
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到注册页面
                RegisterFragment f_register = new RegisterFragment();
                ft = manager.beginTransaction();
                ft.replace(R.id.login_content, f_register);
                ft.commit();
            }
        });

    }
}
