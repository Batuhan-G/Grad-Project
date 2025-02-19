package com.graduationproject.grad_project.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.graduationproject.grad_project.model.*
import com.onesignal.OneSignal
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

object UserOperations: FirebaseConstants() {

    private const val TAG = "UserOperations"

    suspend fun getAdmin(email: String): DocumentSnapshot? {
        return try {
            val admin = adminRef.document(email)
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "There is an admin user in this collection!")
                }
                .await()
            admin
        } catch (e: Exception) {
            Log.e(TAG, "getAdmin --> $e")
            null
        }
    }

    fun checkDeviceIDForAdmin() = CoroutineScope(ioDispatcher).launch {
        try {
            val deviceState = OneSignal.getDeviceState()
            val email = FirebaseAuth.getInstance().currentUser?.email
            val admin = email?.let {
                getAdmin(it)
            }
            if (admin?.get("player_id").toString() != deviceState?.userId) {
                admin?.reference?.update("player_id", deviceState?.userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkDeviceID -> $e")
        }
    }

    fun checkDeviceIDForResident() = CoroutineScope(ioDispatcher).launch {
        try {
            val deviceState = OneSignal.getDeviceState()
            val email = FirebaseAuth.getInstance().currentUser?.email
            val resident = email?.let {
                getResident(it)
            }
            if (resident?.get("player_id").toString() != deviceState?.userId) {
                resident?.reference?.update("player_id", deviceState?.userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkDeviceID -> $e")
        }
    }

    fun retrieveResidentDebt(debt: MutableLiveData<Long?>) = CoroutineScope(ioDispatcher).launch {
        try {
            val email = FirebaseAuth.getInstance().currentUser?.email
            email?.let {
                residentRef.document(it)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            Log.e(TAG, "retrieveResidentDebt -> $error")
                            return@addSnapshotListener
                        }
                        value?.get("debt").toString().toLong().also { myDebt ->
                            debt.postValue(myDebt)
                        }
                    }
            }
        } catch (e: FirebaseException) {
            Log.e(TAG, "retrieveResidentDebt -> $e")
        }
    }

    fun retrieveResidentNotificationCount(count: MutableLiveData<Long?>) = CoroutineScope(ioDispatcher).launch {
        try {
            val email = FirebaseAuth.getInstance().currentUser?.email
            email?.let {
                residentRef.document(it)
                    .collection("notifications")
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            Log.e(TAG, "retrieveResidentNotificationCount -> $error")
                            return@addSnapshotListener
                        }
                        value?.documents.also { list ->
                            count.postValue(list?.size?.toLong())
                        }
                    }
            }
        } catch (e: FirebaseException) {
            Log.e(TAG, "retrieveResidentNotificationCount -> $e")
        }
    }

    fun retrieveResidentRequestsCount(count: MutableLiveData<Long?>) = CoroutineScope(ioDispatcher).launch {
        try {
            val email = FirebaseAuth.getInstance().currentUser?.email
            email?.let {
                residentRef.document(it)
                    .collection("requests")
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            Log.e(TAG, "retrieveResidentRequestsCount -> $error")
                            return@addSnapshotListener
                        }
                        value?.documents.also { list ->
                            count.postValue(list?.size?.toLong())
                        }
                    }
            }
        } catch (e: FirebaseException) {
            Log.e(TAG, "retrieveResidentRequestsCount -> $e")
        }
    }

    fun retrieveSiteNameAndUserNameForAdmin(
        siteName: MutableLiveData<String?>,
        userName: MutableLiveData<String?>
    ) = CoroutineScope(ioDispatcher).launch {
        try {
            val email = FirebaseAuth.getInstance().currentUser?.email
            email?.let {
                adminRef.document(it).addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "retrieveSiteNameUserNameForResident --> $error")
                        return@addSnapshotListener
                    }
                    siteName.postValue(value?.get("siteName")?.toString())
                    userName.postValue(value?.get("fullName")?.toString())
                }
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "retrieveSiteNameForAdmin --> $e")
        }
    }

    fun retrieveSiteNameAndUserNameForResident(
        siteName: MutableLiveData<String?>,
        userName: MutableLiveData<String?>
    ) = CoroutineScope(ioDispatcher).launch {
        try {
            val email = FirebaseAuth.getInstance().currentUser?.email
            email?.let {
                residentRef.document(it).addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "retrieveSiteNameUserNameForResident --> $error")
                        return@addSnapshotListener
                    }
                    siteName.postValue(value?.get("siteName")?.toString())
                    userName.postValue(value?.get("fullName")?.toString())
                }
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "retrieveSiteNameForAdmin --> $e")
        }
    }

    fun saveMeetingNotification(emails: MutableLiveData<MutableList<String?>>, notification: Notification) = CoroutineScope(ioDispatcher).launch {
        try {
            emails.value?.forEach {
                it?.let {
                    residentRef.document(it)
                        .collection("notifications")
                        .document(notification.id)
                        .set(notification)
                        .await()
                }
            }
        } catch (e: FirebaseException) {
            Log.e(TAG, "saveMeetingNotification -> $e")
        }
    }


    fun deleteNotificationForAdmin(notification: Notification) = CoroutineScope(ioDispatcher).launch {
        try {
            val email = FirebaseAuth.getInstance().currentUser?.email
            email?.let {
                adminRef.document(it)
                    .collection("notifications")
                    .document(notification.id)
                    .delete()
                    .await()
            }
        } catch (e: FirebaseException) {
            Log.e(TAG, "deleteNotificationForAdmin -> $e")
        }
    }

    fun deleteAllNotificationsForAdmin() = CoroutineScope(ioDispatcher).launch {
        try {
            val email = FirebaseAuth.getInstance().currentUser?.email
            email?.let {
                adminRef.document(it)
                    .collection("notifications")
                    .get().await().also { querySnapshot ->
                        val documents = querySnapshot.documents
                        documents.forEach { document ->
                            document.reference.delete()
                        }
                    }
            }
        } catch (e: FirebaseException) {
            Log.e(TAG, "deleteNotificationForAdmin -> $e")
        }
    }


    fun checkRegistrationStatus(status: MutableLiveData<String?>) = CoroutineScope(ioDispatcher).launch {
        try {
            val email = FirebaseAuth.getInstance().currentUser?.email
            email?.let {
                residentRef.document(it)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            Log.e(TAG, "checkRegistrationStatus --> $error")
                            return@addSnapshotListener
                        }
                        println("status *> ${value?.get("registrationStatus")?.toString()}")
                        status.postValue(
                            value?.get("registrationStatus")?.toString()
                        )
                    }
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "checkRegistrationStatus -> $e")
        }
    }

    fun takeFlatNumbers(
        siteName: String,
        city: String,
        district: String,
        flatCount: Long,
        list: MutableLiveData<MutableList<Long?>>
    ) = CoroutineScope(ioDispatcher).launch {
        residentRef.whereEqualTo("siteName", siteName)
            .whereEqualTo("city", city)
            .whereEqualTo("district", district)
            .get()
            .await()
            .also {
                val documents = it.documents
                val flats = mutableListOf<Long?>()
                val numbers: MutableList<Long?> = (1..flatCount).toMutableList()
                documents.forEach { snapshot ->
                    flats.add(
                        snapshot["flatNo"].toString().toLong()
                    )
                }.also {
                    launch(Dispatchers.Default) {
                        for (i in 1..flatCount) {
                            if (flats.contains(i)) {
                                numbers.remove(i)
                            }
                        }
                    }
                    list.postValue(numbers)
                }
            }
    }

    fun isThereAnyResident(
        email: String,
        isThereAnyUser: MutableLiveData<Boolean?>
    ) = CoroutineScope(ioDispatcher).launch {
        try {
            residentRef.document(email)
                .get().await().also {
                    if (it["email"] != null) {
                        isThereAnyUser.postValue(true)
                    } else {
                        isThereAnyUser.postValue(false)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "isThereAnyResident --> $e")
            isThereAnyUser.postValue(true)
        }
    }

    fun isThereAnyAdmin(
        email: String,
        isThereAnyUser: MutableLiveData<Boolean?>
    ) = CoroutineScope(ioDispatcher).launch {
        try {
            adminRef.document(email)
                .get().await().also {
                    if (it["email"] != null) {
                        isThereAnyUser.postValue(true)
                    } else {
                        isThereAnyUser.postValue(false)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "isThereAnyResident --> $e")
            isThereAnyUser.postValue(true)
        }
    }

    /**
     * It will be used to send push notification to residents by using
     * their player_ids
     *
     * * */
    fun retrieveResidentsPlayerIDs(residentPlayerIDs: MutableLiveData<ArrayList<String?>>) = CoroutineScope(ioDispatcher).launch {
        val email = FirebaseAuth.getInstance().currentUser?.email
        val admin = email?.let {
            getAdmin(it)
        }

        val residents = async {
            admin?.let { getResidentsInASpecificSite(it) }
        }
        val residentDocuments = residents.await()?.documents
        val playerIDs = arrayListOf<String?>()
        if (residentDocuments != null) {
            println("docuemnt size ${residentDocuments.size}")
            residentDocuments.forEach {
                playerIDs.add(it["player_id"].toString())
            }.also {
                println("playerids size : ${playerIDs.size}")
                residentPlayerIDs.postValue(playerIDs)
            }
        }
    }



    fun retrieveAwaitingResidents(awaitingResidents: MutableLiveData<MutableList<SiteResident?>>)
        = CoroutineScope(ioDispatcher).launch {
        try {
            currentUserEmail?.let {
                val adminInfo = async {
                    getAdmin(it)
                }
                val query = residentRef.whereEqualTo("city", adminInfo.await()?.get("city"))
                    .whereEqualTo("district", adminInfo.await()?.get("district"))
                    .whereEqualTo("siteName", adminInfo.await()?.get("siteName"))
                    .whereEqualTo("registrationStatus", RegistrationStatus.PENDING)

                query.addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "retriveAwaitingResidents --> $error")
                        return@addSnapshotListener
                    }
                    val documents = value?.documents
                    documents?.sortBy {
                        it["flatNo"].toString().toInt()
                    }
                    val retrievedAwaitingResidents = mutableListOf<SiteResident?>()
                    documents?.forEach { document ->
                        retrievedAwaitingResidents.add(
                            document.toObject<SiteResident>()
                        )
                    }.also {
                        awaitingResidents.postValue(retrievedAwaitingResidents)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "retrieveAwaitingResidents -> $e")
        }
    }

    suspend fun getResidentsInASpecificSite(adminInfo: DocumentSnapshot): QuerySnapshot? {
        return try {
            residentRef.whereEqualTo("city", adminInfo["city"])
                .whereEqualTo("district", adminInfo["district"])
                .whereEqualTo("siteName", adminInfo["siteName"])
                .get().await()
        } catch (e: Exception) {
            Log.e(TAG, "getResidentsInASpecificSite -> $e")
            null
        }
    }

    fun retrieveResidentsInSpecificSite(siteResidents: MutableLiveData<MutableList<SiteResident?>>) = CoroutineScope(ioDispatcher).launch {
        try {
            val email = FirebaseAuth.getInstance().currentUser?.email
            val admin = async {
                email?.let { getAdmin(it) }
            }
            residentRef.whereEqualTo("city", admin.await()?.get("city"))
                .whereEqualTo("district", admin.await()?.get("district"))
                .whereEqualTo("siteName", admin.await()?.get("siteName"))
                .get().await().also {
                    it.toObjects<SiteResident>().also { list ->
                        siteResidents.postValue(list.toMutableList())
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "getResidentsInASpecificSite -> $e")
        }
    }


    suspend fun getResident(email: String): DocumentSnapshot? {
        return try {
            val resident = residentRef.document(email)
                .get()
                .await()
            resident
        } catch (e: Exception) {
            Log.e(TAG, "getResident --> $e")
            null
        }
    }

    fun takeResident(email: String, resident: MutableLiveData<SiteResident?>)
     = CoroutineScope(ioDispatcher).launch {
         try {
             residentRef.document(email)
                 .get()
                 .await().also {
                     val item = it.toObject<SiteResident?>()
                     resident.postValue(item)
                 }
         } catch (e: Exception) {
             Log.e(TAG, "takeResident --> $e")
             resident.postValue(null)
         }
    }


    fun takeAdminInSpecificSite(
        resident: SiteResident,
        admin: MutableLiveData<Administrator?>
    ) = CoroutineScope(ioDispatcher).launch {
        try {
            adminRef.whereEqualTo("city", resident.city)
                .whereEqualTo("district", resident.district)
                .whereEqualTo("siteName", resident.siteName)
                .get().await().also {
                    val documents = it.toObjects<Administrator>()
                    admin.postValue(documents[0])
                }
        } catch (e: Exception) {
            Log.e(TAG, "takeAdminInSpecificSite --> $e")
            admin.postValue(null)
        }
    }

    fun acceptResident(resident: SiteResident) = CoroutineScope(ioDispatcher).launch {
        try {
            residentRef.document(resident.email)
                .update("registrationStatus", RegistrationStatus.VERIFIED)
                .await().also {
                    Log.d(TAG, "acceptResident --> Updated resident verification status!")
                }
        } catch (e: Exception) {
            Log.e(TAG, "acceptResident --> $e")
        }
    }

    fun rejectResident(resident: SiteResident) = CoroutineScope(ioDispatcher).launch {
        try {
            residentRef.document(resident.email)
                .update("registrationStatus", RegistrationStatus.REJECTED)
                .await().also {
                    Log.d(TAG, "acceptResident --> Updated resident verification status!")
                }
        } catch (e: Exception) {
            Log.e(TAG, "acceptResident --> $e")
        }
    }

    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        scope: CoroutineDispatcher = Dispatchers.IO
    ): AuthResult? {
        return withContext(scope + coroutineExceptionHandler) {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "createUserWithEmailAndPassword --> $e")
                null
            }
        }
    }

    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        isUserCreated: MutableLiveData<Boolean?>
    ) = CoroutineScope(ioDispatcher).launch {
        try {
            auth.createUserWithEmailAndPassword(email, password).await().also {
                if (it.user != null) {
                    isUserCreated.postValue(true)
                } else {
                    isUserCreated.postValue(false)
                }
            }
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "createUserWithEmailAndPassword --> $e")
            isUserCreated.postValue(null)
        }
    }


    suspend fun addDebt(email: String, debtAmount: Long) {
        withContext(ioDispatcher) {
            try {
                residentRef.document(email)
                    .update("debt", FieldValue.increment(debtAmount))
                    .await()
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "updateDebtAmount ---> $e")
            }
        }
    }

    suspend fun deleteDebt(email: String, debtAmount: Long) {
        withContext(ioDispatcher) {
            try {
                residentRef.document(email)
                    .update("debt", FieldValue.increment(-debtAmount))
                    .await()
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "updateDebtAmount ---> $e")
            }
        }
    }

    suspend fun saveAdminIntoDB(
        admin: HashMap<String, Any>
    ) = CoroutineScope(ioDispatcher).launch {
        try {
            adminRef
                .document(admin["email"].toString())
                .set(admin)
                .await()
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "saveAdminIntDB --> $e")
        }
    }


    suspend fun updateFullNameForAdmin(fullName: String) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val currentUser = async {
                    auth.currentUser
                }
                val currentUserEmail = async {
                    currentUser.await()?.email
                }
                launch {
                    userProfileChangeRequest {
                        displayName = fullName
                    }.also {
                        currentUser.await()?.updateProfile(it)
                    }
                }
                launch {
                    currentUserEmail.await()?.let {
                        adminRef.document(it)
                            .update("fullName", fullName)
                            .await()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateFullNameForAdmin --> $e")
            }
        }.join()
    }

    suspend fun updateFullNameForResident(fullName: String) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val currentUser = async {
                    auth.currentUser
                }
                val currentUserEmail = async {
                    currentUser.await()?.email
                }
                launch {
                    userProfileChangeRequest {
                        displayName = fullName
                    }.also {
                        currentUser.await()?.updateProfile(it)
                    }
                }
                launch {
                    currentUserEmail.await()?.let {
                        residentRef.document(it)
                            .update("fullName", fullName)
                            .await()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateFullNameForResident --> $e")
            }
        }.join()
    }

    fun updatePhoneNumberForAdmin(phoneNumber: String, isSuccessful: MutableLiveData<Boolean?>)
            = CoroutineScope(ioDispatcher).launch {
        try {
            val email = currentUser?.email
            var newPhoneNumber = "+90"
            newPhoneNumber += phoneNumber
            email?.let {
                adminRef.document(it)
                    .update("phoneNumber", newPhoneNumber)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isSuccessful.postValue(true)
                        } else {
                            isSuccessful.postValue(false)
                        }
                    }
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "updatePhoneNumberForAdmin --> $e")
            isSuccessful.postValue(null)
        }
    }


    fun updatePhoneNumberForResident(phoneNumber: String, isSuccessful: MutableLiveData<Boolean?>)
            = CoroutineScope(ioDispatcher).launch {
        try {
            val email = currentUser?.email
            var newPhoneNumber = "+90"
            newPhoneNumber += phoneNumber
            email?.let {
                residentRef.document(it)
                    .update("phoneNumber", newPhoneNumber)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isSuccessful.postValue(true)
                        } else {
                            isSuccessful.postValue(false)
                        }
                    }
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "updatePhoneNumberForAdmin --> $e")
            isSuccessful.postValue(null)
        }
    }

    fun login(email: String, password: String, isSignedIn: MutableLiveData<Boolean?>)
        = CoroutineScope(ioDispatcher).launch {
        try {
            val a = FirebaseAuth.getInstance()
            a.signInWithEmailAndPassword(email, password).await().also {
                if (it.user != null) {
                    isSignedIn.postValue(true)
                } else {
                    println("null")
                    isSignedIn.postValue(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "login --> $e")
            isSignedIn.postValue(null)
        }
    }

    suspend fun isAdmin(
        email: String,
        isAdmin: MutableLiveData<Boolean?>
    ) = CoroutineScope(ioDispatcher).launch {
        try {
            adminRef.document(email)
                .get().await().also {
                    if (it["typeOfUser"] == "Yönetici") {
                        isAdmin.postValue(true)
                    }
                }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "isAdmin --> $e")
            isAdmin.postValue(false)
        }
    }.join()

    suspend fun isResident(
        email: String,
        isResident: MutableLiveData<Boolean?>
    ) = CoroutineScope(ioDispatcher).launch {
        try {
            residentRef.document(email)
                .get().await().also {
                    if (it["typeOfUser"] == "Sakin") {
                        isResident.postValue(true)
                    }
                }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "isAdmin --> $e")
            isResident.postValue(false)
        }
    }.join()

    fun signOut(isSignedOut: MutableLiveData<Boolean?>) = CoroutineScope(ioDispatcher).launch {
        try {
            auth.signOut().also {
                if (auth.currentUser == null)
                    isSignedOut.postValue(true)
                else
                    isSignedOut.postValue(false)
            }
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "signOut --> $e")
            isSignedOut.postValue(null)
        }
    }

    fun saveExpenditure(expenditure: Expenditure) {
        CoroutineScope(ioDispatcher).launch {
            try {
                adminRef.document(requireNotNull(currentUserEmail))
                    .collection("expenditures")
                    .document(expenditure.id)
                    .set(expenditure)
                    .await()
            } catch (e: Exception) {
                Log.e(TAG, "saveExpenditure --> $e")
            }
        }
    }

    fun updateExpenditureAmount(expenditure: Expenditure) {
        CoroutineScope(ioDispatcher).launch {
            try {
                currentUserEmail?.let {
                    adminRef.document(it)
                        .update("expendituresAmount", FieldValue.increment(expenditure.amount))
                        .await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateExpenditureAmount --> $e")
            }
        }
    }

    suspend fun saveResidentIntoDB(resident: HashMap<String, Any>) {
        withContext(ioDispatcher) {
            try {
                residentRef
                    .document(resident["email"].toString())
                    .set(resident)
                    .await()
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "saveResidentIntoDB --> $e")
            }
        }
    }
}