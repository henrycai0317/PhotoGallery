package com.example.photogallery.workManager

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.photogallery.NOTIFICATION_CHANNEL_ID
import com.example.photogallery.R
import com.example.photogallery.connect.FlickrFetchr
import com.example.photogallery.data.GalleryItem
import com.example.photogallery.sharePreference.QueryPreferences
import com.example.photogallery.ui.PhotoGalleryActivity

private const val TAG = "PollWorker"
class PollWorker(val context: Context,workerParams:WorkerParameters)
            :  Worker(context,workerParams){

    override fun doWork(): Result {
        val query = QueryPreferences.getStoredQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)
        val items : List<GalleryItem> = if(query.isEmpty()) {
            FlickrFetchr().fetchPhotosRequest()
                .execute()
                .body()?.photos?.galleryItems
        }else{
            FlickrFetchr().searchPhotosRequest(query)
                .execute()
                .body()?.photos?.galleryItems
        } ?: emptyList()

        if(items.isEmpty()){
            return Result.success()
        }

        val resultId = items.first().id
        if(resultId == lastResultId){
            Log.i(TAG, "Got an old result: $resultId")
        }else{
            Log.i(TAG, "Got a new result: $resultId")
            QueryPreferences.setLastResultId(context,resultId)

            val intent = PhotoGalleryActivity.newIntent(context)    // 用FLAG_IMMUTABLE 標記構建了無法被修改的 PendingIntent  (Android 12 改版需求，必須加入 PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE 標籤)
            val pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_IMMUTABLE)

            val resources = context.resources
            val notification = NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(0,notification)
        }
        return Result.success()
    }

}