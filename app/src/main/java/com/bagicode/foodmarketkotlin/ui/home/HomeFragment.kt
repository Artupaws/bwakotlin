package com.bagicode.foodmarketkotlin.ui.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bagicode.foodmarketkotlin.FoodMarket
import com.bagicode.foodmarketkotlin.R
import com.bagicode.foodmarketkotlin.model.dummy.HomeModel
import com.bagicode.foodmarketkotlin.model.response.home.Data
import com.bagicode.foodmarketkotlin.model.response.home.HomeResponse
import com.bagicode.foodmarketkotlin.model.response.login.User
import com.bagicode.foodmarketkotlin.ui.detail.DetailActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.flow.flowOf

class HomeFragment : Fragment(),HomeAdapter.ItemAdapterCallback, HomeContract.View {

    private var newStateList : ArrayList<Data> = ArrayList()
    private var popularList : ArrayList<Data> = ArrayList()
    private var recomendedList : ArrayList<Data> = ArrayList()

    private lateinit var presenter:HomePresenter

    var progressDialog : Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initView()
        presenter = HomePresenter(this)
        presenter.getHome()
        //initDataDummy()



    }

    private fun initView() {
        progressDialog = Dialog(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_loader, null)

        progressDialog?.let {
            it.setContentView(dialogLayout)
            it.setCancelable(false)
            it.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        var user = FoodMarket.getApp().getUser()
        var userResponse = Gson().fromJson(user, User::class.java)

        if (!userResponse.profile_photo_url.isNullOrEmpty()) {
            Glide.with(requireActivity())
                .load(userResponse.profile_photo_url)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfil)
        }
    }

//    fun initDataDummy() {
//        foodList = ArrayList()
//        foodList.add(HomeModel("Cherry Healthy","",5f))
//        foodList.add(HomeModel("Burger Tamayo","",4f))
//        foodList.add(HomeModel("Bakhwan Cihuy","",4.5f))
//
//    }

    override fun onClick(v: View, data: Data) {
        val detail = Intent(activity, DetailActivity::class.java).putExtra("data", data)
        startActivity(detail)
    }

    override fun onHomeSuccess(homeResponse: HomeResponse) {

        for (a in homeResponse.data.indices) {

            var items:List<String> = homeResponse.data[a].types?.split(",") ?: ArrayList()
            for (x in items.indices) {
                when {
                    items[x].equals("new_food", true) -> {
                        newStateList?.add(homeResponse.data[a])
                    }
                    items[x].equals("recommended", true) -> {
                        recomendedList?.add(homeResponse.data[a])
                    }
                    items[x].equals("popular", true) -> {
                        popularList?.add(homeResponse.data[a])
                    }
                }
            }

        }

        var adapter = HomeAdapter(homeResponse.data, this)
        var layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rcList.layoutManager = layoutManager
        rcList.adapter = adapter

        val sectionPagerAdapter = SectionPagerAdapter(
            childFragmentManager
        )
        sectionPagerAdapter.setData(newStateList, popularList, recomendedList)
        viewPager.adapter = sectionPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onHomeFailed(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        progressDialog?.show()
    }

    override fun dismissLoading() {
        progressDialog?.dismiss()
    }
}