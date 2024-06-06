package com.stream.baba.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FirestorePagingSource(
    private val db: FirebaseFirestore,
    private val name: String
) : PagingSource<QuerySnapshot, Data>() {

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Data> {
        return try {
            // Step 1
            val currentPage = params.key ?: db.collection(name)
                .limit(12)
                .get()
                .await()

            // Step 2
            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            // Step 3
            val nextPage = db.collection(name).limit(12).startAfter(lastDocumentSnapshot)
                .get()
                .await()

            Log.d("data",currentPage.toObjects(Data::class.java).toString())
            // Step 4
            LoadResult.Page(
                data = currentPage.toObjects(Data::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            Log.e("FirestorePagingSource", "Error loading data: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Data>): QuerySnapshot? {
        TODO("Not yet implemented")
    }


}