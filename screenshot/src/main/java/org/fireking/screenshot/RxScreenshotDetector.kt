package org.fireking.screenshot

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable

/**
 * https://github.com/Piasy/RxScreenshotDetector
 * Created by fireking on 2017/10/8.
 */
class RxScreenshotDetector constructor(activity: Activity) {

    private val log_tag = "RxScreenshotDetector"
    private val EXTERNAL_CONTENT_URI_MATCHER = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()
    private val PROJECTION = arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED)
    private val SORT_ORDER = MediaStore.Images.Media.DATE_ADDED + " DESC"
    private val DEFAULT_DETECT_WINDOW_SECONDS: Long = 10
    private val mActivity: Activity = activity
    private val rxPermission: RxPermissions

    init {
        rxPermission = RxPermissions(mActivity)
    }

    fun start(): Observable<String> {
        return execute()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun execute(): Observable<String> {
        return rxPermission.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .flatMap {
                    if (it) {
                        startAfterPermissionGranted(mActivity)
                    } else {
                        Observable.error<String>(SecurityException("Permission not granted"))
                    }
                }
    }

    private fun matchPath(path: String): Boolean {
        return path.toLowerCase().contains("screenshot") || path.contains("截屏") ||
                path.contains("截图")
    }

    private fun matchTime(currentTime: Long, dateAdded: Long): Boolean {
        return Math.abs(currentTime - dateAdded) <= DEFAULT_DETECT_WINDOW_SECONDS
    }

    private fun startAfterPermissionGranted(activity: Activity): Observable<String> {
        val contentResolver = activity.contentResolver

        return Observable.create<String> { emitter ->
            val contentObserver = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean, uri: Uri) {
                    Log.d(log_tag, "onChange: " + selfChange + ", " + uri.toString())
                    if (uri.toString().startsWith(EXTERNAL_CONTENT_URI_MATCHER)) {
                        var cursor: Cursor? = null
                        try {
                            cursor = contentResolver.query(uri, PROJECTION, null, null,
                                    SORT_ORDER)
                            if (cursor != null && cursor.moveToFirst()) {
                                val path = cursor.getString(
                                        cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                                val dateAdded = cursor.getLong(cursor.getColumnIndex(
                                        MediaStore.Images.Media.DATE_ADDED))
                                val currentTime = System.currentTimeMillis() / 1000
                                Log.d(log_tag, "path: " + path + ", dateAdded: " + dateAdded +
                                        ", currentTime: " + currentTime)
                                if (matchPath(path) && matchTime(currentTime, dateAdded)) {
                                    emitter.onNext(path)
                                }
                            }
                        } catch (e: Exception) {
                            Log.d(log_tag, "open cursor fail")
                        } finally {
                            if (cursor != null) {
                                cursor.close()
                            }
                        }
                    }
                    super.onChange(selfChange, uri)
                }
            }
            contentResolver.registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)

            emitter.setCancellable { contentResolver.unregisterContentObserver(contentObserver) }
        }
    }
}