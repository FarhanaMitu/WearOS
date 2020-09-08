package com.example.roomwordsample;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class WordViewModel extends AndroidViewModel {

    private WordRepository mRepository;

    private LiveData<List<Word>> mAllWords;

    public WordViewModel (Application application) {
        super(application);
        Log.i("tag", "ViewModel Constructor");
        mRepository = new WordRepository(application);
        mAllWords = mRepository.getAllWords();
    }

    LiveData<List<Word>> getAllWords() {
        Log.i("tag", "ViewModel getALlWords");
        return mAllWords;
    }

    public void insert(Word word) {
        Log.i("tag", "ViewModel insert");
        mRepository.insert(word);
    }
}
