package com.stream.baba.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


data class HomePageList(
    val name: String,
    var list: List<Data>
)

data class Collection @JvmOverloads constructor(
    @get:PropertyName("Types") @set:PropertyName("Types") var Types: String = ""
)

data class CollectionName(
    var Types: MutableList<Collection> ,
)



data class Data @JvmOverloads constructor(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("year") @set:PropertyName("year") var year: Int = 2000,
    @get:PropertyName("photo") @set:PropertyName("photo") var photo: String = "",
    @get:PropertyName("language") @set:PropertyName("language") var language: String = "",
    @get:PropertyName("webLink") @set:PropertyName("webLink") var webLink: String = "",
    @get:PropertyName("downloadLink") @set:PropertyName("downloadLink") var downloadLink: String = "",
    @get:PropertyName("genres") @set:PropertyName("genres") var genres: String = "",
    @get:PropertyName("rating") @set:PropertyName("rating") var rating: String = "0.0",
    @get:PropertyName("isMovie") @set:PropertyName("isMovie") var isMovie: Boolean = true,
    @get:PropertyName("isBanner") @set:PropertyName("isBanner") var isBanner: Boolean = false

)

data class epData @JvmOverloads constructor(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("webLink") @set:PropertyName("webLink") var webLink: String = "",
    @get:PropertyName("downloadLink") @set:PropertyName("downloadLink") var downloadLink: String = ""

)


suspend fun getCollectionIds(): CollectionName? {
    val firestore = Firebase.firestore
    try {
        val collection = firestore.collection("collectionNames").get().await()
            val data = collection.toObjects(Collection::class.java)
        return CollectionName(data)
    }catch (e: Exception) {
        // Handle any exceptions that occurred during Firestore retrieval
        e.printStackTrace()
    }
    return null
}

suspend fun getHomePageListFromFirestore(
    firestore: FirebaseFirestore,
    name: String
): HomePageList? {

    try {
        var data :List<Data>? = null

            val querySnapshot = firestore.collection(name)
                .limit(10)
                .get()
                .await()

      // val snapshot: QuerySnapshot = firestore.collection(name).get().await()
        val dataList = querySnapshot.toObjects(Data::class.java)
        var typeName : String? = null

        if (!dataList.isNullOrEmpty()) {
            data = dataList
            typeName = name
        }
        return HomePageList(typeName!!, data!!)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}
suspend fun getEpResultPageListFromFirestore(
    firestore: FirebaseFirestore,
    Types: String,
    name: String
): MutableList<epData>? {
    try {
        val snapshot: QuerySnapshot =
            firestore.collection(Types).document(name).collection(name).get().await()

        return snapshot.toObjects(epData::class.java)
    } catch (e: Exception) {
        // Handle any exceptions that occurred during Firestore retrieval
        e.printStackTrace()
    }

    return null
}

    suspend fun getSearchPageListFromFirestore(name: String, searchName: String): HomePageList? {
        val firestore = Firebase.firestore
        try {
            var data :List<Data>? = null
            val snapshot: QuerySnapshot? = firestore.collection(name)
                .whereGreaterThanOrEqualTo("name", searchName)
                .whereLessThanOrEqualTo("name", searchName + "\uf8ff")
                .get().await()
            val dataList: List<Data> = snapshot!!.toObjects(Data::class.java)
            var typeName : String? = null
            if (dataList.isNotEmpty()) {
                 data = dataList
                typeName = name
            }
            return HomePageList(typeName!!, data!!)
        } catch (e: Exception) {
            // Handle any exceptions that occurred during Firestore retrieval
            e.printStackTrace()
        }

        return null
    }




    suspend fun getExpandedData(
        firestore: FirebaseFirestore,
        name: String,
        lastVisible: DocumentSnapshot?
    ): Pair<HomePageList, DocumentSnapshot>? {
        try {
            var data: List<Data>? = null
            val nextPageSize = 2

            val main = firestore.collection(name).limit(1).get().await()
                val next = firestore.collection(name)
                    .startAfter(lastVisible?: main.documents[main.size() - 1])
                    .limit(nextPageSize.toLong())
                    .get().await()

                val dataList = next.toObjects(Data::class.java)
                var typeName: String? = null

                if (dataList.isNotEmpty()) {
                    data = dataList
                    typeName = name
                }

                return Pair(HomePageList(typeName!!, data!!), next.documents[next.size() - 1])

        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
            return null
        }

    }


    suspend fun getSearchDataListFromFirestore(name: String, searchName: String): List<Data>? {
        val firestore = Firebase.firestore
        try {
            val data = ArrayList<Data>()
            val snapshot: QuerySnapshot? = firestore.collection(name)
                .whereGreaterThanOrEqualTo("name", searchName)
                .whereLessThanOrEqualTo("name", searchName + "\uf8ff")
                .get().await()
            val dataList: List<Data> = snapshot!!.toObjects(Data::class.java)
            for (i in dataList) {
                if (i.toString().isNotEmpty()) {
                    data.add(i)
                }
            }
            return data
        } catch (e: Exception) {
            // Handle any exceptions that occurred during Firestore retrieval
            e.printStackTrace()
        }

        return null
    }





