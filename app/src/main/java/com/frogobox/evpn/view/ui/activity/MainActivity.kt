package com.frogobox.evpn.view.ui.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.frogobox.evpn.R
import com.frogobox.evpn.base.BaseActivity
import com.frogobox.evpn.helper.Constant.Variable.EXTRA_COUNTRY
import com.frogobox.evpn.source.model.Server
import com.frogobox.evpn.util.PropertiesService
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.hookedonplay.decoviewlib.events.DecoEvent.ExecuteEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.util.*

class MainActivity : BaseActivity() {

    private var popupWindow: PopupWindow? = null
    private var countryList: List<Server>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val totalServ = dbHelper.count
        val totalServers = String.format(resources.getString(R.string.total_servers), totalServ)
        countryList = dbHelper.uniqueCountries
        setSupportActionBar(toolbar_main)
        setupShowAdsInterstitial()
        setupShowAdsBanner(findViewById(R.id.admob_adview))
        checkState()
        centree.text = totalServers

        dynamicArcView3.visibility = View.VISIBLE
        dynamicArcView2.visibility = View.GONE
        dynamicArcView2.addSeries(SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0f, 100f, 0f)
                .setInterpolator(AccelerateInterpolator())
                .build())
        val seriesItem1 = SeriesItem.Builder(Color.parseColor("#00000000"))
                .setRange(0f, 100f, 0f)
                .setLineWidth(32f)
                .build()
        val seriesItem2 = SeriesItem.Builder(Color.parseColor("#ffffff"))
                .setRange(0f, 100f, 0f)
                .setLineWidth(32f)
                .build()
        val series1Index2 = dynamicArcView2.addSeries(seriesItem2)
        val proc = (Random().nextInt(10) + 5).toFloat()
        dynamicArcView2.addEvent(DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(0)
                .setDuration(600)
                .build())
        dynamicArcView2.addEvent(DecoEvent.Builder(proc).setIndex(series1Index2).setDelay(2000).setListener(object : ExecuteEventListener {
            override fun onEventStart(decoEvent: DecoEvent) {}
            override fun onEventEnd(decoEvent: DecoEvent) {
                val totalServ = dbHelper.count
                val totalServers = String.format(resources.getString(R.string.total_servers), totalServ)
                centree.text = totalServers
            }
        }).build())
        homeBtnRandomConnection.setOnClickListener { v: View? ->
            val randomServer = randomServer
            if (randomServer != null) {
                newConnecting(randomServer, true, true)
            } else {
                val randomError = String.format(resources.getString(R.string.error_random_country), PropertiesService.getSelectedCountry())
                Toast.makeText(this@MainActivity, randomError, Toast.LENGTH_LONG).show()
            }
        }
        homeBtnChooseCountry.setOnClickListener { v: View? -> chooseCountry(initPopUp()) }
    }

    private fun checkState() {
        if (connectedServer == null) {
            elapse2.setBackgroundResource(R.drawable.button2)
            elapse2.text = "No VPN Connected"
        } else {
            elapse2.text = "Connected"
            elapse2.setBackgroundResource(R.drawable.button3)
        }
    }

    override fun onResume() {
        super.onResume()
        checkState()
        invalidateOptionsMenu()
    }

    private fun initPopUp(): View {
        val inflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.choose_country, null)
        val widthWindow = 300
        val heightWindow = 500
        popupWindow = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PopupWindow(
                    view,
                    (widthWindow * 0.6.toFloat()).toInt(),
                    (heightWindow * 0.8.toFloat()).toInt()
            )
        } else {
            PopupWindow(
                    view,
                    (widthWindow * 0.8.toFloat()).toInt(),
                    (heightWindow * 0.7.toFloat()).toInt()
            )
        }
        popupWindow!!.isOutsideTouchable = false
        popupWindow!!.isFocusable = true
        popupWindow!!.setBackgroundDrawable(BitmapDrawable())
        return view
    }

    private fun chooseCountry(view: View) {
        val homeCountryList = view.findViewById<ListView>(R.id.homeCountryList)
        val countryListName: MutableList<String?> = ArrayList()
        for (server in countryList!!) {
            val localeCountryName = if (localeCountries[server.countryShort] != null) localeCountries[server.countryShort] else server.countryLong
            countryListName.add(localeCountryName)
        }
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, countryListName)
        homeCountryList.adapter = adapter
        homeCountryList.onItemClickListener = OnItemClickListener { parent: AdapterView<*>?, view1: View?, position: Int, id: Long ->
            popupWindow!!.dismiss()
            startActivity(Intent(this, VPNListActivity::class.java).putExtra(EXTRA_COUNTRY, countryList!![position].countryShort))
        }
        popupWindow!!.showAtLocation(homeContextRL, Gravity.CENTER, 0, 0)
    }
}