package com.coupledays.app

import android.content.res.Configuration
import android.os.Bundle
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
import com.coupledays.app.fragments.MainFragment
import com.coupledays.app.fragments.ScreenThree
import com.coupledays.app.fragments.ScreenTwo
import groovy.transform.CompileStatic


@CompileStatic
@InjectViews(R.layout.activity_main)
class MainActivityNew extends ActionBarActivity {

    String[] screenTitles
    DrawerLayout drawerLayout
    ListView drawerList
    ActionBarDrawerToggle drawerToggle
    CharSequence drawerTitle
    CharSequence title

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
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
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_search:
                // Show toast about click.
                Toast.makeText(this, R.string.action_search, Toast.LENGTH_SHORT).show()
                return true
            default:
                return super.onOptionsItemSelected(item)
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Update the main content by replacing fragments
        Fragment fragment = null
        switch (position) {
            case 0:
                fragment = new MainFragment()
                break;
            case 1:
                fragment = new ScreenTwo()
                break;
            case 2:
                fragment = new ScreenThree()
                break;
            default:
                break;
        }
        if (fragment) {
            this.supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit()
            drawerList.setItemChecked(position, true)
            setTitle(screenTitles[position])
            drawerLayout.closeDrawer(drawerList)
        } else {
            // Error
            Log.e("ERROR", "Error. Fragment is not created");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState()
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig)
    }

}
