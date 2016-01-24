package com.example.recorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements OnClickListener {

    private Button start;
    private Button stop;
    private ListView listView;
    ShowRecorderAdpter showRecord;

    // 录音文件播放
    // 录音
    // 音频文件保存地址
    private MediaPlayer myPlayer;
    private MediaRecorder myRecorder = null;
    private String path;
    private File saveFilePath;
    // 所录音的文件名
    String[] listFile = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化控件
        InitView();

    }

    private void InitView() {
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        listView = (ListView) findViewById(R.id.list);

        myPlayer = new MediaPlayer();
        showRecord = new ShowRecorderAdpter();

        //如果手机有sd卡
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            try {
                path = Environment.getExternalStorageDirectory()
                        .getCanonicalPath().toString()
                        + "/MyRecorders";
                File files = new File(path);
                if (!files.exists()) {
                    //如果有没有文件夹就创建文件夹
                    files.mkdir();
                }
                listFile = files.list();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        listView.setAdapter(showRecord);


    }


    //由于在item中涉及到了控件的点击效果，所以采用BaseAdapter
    class ShowRecorderAdpter extends BaseAdapter {

        @Override
        public int getCount() {
            return listFile.length;
        }

        @Override
        public Object getItem(int arg0) {
            return arg0;
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;

        }

        @Override
        public View getView(final int postion, View arg1, ViewGroup arg2) {
            View views = LayoutInflater.from(MainActivity.this).inflate(
                    R.layout.list_item, null);
            LinearLayout parent = (LinearLayout) views.findViewById(R.id.list_parent);
            TextView filename = (TextView) views.findViewById(R.id.show_file_name);
            Button plays = (Button) views.findViewById(R.id.bt_list_play);
            Button stop = (Button) views.findViewById(R.id.bt_list_stop);

            //在textview中显示的时候把“.amr”去掉
            filename.setText(listFile[postion].substring(0, listFile[postion].length() - 4));
            parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog aler = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("确定删除该录音？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog
                                        , int which) {
                                    File file = new File(path + "/" + listFile[postion]);
                                    file.delete();
                                    // 在删除文件后刷新文件名列表
                                    File files = new File(path);
                                    listFile = files.list();

                                    // 当文件被删除刷新ListView
                                    showRecord.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create();
                    //设置不允许点击提示框之外的区域
                    aler.setCanceledOnTouchOutside(false);
                    aler.show();
                    return false;
                }
            });
            // 播放录音
            plays.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //确认不是在录音的过程中播放
                    if (myRecorder == null) {
                        try {
                            myPlayer.reset();
                            myPlayer.setDataSource(path + "/" + listFile[postion]);
                            if (!myPlayer.isPlaying()) {
                                myPlayer.prepare();
                                myPlayer.start();
                            } else {
                                myPlayer.pause();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "请不要再录音的过程中播放！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // 停止播放
            stop.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (myRecorder == null && myPlayer.isPlaying()) {
                        myPlayer.stop();
                    }
                }
            });
            return views;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                final EditText filename = new EditText(this);
                AlertDialog aler = new Builder(this)
                        .setTitle("请输入要保存的文件名")
                        .setView(filename)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        String text = filename.getText().toString();
                                        //如果文件名为空则跳出提示信息
                                        if (text.equals("")) {
                                            Toast.makeText(MainActivity.this,
                                                    "请不要输入空的文件名!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            //开启录音
                                            RecorderStart(text);

                                            start.setText("正在录音中。。");
                                            start.setEnabled(false);
                                            stop.setEnabled(true);
                                            // 在增添文件后刷新文件名列表
                                            File files = new File(path);
                                            listFile = files.list();
                                            // 当文件增加刷新ListView
                                            showRecord.notifyDataSetChanged();
                                        }
                                    }
                                })
                        .setNegativeButton("取消",null)
                        .create();
                //设置不允许点击提示框之外的区域
                aler.setCanceledOnTouchOutside(false);
                aler.show();
                break;
            case R.id.stop:
                myRecorder.stop();
                myRecorder.release();
                myRecorder = null;
                // 判断是否保存 如果不保存则删除
                aler = new AlertDialog.Builder(this)
                        .setTitle("是否保存该录音")
                        .setPositiveButton("确定", null)
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        saveFilePath.delete();
                                        // 在删除文件后刷新文件名列表
                                        File files = new File(path);
                                        listFile = files.list();

                                        // 当文件被删除刷新ListView
                                        showRecord.notifyDataSetChanged();
                                    }
                                }).create();
                //设置不允许点击提示框之外的区域
                aler.setCanceledOnTouchOutside(false);
                aler.show();

                start.setText("录音");
                start.setEnabled(true);
                stop.setEnabled(false);
            default:
                break;
        }

    }

    private void RecorderStart(String text) {
        try {
            myRecorder = new MediaRecorder();
            // 从麦克风源进行录音
            myRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            // 设置输出格式
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            // 设置编码格式
            myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            String paths = path + "/" + text + ".amr";
            saveFilePath = new File(paths);
            myRecorder.setOutputFile(saveFilePath.getAbsolutePath());
            myRecorder.prepare();
            // 开始录音
            myRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 如果myPlayer正在播放，那么就停止播放，并且释放资源
        if (myPlayer.isPlaying()) {
            myPlayer.stop();
            myPlayer.release();
        }
        //如果myRecorder有内容（代表正在录音），那么就直接释放资源
        if(myRecorder!=null) {
            myRecorder.release();
            myPlayer.release();
        }
    }

}  