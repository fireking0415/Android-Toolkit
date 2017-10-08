## 全局截图分享功能(见模块screenshot)

* 核心思想

通过监听MediaStore图片文件的变化，对于满足条件的图片获取之后，返回到一个singleTask的类中，该类处理图片的相关操作。

* 核心代码 （代码使用kotlin实现）

```
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
```