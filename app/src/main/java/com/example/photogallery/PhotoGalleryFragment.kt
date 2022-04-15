package com.example.photogallery

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photogallery.connect.FlickrApi
import com.example.photogallery.connect.FlickrFetchr
import com.example.photogallery.data.GalleryItem
import com.example.photogallery.data.PhotoGalleryViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val TAG = "PhotoGalleryFragment"
class PhotoGalleryFragment: Fragment() {

    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

      photoGalleryViewModel =
          ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery,container,false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context,3)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
               photoRecyclerView.adapter = PhotoAdapter(galleryItems)
            }
        )
    }

    private class PhotoHolder(private val itemImageView : ImageView) : RecyclerView.ViewHolder(itemImageView){
        fun bindDrawable (context: Context,url:String,placeHolde:Drawable){
            Glide.with(context)
                .load(url)
                .placeholder(placeHolde)
                .into(itemImageView);
        }
    }

    private inner class PhotoAdapter(private val galleryItems:List<GalleryItem>)
        :RecyclerView.Adapter<PhotoHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater.inflate(R.layout.list_item_gallery,parent,false) as ImageView
            return PhotoHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            val placeholder:Drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.x_space_launch
            ) ?: ColorDrawable()
            holder.bindDrawable(requireContext(),galleryItem.url,placeholder)
        }

        override fun getItemCount(): Int = galleryItems.size

    }

    companion object{
        fun newInstance() = PhotoGalleryFragment()
    }
}