package com.skp3214.financepal.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleSignInAuth(
    private val context: Context,
) {
    private val credentialManager=CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun signIn():Boolean{
        if(isLoggedIn()){
            return true
        }
        try {
            val result=buildCredentialRequest()
            return handleSignIn(result)
        }
        catch (e:Exception){
            e.printStackTrace()
            if(e is CancellationException){
                throw e
            }
        }
        return false
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential=result.credential
        if(credential is CustomCredential && credential.type==GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
            try {
                val tokenCredential=GoogleIdTokenCredential.createFrom(credential.data)

                val authCredential=GoogleAuthProvider.getCredential(tokenCredential.idToken,null)

                val authResult=firebaseAuth.signInWithCredential(authCredential).await()

                return authResult.user!=null
            }
            catch (e:GoogleIdTokenParsingException){
                e.printStackTrace()
                return false
            }
        }
        else{
            return false
        }
    }

    private suspend fun buildCredentialRequest():GetCredentialResponse{
        val result=GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        "12357921490-1ovdt6pbgkdt9fue9k95iaork84cftbd.apps.googleusercontent.com"
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()
        return credentialManager.getCredential(
            request=result,
            context=context
        )
    }

    suspend fun signOut(){
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
    }
}