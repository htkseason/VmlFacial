package pers.season.vmlfacial;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import pers.season.vmlfacial.account.AccountWareHouse;

/**
 * Created by 10748 on 2017/5/24.
 */

public class VfUtils {

    public final static String PicDataPath = Environment.getExternalStorageDirectory().toString() + "/vmlfacial/album/";
    public final static String AccountDataPath = Environment.getExternalStorageDirectory().toString() + "/vmlfacial/account.data";

    public static void saveBitmap(Bitmap mBitmap, long fileName) {
        String mPicDataPath = PicDataPath;
        if (WorldVar.userName != null)
            mPicDataPath += "/" + WorldVar.userName;
        File folder = new File(mPicDataPath);

        if (!folder.exists())
            folder.mkdirs();
        try {
            File f = new File(folder.getCanonicalPath() + "/" + fileName);
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String login(String name, String pwd) {
        if (WorldVar.accountWareHouse == null)
            VfUtils.loadAccountData();
        if (name == null || pwd == null || name.length() == 0 || pwd.length() == 0) {
            return "Invalid name or password";
        } else if (!WorldVar.accountWareHouse.accounts.containsKey(name)) {
            return "Account name not exist";
        } else if (!WorldVar.accountWareHouse.accounts.get(name).contentEquals(pwd)) {
            return "Wrong password";
        } else {
            WorldVar.userName = name;
            return null;
        }
    }

    public static String regsiter(String name, String pwd) {
        if (WorldVar.accountWareHouse == null)
            VfUtils.loadAccountData();
        if (name == null || pwd == null || name.length() == 0 || pwd.length() == 0) {
            return "Invalid name or password";
        } else if (WorldVar.accountWareHouse.accounts.containsKey(name)) {
            return "Existed account name";
        } else {
            WorldVar.accountWareHouse.accounts.put(name, pwd);
            VfUtils.saveAccountData(WorldVar.accountWareHouse);
            WorldVar.userName = name;
            return null;
        }

    }

    public static void saveAccountData(AccountWareHouse data) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(AccountDataPath));
            oos.writeObject(data);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void loadAccountData() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(AccountDataPath));
            WorldVar.accountWareHouse = (AccountWareHouse) ois.readObject();
            ois.close();
        } catch (IOException e) {
            AccountWareHouse newAccountWareHouse = new AccountWareHouse();
            saveAccountData(newAccountWareHouse);
            WorldVar.accountWareHouse = newAccountWareHouse;
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


}
