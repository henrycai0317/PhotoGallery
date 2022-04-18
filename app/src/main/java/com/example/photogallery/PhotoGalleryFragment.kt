package com.example.photogallery

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
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
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog()
        setHasOptionsMenu(true)

      photoGalleryViewModel =
          ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
    }

    private fun loadingDialog() {
        alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Downloading photos")
            .setMessage("It might take a few seconds..")
            .show()
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
                alertDialog.dismiss()
            }
        )
    }
    //Add Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_photo_gallery,menu)

        val searchItem : MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "onQueryTextSubmit: $queryText")
                    photoGalleryViewModel.fetchPhotos(queryText)
                    clearFocus()
                    loadingDialog()
                    return true
                }

                override fun onQueryTextChange(queryText: String?): Boolean {
                    Log.d(TAG, "onQueryTextChange: $queryText")
                    return false
                }
            })
            //Add Search History Key Word
            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm,false)
            }

            setOnFocusChangeListener { view, hasFocus ->
                if(!hasFocus){
                    searchItem.collapseActionView()
                    clearFocus()
                }
            }

        }


    }

    //Menu selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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