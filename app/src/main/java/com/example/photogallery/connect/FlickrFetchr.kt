package com.example.photogallery.connect

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.photogallery.data.FlickrResponse
import com.example.photogallery.data.GalleryItem
import com.example.photogallery.data.PhotoResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr {

    private val flickrApi:FlickrApi

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

         flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos():LiveData<List<GalleryItem>>{
        return fetchPhotoMetadata(flickrApi.fetchPhotos())
    }

    fun searchPhotos(query : String) : LiveData<List<GalleryItem>>{
        return fetchPhotoMetadata(flickrApi.searchPhotos(query))
    }

    private fun fetchPhotoMetadata(flickrRequest : Call<FlickrResponse>)
                :LiveData<List<GalleryItem>>{

        val responseLiveData:MutableLiveData<List<GalleryItem>> = MutableLiveData()

       flickrRequest.enqueue(object :Callback<FlickrResponse>{
           override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
               Log.d(TAG, "Response receiced")
               val flickrResponse: FlickrResponse? = response.body()
               val photoResponse:PhotoResponse? = flickrResponse?.photos
               var galleryItems:List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
               galleryItems = galleryItems.filterNot {
                   it.url.isBlank()
               }
               responseLiveData.value = galleryItems
           }

           override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
               Log.d(TAG, "Failed to fetch photos: ",t)
           }

       })
        return responseLiveData
    }

}