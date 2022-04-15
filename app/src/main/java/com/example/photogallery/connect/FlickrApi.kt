package com.example.photogallery.connect

import com.example.photogallery.data.FlickrResponse
import retrofit2.Call
import retrofit2.http.GET

interface FlickrApi {
    @GET("services/rest/?method=flickr.interestingness.getList" +
            "&api_key=f98a521e266690fcd58ed39c9f07d1a1" +
            "&format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s"
    )
    fun fetchPhotos():Call<FlickrResponse>
}