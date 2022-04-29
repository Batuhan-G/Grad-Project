package com.graduationproject.grad_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.graduationproject.grad_project.firebase.ExpendituresOperations
import com.graduationproject.grad_project.model.Expenditure

class ExpendituresViewModel: ViewModel() {

    private var _expenditures = MutableLiveData<MutableList<Expenditure?>>()
    val expenditures: LiveData<MutableList<Expenditure?>> get() = _expenditures

    fun retrieveAllExpendituresWithSnapshot() {
        ExpendituresOperations.retrieveExpendituresForAdmin(_expenditures)
    }

}