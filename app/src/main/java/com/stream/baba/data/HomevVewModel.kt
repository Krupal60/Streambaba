package com.stream.baba.data


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _data = MutableLiveData<List<HomePageList?>>()
    val data: LiveData<List<HomePageList?>> get() = _data

    private val _expandedData = MutableLiveData<HomePageList?>()
    val expandedData: LiveData<HomePageList?> get() = _expandedData

    private var isLoading = false

    private var lastVisible: DocumentSnapshot? = null

    private val _epData = MutableLiveData<List<epData?>>()
    val epData: LiveData<List<epData?>> get() = _epData

    private val _searchdata = MutableLiveData<List<HomePageList?>>()
    val searchdata: LiveData<List<HomePageList?>> get() = _searchdata

    private val _homeSearchdata = MutableLiveData<List<Data?>>()
    val homeSearchdata: LiveData<List<Data?>> get() = _homeSearchdata

    private val _query = MutableLiveData<String>()
    val query: MutableLiveData<String> get() = _query

    private val _bannerData = MutableLiveData<List<Data?>>()
    val bannerData: LiveData<List<Data?>> get() = _bannerData

    private val _selectedData = MutableLiveData<Data?>()
    val selectedData: LiveData<Data?> = _selectedData

    fun setSelectedData(data: Data?) {
        _selectedData.value = data
        Log.d("_selectedData", _selectedData.value.toString())
    }

    fun setquery(query: String?) {
        _query.value = query.toString()
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun loadData() {
        GlobalScope.launch(Dispatchers.Main) {
            val typesName = getCollectionIds()
            val homePageList = mutableListOf<HomePageList>() // Initialize an empty list
            val BannerData = mutableListOf<Data>()
            val firestore = Firebase.firestore


            typesName?.Types?.forEach { type ->
                val pageList = getHomePageListFromFirestore(firestore, type.Types)
                pageList?.let { data ->
                    homePageList.add(
                        data!!
                    )
                }
            }
            _data.value = homePageList

            homePageList.forEach { data ->
                data.list.forEach { it ->
                    if (it.isBanner) {
                        BannerData.add(it)
                    }
                }

            }
            _bannerData.value =
                BannerData // Handle the case where bannerList is null
        }
    }


    fun createPager(name: String): Flow<PagingData<Data>> {
        return Pager(PagingConfig(200)) {
            Log.d("name",name)
            FirestorePagingSource(FirebaseFirestore.getInstance(), name)
        }.flow.cachedIn(viewModelScope)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun loadEpData(name: String, type: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val firestore = Firebase.firestore
            Log.d("Name", "$name,$type")
            val epPageList = getEpResultPageListFromFirestore(firestore, type, name)
            _epData.value = epPageList ?: emptyList()
            Log.d("Hello", epPageList.toString())
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun loadSearchData(name: String) {
        Log.d("serach", "name$name")
        if (name != "") {
            GlobalScope.launch(Dispatchers.Main) {
                val typesName = getCollectionIds()
                val searchPageList = mutableListOf<HomePageList>() // Initialize an empty list
                typesName?.Types?.forEach { type ->
                    val pageList = getSearchPageListFromFirestore(type.Types, name)
                    pageList?.let { searchPageList.add(it) }
                }
                _searchdata.value = searchPageList
            }
        }
    }


    fun loadHomeSearchData(name: String) {
        if (name != "") {
            viewModelScope.launch(Dispatchers.Main) {
                val typesName = getCollectionIds()
                val searchResults = ArrayList<Data>() // List to hold all search results

                typesName?.Types?.forEach { type ->
                    val pageList = getSearchDataListFromFirestore(type.Types, name)
                    searchResults.addAll(pageList ?: emptyList())
                }

                _homeSearchdata.value = searchResults // Set the search results once
            }
        }
    }




}
