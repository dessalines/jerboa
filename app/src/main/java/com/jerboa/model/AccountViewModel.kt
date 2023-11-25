package com.jerboa.model

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jerboa.api.API
import com.jerboa.api.DEFAULT_INSTANCE
import com.jerboa.db.entity.Account
import com.jerboa.db.entity.AnonAccount
import com.jerboa.db.entity.isAnon
import com.jerboa.db.entity.isReady
import com.jerboa.db.repository.AccountRepository
import com.jerboa.feat.AccountVerificationState
import com.jerboa.jerboaApplication
import kotlinx.coroutines.launch

@Stable
class AccountViewModel(private val repository: AccountRepository) : ViewModel() {
    val currentAccount = repository.currentAccount
    val allAccounts = repository.allAccounts

    fun insert(account: Account) =
        viewModelScope.launch {
            repository.insert(account)
        }

    fun removeCurrent() =
        viewModelScope.launch {
            repository.removeCurrent()
        }

    fun setCurrent(account: Account) =
        viewModelScope.launch {
            repository.setCurrent(account.id)
            API.setLemmyInstance(account.instance)
        }

    // Be careful when setting the verification state,
    // when the account is not ready and used, it triggers a verification check
    // which can update the siteviewmodel.siteRes, if you have logic invalidating the
    // account based on siteRes changes make sure it doesn't cause a loop
    fun setVerificationState(
        accountId: Int,
        state: Int,
    ) = viewModelScope.launch {
        repository.setVerificationState(accountId, state)
    }

    fun invalidateAccount(account: Account) {
        if (!account.isAnon() && account.isReady()) {
            setVerificationState(account.id, AccountVerificationState.NOT_CHECKED.ordinal)
        }
    }

    fun delete(account: Account) =
        viewModelScope.launch {
            repository.delete(account)
        }

    // TODO ON DONE only change

    fun deleteAccountAndSwapCurrent(
        account: Account,
        swapToAnon: Boolean = false,
    ) = viewModelScope.launch {
        if (account.isAnon()) return@launch

        repository.delete(account)

        val accounts = repository.allAccounts.value
        val nextAcc = accounts?.firstOrNull { it.id != account.id }

        if (!swapToAnon && nextAcc != null) {
            setCurrent(nextAcc)
        } else {
            API.setLemmyInstance(DEFAULT_INSTANCE)
        }
    }
}

object AccountViewModelFactory {
    val Factory =
        viewModelFactory {
            initializer {
                AccountViewModel(jerboaApplication().container.accountRepository)
            }
        }
}
