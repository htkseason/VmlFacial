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
 * Created by Iris on 26/05/2017.
 */

public class RegisterFragment extends Fragment {

    private FragmentManager manager;
    private FragmentTransaction ft;

    private Button btn_submit;
    private Button btn_return_login;

    private EditText txt_uname;
    private EditText txt_pwd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register, container, false);

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

        btn_submit = (Button) getActivity().findViewById(R.id.submit);
        btn_return_login = (Button) getActivity().findViewById(R.id.return_login);
        txt_uname = (EditText) getActivity().findViewById(R.id.runame);
        txt_pwd = (EditText) getActivity().findViewById(R.id.rpsw);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Register " +txt_uname.getText().toString() +" / "+ txt_pwd.getText().toString());
                String result = VfUtils.regsiter(txt_uname.getText().toString(), txt_pwd.getText().toString());
                if (result != null)
                    Toast.makeText(RegisterFragment.this.getActivity().getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                else {
                    Intent it = new Intent();
                    it.setClass(getActivity(), LogoutActivity.class);
                    startActivity(it);
                    RegisterFragment.this.getActivity().finish();
                }
            }
        });
        btn_return_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginFragment f_login = new LoginFragment();
                ft = manager.beginTransaction();
                ft.replace(R.id.login_content, f_login);
                ft.commit();
            }
        });
    }

}
