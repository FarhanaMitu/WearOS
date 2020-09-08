package com.example.roomwordsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import java.util.List;

public class MainActivity extends FragmentActivity {

    //    private TextView mTextView;
    private WordViewModel mWordViewModel;
    private WordListAdapter mAdapter;
    private WearableRecyclerView mRecyclerView;
    private Button mBtn;

    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mBtn = findViewById(R.id.btn_save);

        Log.i("tag", "Before ViewModel initialize");
//        mWordViewModel = ViewModelProviders.of(this).get(WordViewModel.class);
        mWordViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(WordViewModel.class);
        Log.i("tag", "After ViewModel initialize");

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setEdgeItemsCenteringEnabled(true);
        mRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        mAdapter = new WordListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mWordViewModel.getAllWords().observe((LifecycleOwner) this, new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                // Update the cached copy of the words in the adapter.
                Log.i("tag", "ViewModel onChanged method");
                mAdapter.setWords(words);
            }
        });

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewActivity.class);
                startActivityForResult(intent, NEW_WORD_ACTIVITY_REQUEST_CODE);
            }
        });

//        Log.i("tag", "Apple insert");
//        mWordViewModel.insert(new Word("Apple"));
//        mWordViewModel.insert(new Word("Mango"));
//        Log.i("tag", "Mango insert");
//        mWordViewModel.insert(new Word("Apple1"));
//        mWordViewModel.insert(new Word("Mango1"));

//        List<Word> wordItems = new ArrayList<>();
//        wordItems.add(new Word("Apple"));
//        wordItems.add(new Word("Mango"));
//        wordItems.add(new Word("Orange"));
//        wordItems.add(new Word("Banana"));
//        wordItems.add(new Word("Apple 1"));
//        wordItems.add(new Word("Mango 1"));
//        wordItems.add(new Word("Orange 1"));
//        wordItems.add(new Word("Banana 1"));

//        mAdapter = new WordListAdapter(this);
//        mAdapter.setWords(wordItems);
//        mRecyclerView.setAdapter(adapter);

        // Enables Always-on
//        setAmbientEnabled();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_WORD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Word word = new Word(data.getStringExtra(NewActivity.EXTRA_REPLY));
            mWordViewModel.insert(word);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.empty_not_saved,
                    Toast.LENGTH_LONG).show();
        }
    }

}