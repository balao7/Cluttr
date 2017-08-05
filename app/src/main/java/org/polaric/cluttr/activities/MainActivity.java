package org.polaric.cluttr.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.mock.MockApplication;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.digitus.Digitus;
import com.afollestad.digitus.DigitusCallback;
import com.afollestad.digitus.DigitusErrorType;
import com.afollestad.digitus.FingerprintDialog;

import org.polaric.cluttr.Cluttr;
import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;
import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.Media;
import org.polaric.cluttr.fragments.AlbumFragment;
import org.polaric.cluttr.fragments.BaseFragment;
import org.polaric.cluttr.fragments.GrantFragment;
import org.polaric.cluttr.io.MediaLoader;
import org.polaric.colorful.ColorfulActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class MainActivity extends ColorfulActivity implements NavigationView.OnNavigationItemSelectedListener, FingerprintDialog.Callback {

    @BindView(R.id.nav_main) protected NavigationView navMenu;
    @BindView(R.id.nav_footer) protected NavigationView navFooter;
    @BindView(R.id.drawer_layout) protected DrawerLayout mDrawer;

    private boolean PICK_INTENT=false;
    private AuthenticationPassedCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        navFooter.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            Media.init(this, false);
            lockDrawer(true);

            if (requestPerms()) {
                if (getIntent().getAction().equals(Intent.ACTION_GET_CONTENT) || getIntent().getAction().equals(Intent.ACTION_PICK)) {
                    PICK_INTENT = true;
                    clearAndLoadFragments(new AlbumFragment());
                } else {
                    Log.d(Util.LOG_TAG, "Starting on intent " + getIntent().getAction());
                    clearAndLoadFragments(new AlbumFragment());
                }
                if (Cluttr.getApplication().isNewRelease()) {
                    Util.ViewUtils.showChangelogDialog(this);
                }

                Log.d(Util.LOG_TAG, "First start, indexing media");
                MediaLoader.init()
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                //Stub
                            }
                        });
            }
        }


    }

    public void toggleTransparentNav(boolean transparent) {
        if (transparent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent sIntent = new Intent(this,SettingsActivity.class);
                startActivity(sIntent);
                return false;
            case R.id.about:
                Intent aIntent = new Intent(this,AboutActivity.class);
                startActivity(aIntent);
                return false;
            case R.id.donate_footer:
                startActivity(new Intent(this,DonateActivity.class));
                return false;
        }
        return false;
    }

    public void openDrawer() {
        mDrawer.openDrawer(Gravity.LEFT);
    }

    public boolean getPickIntent() {
        return PICK_INTENT;
    }

    public void lockDrawer(boolean lock) {
        if (lock) {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    private void clearAndLoadFragments(BaseFragment frag) {
        //TODO: POP ENTIRE BACKSTACK AND CLEAR ALL FRAGMENTS
        lockDrawer(false);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(Util.Fragment.FRAGMENT_PERM_REQUEST_TAG);
        if(fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        fragment = getSupportFragmentManager().findFragmentByTag(Util.Fragment.FRAGMENT_ALBUM_TAG);
        if(fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, frag, Util.Fragment.FRAGMENT_ALBUM_TAG);
        transaction.commit();

    }

    public boolean requestPerms() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Util.LOG_TAG, "Requesting I/O permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.PERMISSION_REQUEST_STORAGE);
            return false;
        } else {
            Log.d(Util.LOG_TAG, "Permissions already granted, loading media");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==Util.PERMISSION_REQUEST_STORAGE
                && grantResults.length>1
                && grantResults[0]==PackageManager.PERMISSION_GRANTED
                && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
            Log.d(Util.LOG_TAG, "Permissions granted, recreating activity");
            Cluttr.getApplication().restart();
            finish();
        } else if (requestCode==Util.PERMISSION_REQUEST_FINGERPRINT) {
            Digitus.get().handleResult(requestCode, permissions, grantResults);
        } else {
            Log.d(Util.LOG_TAG, "Permissions denied, showing grant fragment");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content,new GrantFragment(),Util.Fragment.FRAGMENT_ALBUM_TAG);
            transaction.commit();
        }
    }

    public void openFingerprintDialog(AuthenticationPassedCallback callback) {
        this.callback=callback;
        FingerprintDialog.show(this, Util.FINGERPRINT_KEY, Util.PERMISSION_REQUEST_FINGERPRINT);
    }

    @Override
    public void onFingerprintDialogAuthenticated() {
        if (callback!=null) {
            callback.onAuthenticationPassed();
        }
    }

    @Override
    public void onFingerprintDialogVerifyPassword(FingerprintDialog dialog, final String password) {
        dialog.notifyPasswordValidation(password.equals(PreferenceManager.getDefaultSharedPreferences(this).getString(Util.PASSWORD_KEY,null)));
    }

    @Override
    public void onFingerprintDialogStageUpdated(FingerprintDialog dialog, FingerprintDialog.Stage stage) {
        Log.d(Util.LOG_TAG, "Fingerprint stage: " + stage.name());
    }

    @Override
    public void onFingerprintDialogCancelled() {
        Toast.makeText(this, "Authentication cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Util.VIDEO_ACTIVITY_VIEW) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public interface AuthenticationPassedCallback {
        void onAuthenticationPassed();
    }
}
