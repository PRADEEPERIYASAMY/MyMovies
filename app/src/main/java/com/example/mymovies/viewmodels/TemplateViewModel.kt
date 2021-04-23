package com.example.mymovies.viewmodels

/*
class FirebaseDbViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val repository: FirebaseDbRepository
) : BaseViewModel<FirebaseDbAction>() {

    private var firebaseDbMutableLiveData = MutableLiveData<FirebaseDbModels>()
    val firebaseDbLiveData: LiveData<FirebaseDbModels>
        get() = firebaseDbMutableLiveData

    override fun doAction(action: FirebaseDbAction): Any =
        when (action) {
            is FirebaseDbAction.FetchFirebaseDbData -> fetchAlphabetImagesAndName()
        }

    private fun fetchAlphabetImagesAndName() = launch {
        when (val result = repository.getAlphabetNamesAndImages()) {
            is Result.Value -> {
                firebaseDbMutableLiveData.postValue(result.value)
            }
        }
    }
}*/
