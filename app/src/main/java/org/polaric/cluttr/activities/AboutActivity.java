package org.polaric.cluttr.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import org.polaric.cluttr.BuildConfig;
import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;
import org.polaric.colorful.Colorful;
import org.polaric.colorful.ColorfulActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.BSD3ClauseLicense;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;


public class AboutActivity extends ColorfulActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.about_toolbar) protected Toolbar toolbar;
    @BindView(R.id.appnav) protected NavigationView appNav;
    @BindView(R.id.authornav) protected NavigationView authorNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.about);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        toolbar.setNavigationOnClickListener(this);

        appNav.getMenu().findItem(0).setTitle(getResources().getString(R.string.version) + " " + BuildConfig.VERSION_NAME);
        appNav.setNavigationItemSelectedListener(this);

        authorNav.setNavigationItemSelectedListener(this);

        if (appNav != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            appNav.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    return insets;
                }
            });
        }

        if (authorNav != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            authorNav.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    return insets;
                }
            });
        }

        /*TODO
        if (!Util.isGooglePlayServicesAvailable(this)) {
            ((NavigationView) findViewById(R.id.authornav)).getMenu().getItem(1).setVisible(false);
        }*/

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.github:
                openInChrome("https://github.com/garretyoder");
                break;
            case R.id.website:
                openInChrome("http://www.polaric.org");
                break;
            case R.id.rate:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (android.content.ActivityNotFoundException anfe) {
                    openInChrome("https://play.google.com/store/apps/details?id=" + getPackageName());
                }
                break;
            case R.id.garret:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=6014078401699539823")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    openInChrome("https://play.google.com/store/apps/dev?id=6014078401699539823");
                }
                break;
            case R.id.donate:
                startActivity(new Intent(this,DonateActivity.class));
                break;
            case R.id.reportbug:
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "garret@polaric.org", null)));
                break;
            case R.id.changelog:
                Util.ViewUtils.showChangelogDialog(this);
                break;
            case R.id.licenses:
                Notices notices = new Notices();
                notices.addNotice(new Notice("Colorful", "https://github.com/garretyoder/Colorful", "Copyright 2016 Garret Yoder", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Material Dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Michael Follestad", new MITLicense()));
                notices.addNotice(new Notice("Glide", "https://github.com/bumptech/glide", "Copyright 2014 Google, Inc. All rights reserved.", new BSD3ClauseLicense()));
                notices.addNotice(new Notice("Recycler Fast Scroll", "https://github.com/plusCubed/recycler-fast-scroll", null, new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Licenses Dialog", "https://github.com/PSDev/LicensesDialog", null, new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("RxJava","https://github.com/ReactiveX/RxJava","Copyright 2013 Netflix, Inc.", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Butterknife","https://github.com/JakeWharton/butterknife","Copyright 2013 Jake Wharton", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Fastscroller","https://github.com/plusCubed/recycler-fast-scroll", "Copyright 2016 Daniel Ciao",new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Video Player", "https://github.com/afollestad/easy-video-player", null, new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Subsampling ImageView", "https://github.com/davemorrissey/subsampling-scale-image-view", "Copyright 2015 David Morrissey", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Metadata Extractor", "https://github.com/drewnoakes/metadata-extractor", "Copyright 2002-2016 Drew Noakes", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Android In-App Billing v3 Library", "https://github.com/anjlab/android-inapp-billing-v3", "Copyright 2014 AnjLab", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Android Support Libraries", "https://github.com/android/platform_frameworks_support", null, new ApacheSoftwareLicense20()));
                new LicensesDialog.Builder(this)
                        .setNotices(notices)
                        .setTitle(R.string.licenses)
                        .build()
                        .show();
                break;
            case R.id.gpl:
                openInChrome("https://git.polaric.org/polaric/Cluttr");
                break;
        }
        return false;
    }
    private void openInChrome(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setSecondaryToolbarColor(getResources().getColor(Colorful.getThemeDelegate().getPrimaryColor().getColorRes()));
        builder.setToolbarColor(getResources().getColor(Colorful.getThemeDelegate().getPrimaryColor().getColorRes()));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}
