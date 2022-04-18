package com.example.photogallery.data

import android.app.Application
import androidx.lifecycle.*
import com.example.photogallery.connect.FlickrFetchr
import com.example.photogallery.sharePreference.QueryPreferences
import retrofit2.http.Query

class PhotoGalleryViewModel(private val app:Application):AndroidViewModel(app){
    val galleryItemLiveData:LiveData<List<GalleryItem>>

    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()

    //從mutableSearchTerm得到Key world字 存到searchTerm裡頭
    val searchTerm :String
            get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)
        galleryItemLiveData =
            Transformations.switchMap(mutableSearchTerm){  searchTerm ->
                if (searchTerm.isBlank()){
                    flickrFetchr.fetchPhotos()
                }else{
                flickrFetchr.searchPhotos(searchTerm)
                }
            }
    }

    fun fetchPhotos(query: String = ""){
        QueryPreferences.setStoredQuery(app,query)
        mutableSearchTerm.value = query
    }

}