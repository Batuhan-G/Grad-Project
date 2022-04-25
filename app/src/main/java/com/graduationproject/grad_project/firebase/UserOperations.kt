package com.graduationproject.grad_project.firebase

import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.graduationproject.grad_project.model.Expenditure
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

object UserOperations: FirebaseConstants() {

    private const val TAG = "UserOperations"
    private var isAdmin = false
    private var isResident = false

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

    fun acceptResident() {

    }

    fun reAuthenticateUser(email: String, password: String) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val credential = EmailAuthProvider.getCredential(
                    email, password
                )
                val currentUser = currentUser!!
                currentUser.reauthenticate(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "reAuthenticateUser --> ReAuthentication is successful!")
                    }
                }.await()
            } catch (e: Exception) {
                Log.e(TAG, "reAuthenticateUser --> $e")
            }
        }
    }

    suspend fun getAdminInSpecificSite(residentInfo: DocumentSnapshot): QuerySnapshot? {
        return try {
            val admin = adminRef.whereEqualTo("city", residentInfo["city"])
                .whereEqualTo("district", residentInfo["district"])
                .whereEqualTo("siteName", residentInfo["siteName"])
                .get().await()
            admin
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getAdminInSpecificSite ---> $e")
            null
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

    suspend fun addDebt(email: String, debtAmount: Double) {
        withContext(ioDispatcher) {
            try {
                residentRef.document(email)
                    .update("debt", FieldValue.increment(debtAmount.toLong()))
                    .await()
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "updateDebtAmount ---> $e")
            }
        }
    }

    suspend fun deleteDebt(email: String, debtAmount: Double) {
        withContext(ioDispatcher) {
            try {
                residentRef.document(email)
                    .update("debt", FieldValue.increment(-debtAmount.toLong()))
                    .await()
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "updateDebtAmount ---> $e")
            }
        }
    }

    suspend fun saveAdminIntoDB(
        admin: HashMap<String, Any>,
        scope: CoroutineDispatcher = Dispatchers.IO
    ) {
        withContext(scope) {
            try {
                adminRef
                    .document(admin["email"].toString())
                    .set(admin)
                    .await()
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "saveAdminIntDB --> $e")
            }
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

    fun updateEmailForAdmin(newEmail: String) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val currentUser = async {
                    auth.currentUser
                }
                val currentUserEmail = async {
                    currentUser.await()?.email
                }
                launch {
                    currentUser.await()?.updateEmail(newEmail)?.await()
                }
                launch {
                    currentUserEmail.await()?.let {
                        adminRef.document(it)
                            .update("email", newEmail).await()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateEmailInfo --> $e")
            }
        }
    }

    fun updateEmailForResident(email: String) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val currentUser = async {
                    auth.currentUser
                }
                val currentUserEmail = async {
                    currentUser.await()?.email
                }
                launch {
                    currentUser.await()?.updateEmail(email)?.await()
                }
                launch {
                    currentUserEmail.await()?.let {
                        residentRef.document(it)
                            .update("email", email).await()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateEmailInfo --> $e")
            }
        }
    }



    fun updatePassword(password: String, view: View) {
        CoroutineScope(ioDispatcher).launch {
            try {
                currentUser?.updatePassword(password)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "updatePassword --> Updating password is SUCCESSFUL!")
                        Snackbar.make(
                            view,
                            "Şifre değiştirme başarılı!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }?.await()
            } catch (e: Exception) {
                Log.e(TAG, "updatePassword ---> $e")
                Snackbar.make(
                    view,
                    e.toString(),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    fun updatePhoneNumberForAdmin(phoneNumber: String) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val email = currentUser?.email
                email?.let {
                    adminRef.document(it)
                        .update("phoneNumber", phoneNumber)
                        .await()
                }
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "updatePhoneNumberForAdmin --> $e")
            }
        }
    }

    fun updatePhoneNumberForResident(phoneNumber: String) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val email = currentUser?.email
                email?.let {
                    residentRef.document(it)
                        .update("phoneNumber", phoneNumber)
                        .await()
                }
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "updatePhoneNumberForResident ---> $e")
            }
        }
    }

    suspend fun loginWithEmailAndPassword(
        email: String,
        password: String,
        view: View?,
        scope: CoroutineDispatcher = Dispatchers.IO
    ): AuthResult? {
        return withContext(scope) {
            try {
                var data: AuthResult? = null
                if (auth.currentUser == null) {
                    try {
                        data = auth.signInWithEmailAndPassword(email, password).await().also {
                            Log.d(TAG, "loginWithEmailAndPassword --> $it")
                        }
                    } catch (e: FirebaseAuthException) {
                        view?.let {
                            Snackbar.make(
                                it,
                                e.message.toString(),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                    }
                }
                data
            } catch (e: Exception) {
                Log.e(TAG, "loginWithEmailAndPassword -->  $e")
                null
            }
        }
    }

    fun signOut() = auth.signOut()

    private fun isSignedIn() = auth.currentUser != null

    fun takeTheUserEmailIfSignedIn(): String? {
        if (isSignedIn()) {
            return auth.currentUser?.email.toString()
        }
        return null
    }

    private suspend fun isAdmin(email: String, scope: CoroutineDispatcher = Dispatchers.IO): Boolean {
        return withContext(scope) {
            try {
                val admin = async {
                    getAdmin(email)
                }
                val type = admin.await()?.get("typeOfUser")
                if (type == "Yönetici") isAdmin = true
                isResident = false
                isAdmin
            } catch (e: Exception) {
                Log.e(TAG, "isAdmin ---> $e")
                isAdmin
            }
        }
    }

    private suspend fun isResident(email: String, scope: CoroutineDispatcher = Dispatchers.IO): Boolean {
        return withContext(scope) {
            try {
                val resident = async {
                    getResident(email)
                }
                val type = resident.await()?.get("typeOfUser")
                if (type == "Sakin") isResident = true
                isAdmin = false
                isResident
            } catch (e: Exception) {
                Log.e(TAG, "isResident --> $e")
                isResident
            }
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
                        .update("expendituresAmount", FieldValue.increment(expenditure.amount.toLong()))
                        .await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateExpenditureAmount --> $e")
            }
        }
    }

    suspend fun takeTheUserType(email: String, scope: CoroutineDispatcher = Dispatchers.IO): String {
        return withContext(scope) {
            val isAdmin = async {
                isAdmin(email)
            }
            val isResident = async {
                isResident(email)
            }
            var userType = ""
            if (isAdmin.await()) userType = "Yönetici"
            if (isResident.await()) userType = "Sakin"
            userType
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