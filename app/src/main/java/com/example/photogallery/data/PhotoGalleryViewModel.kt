package com.example.photogallery.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.photogallery.connect.FlickrFetchr

class PhotoGalleryViewModel:ViewModel(){
    val galleryItemLiveData:LiveData<List<GalleryItem>>

    init {
        galleryItemLiveData = FlickrFetchr().fetchPhotos()
    }
}