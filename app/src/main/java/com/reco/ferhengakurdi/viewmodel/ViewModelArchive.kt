package com.reco.ferhengakurdi.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.reco.ferhengakurdi.model.ApiResponse
import com.reco.ferhengakurdi.model.AppDatabase
import com.reco.ferhengakurdi.model.DetailScreenListModel
import com.reco.ferhengakurdi.model.GalleryInfo
import com.reco.ferhengakurdi.model.GalleryInfoEntity
import khttp.get
import khttp.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

class ViewModelArchive(val database: AppDatabase) : ViewModel() {

    var parsedGallery by mutableStateOf<List<GalleryInfo>>(emptyList())

    var detailArchive by mutableStateOf<List<DetailScreenListModel>>(emptyList())
        private set

    var query by mutableStateOf("")
        private set

    val TAG = "ViewModelArchive"

    fun updateQuery(str: String) {
        query = str
    }

    fun getGalleryList(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDataAvailable = database.galleryInfoDao().getAll().isNotEmpty()

            if (isDataAvailable) {
                val galleryFromDb = withContext(Dispatchers.IO) {
                    database.galleryInfoDao().getAll()
                }
                withContext(Dispatchers.Main) {
                    parsedGallery = galleryFromDb.map {
                        GalleryInfo(it.title, it.imageUrl, it.galleryId)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Wait...", Toast.LENGTH_LONG).show()
                }
                val response = get("https://kurdistan.photoshelter.com/gallery-list")
                if (response.statusCode == 200) {
                    runBlocking {
                        val parsedGalleryList = parseGallery(response.text)
                        getGalleryDetail(parsedGalleryList)
                        saveGalleryData(parsedGalleryList)
                        withContext(Dispatchers.Main) {
                            parsedGallery = parsedGalleryList
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveGalleryData(parsedGallery: List<GalleryInfo>) {
        val galleryInfoEntities = parsedGallery.map {
            GalleryInfoEntity(title = it.title, imageUrl = it.imageUrl, galleryId = it.galleryId)
        }
        withContext(Dispatchers.IO) {
            database.galleryInfoDao().insertAll(galleryInfoEntities)
        }
    }

    private fun parseGallery(html: String): List<GalleryInfo> {
        val document: Document = Jsoup.parse(html)
        val galleryList = mutableListOf<GalleryInfo>()

        val galleryElements = document.select("li.gallery")
        for (galleryElement in galleryElements) {
            val title = galleryElement.select("a.collections_galleries_list_name").text()
            val imageUrl = galleryElement.select("img").attr("src")
            val galleryUrl = galleryElement.select("td.slide a").attr("href")
            val galleryId = extractGalleryId(galleryUrl)

            if (title.isNotEmpty() && imageUrl.isNotEmpty() && galleryId.isNotEmpty()) {
                val galleryInfo = GalleryInfo(title, imageUrl, galleryId)
                galleryList.add(galleryInfo)
            }
        }

        return galleryList
    }

    private fun extractGalleryId(galleryUrl: String): String {
        return try {
            val uri = URI(galleryUrl)
            val pathSegments = uri.path.split("/")
            pathSegments.lastOrNull { it.isNotEmpty() } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun getGalleryDetail(items: List<GalleryInfo>) {
        val semaphore = Semaphore(15) // Limit to a maximum of 15 concurrent coroutines
        val deferredList = items.map { item ->
            GlobalScope.async(Dispatchers.IO) {
                semaphore.acquire() // Acquire a permit from the semaphore
                try {
                    val response = post(
                        "https://kurdistan.photoshelter.com/psapi/v2.0/gallery/${item.galleryId}",
                        data = "fields=*&f_https_link=t&api_key=PS631731c7",
                        headers = mapOf("Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8")
                    ).jsonObject.getJSONObject("data").getJSONObject("key_image").getString("link")
                    item.imageUrl = response
                } finally {
                    semaphore.release() // Release the permit back to the semaphore
                }
            }
        }

        deferredList.awaitAll()
    }


    fun getArchiveImages(id: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = post(
                    "https://kurdistan.photoshelter.com/psapi/v2.0/gallery/$id/images",
                    data = "fields=*&f_https_link=t&api_key=PS631731c7",
                    headers = mapOf("Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8")
                )
                val gson = Gson()
                val apiResponse: ApiResponse = gson.fromJson(response.text, ApiResponse::class.java)

                if (apiResponse.status == "ok") {
                    withContext(Dispatchers.Main) {
                        detailArchive = apiResponse.data.images
                        onComplete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchQuery(){
        viewModelScope.launch(Dispatchers.IO){
            try {
                val response = get("https://kurdistan.photoshelter.com/search?I_DSC=$query&I_SDATE%5BMM%5D=&I_SDATE%5BDD%5D=DD&I_SDATE%5BYYYY%5D=YYYY&I_EDATE%5BMM%5D=&I_EDATE%5BDD%5D=DD&I_EDATE%5BYYYY%5D=YYYY&I_CITY=&I_STATE=&I_COUNTRY_ISO=&I_ORIENTATION=&I_IS_RELEASED=&I_IS_PRELEASED=&_CB_I_PR=t&_CB_I_PU=t&_CB_I_RF=t&_CB_I_RM=t&I_SORT=RANK&I_DSC_AND=t&V_ID=&G_ID=&C_ID=&_ACT=search")
                val a = parseGallery(response.text)
                println(a)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}
