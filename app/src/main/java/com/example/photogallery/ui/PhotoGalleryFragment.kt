package com.example.photogallery.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bumptech.glide.Glide
import com.example.photogallery.R
import com.example.photogallery.data.GalleryItem
import com.example.photogallery.data.PhotoGalleryViewModel
import com.example.photogallery.sharePreference.QueryPreferences
import com.example.photogallery.workManager.PollWorker
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"
class PhotoGalleryFragment: VisibleFragment() {

    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

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

        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if(isPolling){
            R.string.stop_polling
        }else{
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)


    }

    //Menu selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling){
                    WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(),false)
                }else{
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest =
                        PeriodicWorkRequestBuilder<PollWorker>(15,TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicRequest)
                    QueryPreferences.setPolling(requireContext(),true)

                }
                //invalidateOptionsMenu()用來表示Android，選單內容已更改
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class PhotoHolder(private val itemImageView : ImageView)
        : RecyclerView.ViewHolder(itemImageView),View.OnClickListener{

        private lateinit var galleryItem: GalleryItem

        init {
            itemView.setOnClickListener(this)
        }

        fun bindDrawable (context: Context,url:String,placeHolde:Drawable){
            Glide.with(context)
                .load(url)
                .placeholder(placeHolde)
                .into(itemImageView);
        }

        fun bindGalleryItem(item : GalleryItem){
            galleryItem = item
        }

        override fun onClick(p0: View?) {
            val intent = PhotoPageActivity.newIntent(requireContext(),galleryItem.photoPageUri)
            startActivity(intent)
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
            holder.bindGalleryItem(galleryItem)
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