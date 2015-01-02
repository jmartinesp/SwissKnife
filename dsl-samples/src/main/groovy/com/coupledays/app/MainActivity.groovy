package com.coupledays.app

import android.content.res.Configuration
import android.os.Bundle
import android.os.StrictMode
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarActivity
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.android.ast.InjectViews
import com.coupledays.app.fragments.ByRoomFragment
import com.coupledays.app.fragments.MainFragment
import com.coupledays.app.fragments.ScreenPagerFragment
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import groovy.transform.CompileStatic

@CompileStatic
@InjectViews(R.layout.activity_main_new)
class MainActivity extends ActionBarActivity {

    String[] screenTitles
    DrawerLayout drawerLayout
    ListView drawerList
    ActionBarDrawerToggle drawerToggle
    CharSequence drawerTitle
    CharSequence title

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build()
        ImageLoader.getInstance().init(config);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build());
        title = drawerTitle = getTitle()
        screenTitles = getResources().getStringArray(R.array.screen_array);
        drawerLayout = (DrawerLayout) view(R.id.drawer_layout)
        drawerList = (ListView) view(R.id.left_drawer)
        drawerList.adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, screenTitles)
        drawerList.onItemClickListener = { AdapterView parent, View view, int position, long id ->
            this.selectItem(position)
        }
        drawerToggle = new ActionBarDrawerToggle(this, this.drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.setDrawerListener(drawerToggle)
        if (savedInstanceState == null) {
            selectItem(0)
        }
    }

    @Override
    protected void onStart() {
        super.onStart()
        supportActionBar.with {
            displayHomeAsUpEnabled = true
            homeButtonEnabled = true
            it.title = this.title
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        def inflater = this.menuInflater
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu)
    }

    /* Called whenever we call invalidateOptionsMenu() */

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        switch (item.itemId) {
            case R.id.action_search:
                Toast.makeText(this, R.string.action_search, Toast.LENGTH_SHORT).show()
                return true
            default:
                return super.onOptionsItemSelected(item)
        }
    }

    private void selectItem(int position) {
        Fragment fragment = new ScreenPagerFragment()
        fragment.setArguments([fragmentType: position].asBundle())
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit()
        drawerList.setItemChecked(position, true)
        setTitle(screenTitles[position])
        drawerLayout.closeDrawer(drawerList)
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

}
