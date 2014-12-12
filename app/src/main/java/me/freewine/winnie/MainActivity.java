package me.freewine.winnie;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private TextView cntView;
        private SharedPreferences sharedPref;

        private SoundPool soundPool;
        private HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            //实现soundPool对象, 参数分别对应声音池数量，AudioManager.STREAM_MUSIC 和 0
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
            /* 加载文件, 执行该方法返回的是该音频文件在音效池中的位置，用HashMap保存 */
            map.put(1, soundPool.load(getActivity(), R.raw.plus, 1));
            map.put(2, soundPool.load(getActivity(), R.raw.minus, 1));
            map.put(3, soundPool.load(getActivity(), R.raw.over, 1));

            sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            int cnt = sharedPref.getInt(getString(R.string.saved_cnt), 150);

            cntView = (TextView) rootView.findViewById(R.id.nowCount);
            cntView.setText(String.valueOf(cnt));

            Button plusBtn = (Button) rootView.findViewById(R.id.plus50);
            plusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int cnt = Integer.valueOf(cntView.getText().toString());
                    cnt += 50;
                    cntView.setTextColor(Color.WHITE);
                    cntView.setText(String.valueOf(cnt));

                    //播放声音
                    soundPool.play(map.get(1), 1, 1, 1, 0, 1);

                    //将操作记录保存到数据库
                    InsertToDB(cnt, 50);

                    //保存计数
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.saved_cnt), cnt);
                    editor.commit();
                }
            });

            Button minusBtn = (Button) rootView.findViewById(R.id.minus50);
            minusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int cnt = Integer.valueOf(cntView.getText().toString());
                    cnt -= 50;
                    if (cnt < 0) {
                        cnt = 0;
                        cntView.setTextColor(getResources().getColor(R.color.pink_a400));

                        //播放声音
                        soundPool.play(map.get(3), 1, 1, 1, 0, 1);
                    } else {
                        cntView.setText(String.valueOf(cnt));

                        //播放声音
                        //播放声音
                        soundPool.play(map.get(2), 1, 1, 1, 0, 1);
                    }

                    //将操作记录保存到数据库
                    InsertToDB(cnt, -50);

                    //保存计数
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.saved_cnt), cnt);
                    editor.commit();
                }
            });

            ImageButton historyBtn = (ImageButton) rootView.findViewById(R.id.BtnHistory);
            historyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(getActivity(), HistoryActivity.class);
                    startActivity(it);
                }
            });
            return rootView;
        }

        @Override
        public void onDestroyView() {
            // 销毁的时候释放SoundPool资源
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }
            super.onDestroyView();
        }

        //添加数据库
        void InsertToDB(int cnt, int change) {
            // Creates another ContentValues for storing date information
            ContentValues values = new ContentValues();
            // Adds the URL's last modified date to the ContentValues
            values.put(HistoryContract.COLUMN_NAME_CNT, cnt);
            values.put(HistoryContract.COLUMN_NAME_CHANGE, change);
            values.put(HistoryContract.COLUMN_NAME_TIME, System.currentTimeMillis());

            getActivity().getContentResolver().insert(HistoryContract.HISTORY_TABLE_CONTENTURI, values);
        }
    }
}
