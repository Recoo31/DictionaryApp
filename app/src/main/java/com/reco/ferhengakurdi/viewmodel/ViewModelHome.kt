package com.reco.ferhengakurdi.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.reco.ferhengakurdi.model.GetSimilarModel
import com.reco.ferhengakurdi.model.PhraseModel
import com.reco.ferhengakurdi.util.getGoogleToken
import khttp.get
import khttp.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLDecoder
import java.net.URLEncoder

class ViewModelHome : ViewModel() {
    var phrase by mutableStateOf<List<PhraseModel>>(emptyList())
    var otherTranslate by mutableStateOf<List<String>>(emptyList())
    var translateExampleTop by mutableStateOf<Pair<String, String>?>(null)
    var translateExample by mutableStateOf<List<Pair<String, String>>>(emptyList())
    var translationGlobse by mutableStateOf("")
    var translationGoogle by mutableStateOf("")
    val TAG = "ViewModelHome"
    lateinit var navigator: DestinationsNavigator

    var query by mutableStateOf("")
        private set
    var showSecond by mutableStateOf(false)

    var lang1 by mutableStateOf("tr")
    var lang2 by mutableStateOf("ku")

    fun swapLanguages() {
        val temp = lang1
        lang1 = lang2
        lang2 = temp
    }

    fun updateQuery(input: String) {
        query = input
    }

    fun similarPhrases() {
        Log.i("TAG", "similarPhrases: searching...")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response =
                    get("https://iapi.glosbe.com/iapi3/similar/similarPhrasesMany?p=$query&l1=$lang1&l2=$lang2&removeDuplicates=true&searchCriteria=WORDLIST-ALPHABETICALLY-2-s%3BPREFIX-PRIORITY-2-s%3BTRANSLITERATED-PRIORITY-2-s%3BFUZZY-PRIORITY-2-s%3BWORDLIST-ALPHABETICALLY-2-r%3BPREFIX-PRIORITY-2-r%3BTRANSLITERATED-PRIORITY-2-r%3BFUZZY-PRIORITY-2-r&env=tr")
                Log.d(TAG, "Response: ${response.text}")
                if (response.statusCode == 200) {
                    val gson = Gson()
                    val getSimilarModel: GetSimilarModel =
                        gson.fromJson(response.text, GetSimilarModel::class.java)
                    phrase = getSimilarModel.phrases
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getQuery() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = get("https://glosbe.com/$lang1/$lang2/$query")
                translateExampleTop = parseTranslateExample(response.text)
                otherTranslate = parseOtherTranslate(response.text)
                handlePagination(response.text)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseOtherTranslate(html: String): List<String> {
        val document: Document = Jsoup.parse(html)

        val contentSummary = document.select("#content-summary")
        val topTranslations = contentSummary.select("strong").text()

        return topTranslations.split(", ")
    }

    private fun handlePagination(html: String) {
        val parsedParagraphs = parseParagraphsFromHtml(html)
        translateExample = parsedParagraphs

        if (html.contains("LOAD MORE")) {
            val loadMoreLink = extractLoadMoreLink(html)
            val decodedLoadMoreLink = URLDecoder.decode(loadMoreLink, "UTF-8")
            if (decodedLoadMoreLink.isNotEmpty()) {
                val nextPageResponse = get("https://glosbe.com$decodedLoadMoreLink")
                translateExample = parseParagraphsFromHtml(nextPageResponse.text)
            }
        }
    }

    private fun extractLoadMoreLink(html: String): String {
        val document = Jsoup.parse(html)
        val loadMoreButton = document.select("button[data-fragment-url]")
        val a = loadMoreButton.attr("data-fragment-url")
        return a
    }


    fun getTranslateScore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = post(
                    "https://translator-api.glosbe.com/translateByLangWithScore?sourceLang=$lang1&targetLang=$lang2",
                    data = query
                )
                translationGlobse = response.jsonObject.getString("translation")

                val responseGoogle = post(
                    "https://translate.googleapis.com/translate_a/t?anno=3&client=te&format=html&v=1.0&key&sl=$lang1&tl=$lang2&sp=nmt&tc=1&tk=${getGoogleToken(query)}",
                    data = "q="+URLEncoder.encode(query, "UTF-8"),
                    headers = mapOf(
                        "content-type" to "application/x-www-form-urlencoded",
                    )
                ).jsonArray
                translationGoogle = responseGoogle.getString(0)
                showSecond = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun parseTranslateExample(html: String): Pair<String, String>? {
        val doc = Jsoup.parse(html)
        val paragraphs = doc.select("div.translation__example p")

        if (paragraphs.size >= 2) {
            val firstText = paragraphs[0].toString()
            val secondText = paragraphs[1].toString()
            return Pair(firstText, secondText)
        }
        return null
    }

    private fun isArabic(input: String): Boolean {
        val arabicPattern = Regex("\\p{InArabic}")
        return arabicPattern.containsMatchIn(input)
    }

    private fun parseParagraphsFromHtml(html: String): List<Pair<String, String>> {
        val document = Jsoup.parse(html)
        val paragraphs = document.select("div.px-1.text-sm.text-gray-900.break-words > div")

        val parsedParagraphs = mutableListOf<Pair<String, String>>()

        paragraphs.forEach { paragraph ->
            val firstHalf = paragraph.select("div[class=w-1/2 dir-aware-pr-1]").toString()
            val secondHalf = paragraph.select("div[class=w-1/2 dir-aware-pl-1]").toString()

            val pair = Pair(firstHalf.trim(), secondHalf.trim())
            if (!isArabic(firstHalf) && !isArabic(secondHalf)) {
                if (pair.first.isNotBlank() || pair.second.isNotBlank()) {
                    parsedParagraphs.add(pair)
                }
            }
        }

        return parsedParagraphs
    }

}