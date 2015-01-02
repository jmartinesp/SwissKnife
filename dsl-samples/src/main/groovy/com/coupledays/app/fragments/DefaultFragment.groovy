package com.coupledays.app.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.AbsListView
import com.android.components.GArrayAdapter
import com.coupledays.app.R
import com.coupledays.app.ShowOnMapActivity
import com.coupledays.app.adapters.SliderPagerAdapter
import com.coupledays.app.utils.SwipeDetector
import com.coupledays.entity.Apartment
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener
import groovy.transform.CompileStatic

@CompileStatic
abstract class DefaultFragment extends Fragment implements AbsListView.OnScrollListener {
    List<Apartment> apartments
    int lastPosition = -1
    int offset = 0
    boolean loadingState = false

    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater.inflate(R.layout.main_fragment, container, false)
    }

    @Override
    void onViewCreated(View view1, Bundle savedInstanceState) {
        super.onViewCreated(view1, savedInstanceState)
        view1.async { fragment, task ->
            task.error { e ->
                e?.printStackTrace()
                view1.showToast(e?.getMessage())
            }
            task.after {
                injectListView()
            }
            this.apartments = getApartmentList(this.offset)
        }
    }

    void injectListView() {
        def listView = this.apartments.asListView(this.getActivity(), R.id.apartmentListView, R.layout.apartment_row_layout) { Apartment apartment, View rowView, int pos ->
            def toggleLayout = injectToggleLayout(rowView, apartment)
            def imageView = rowView.image(R.id.apartmentImageView)
            imageView.onClick {
                if (toggleLayout.isShown()) {
                    toggleLayout.visibility = View.GONE
                } else {
                    toggleLayout.visibility = View.VISIBLE
                }
            }
            ImageLoader.instance.displayImage("${Apartment.restUrl}${apartment.images?.first()}", imageView, ApartmentFragment.options)
            rowView.text(R.id.aparmentPrice).setText apartment.price?.toString()
            rowView.text(R.id.apartmentType).setText "${apartment.rooms}-комнатная"
            applyAnimation(rowView, pos)
        }
/*        listView.onClick { apartment, View view, int position ->
            def pager = ((ViewPager)listView.parent.parent.parent)
            ((SliderPagerAdapter)pager.adapter).setApartment((Apartment)apartment)
            pager.setCurrentItem(2)
        }*/
        listView.onScrollListener = new PauseOnScrollListener(ImageLoader.instance, true, true)
        listView.onTouchListener = new SwipeDetector(listView, ((SliderPagerAdapter) ((ViewPager) listView.parent.parent.parent).adapter))
        this.apartments.clear()
    }

    View injectToggleLayout(View rowView, Apartment apartment) {
        def layout = rowView.view(R.id.toggleLayout)
        layout.text(R.id.apartmentAddress).setText "Адрес: $apartment.address"
        layout.button(R.id.callHolderButton).onClick {
            layout.showToast("Позвонить $apartment.holder.phone")
        }
        layout.button(R.id.viewOnMapButton).onClick {
            def intent = new Intent(this.activity, ShowOnMapActivity)
            intent.putExtra('lat', apartment.lat)
            intent.putExtra('lon', apartment.lon)
            intent.putExtra('address', apartment.address)
            intent.putExtra('price', apartment.price)
            startActivity intent
        }
        layout.visible(false)
    }

    @Override
    void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!this.loadingState && firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
            this.loadingState = true
            absListView.async { listView, task ->
                task.error { e ->
                    e?.printStackTrace()
                    this.loadingState = false
                }
                task.after {
                    if (this.apartments.size() > 0) {
                        for (item in this.apartments) {
                            ((GArrayAdapter) listView.adapter).add(item)
                        }
                        ((GArrayAdapter) listView.adapter).notifyDataSetChanged()
                        this.apartments.clear()
                        this.offset += 1
                    }
                    this.loadingState = false
                }
                this.apartments = getApartmentList(this.offset)
            }
        }
    }

    @Newify(TranslateAnimation)
    private void applyAnimation(View view, int pos) {
        def animation = TranslateAnimation.new(0, 0, (pos > this.lastPosition) ? 100 : -100, 0)
        animation.duration = 400
        view.startAnimation animation
        this.lastPosition = pos
    }

    @Override
    void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    abstract List<Apartment> getApartmentList(int offset)
}
