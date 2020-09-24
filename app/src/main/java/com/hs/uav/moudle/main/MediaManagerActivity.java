package com.hs.uav.moudle.main;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.MAVLink.DLink.msg_mediafile_information;
import com.MAVLink.DLink.msg_mediafile_request;
import com.MAVLink.DLink.msg_mediafile_request_list;
import com.MAVLink.enums.CAMERA_STORAGE_LOCATION;
import com.MAVLink.enums.MEDIAFILE_REQUEST_TYPE;
import com.google.gson.Gson;
import com.hs.uav.R;
import com.hs.uav.common.app.AppController;
import com.hs.uav.common.app.AppViewModelFactory;
import com.hs.uav.common.entity.FileBean;
import com.hs.uav.databinding.ActivityMediaManagerLayoutBinding;
import com.hs.uav.logic.data.db.CommonDaoUtils;
import com.hs.uav.logic.data.db.DaoUtilsStore;
import com.hs.uav.logic.listener.DownloadFileLinster;
import com.hs.uav.logic.mqtt.MqttService;
import com.hs.uav.moudle.MainViewModel;
import com.hs.uav.moudle.main.adapter.FileGridAdapter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.BR;
import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.base.MySystemTipsDialog;
import me.goldze.mvvmhabit.utils.KLog;

/***
 * 相機拍照管理UI
 * @author tony.liu
 */
public class MediaManagerActivity extends BaseActivity<ActivityMediaManagerLayoutBinding, MainViewModel> implements DownloadFileLinster {
    CommonDaoUtils<FileBean> fileBeanCommonDaoUtils;
    FileGridAdapter fileGridAdapter;
    FileBean currentFileBean;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_media_manager_layout;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public MainViewModel initViewModel() {
        AppController.getInstance().regDownloadFileLinster(this);
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(MainViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        DaoUtilsStore _Store = DaoUtilsStore.getInstance();
        fileBeanCommonDaoUtils = _Store.getFileBeanDaoUtils();
        //获取云端文件数量
        msg_mediafile_request_list requestFileList = new msg_mediafile_request_list();
        requestFileList.storage_location = CAMERA_STORAGE_LOCATION.INTERNAL_STORAGE;
        MqttService.publish(requestFileList.pack().encodePacket());
        getLocalFileDB();
    }

    private void getLocalFileDB() {
        List<FileBean> fileBeans = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            fileBeans.add(new FileBean());
        }
        fileGridAdapter = new FileGridAdapter(this, fileBeans);
        binding.gridView.setAdapter(fileGridAdapter);

        fileGridAdapter.fileBeanSingleLiveEvent.observe(this, fileBean -> {
            currentFileBean = fileBean;
            if (binding.showBigView.getVisibility() == View.GONE) {
                binding.showBigView.setVisibility(View.VISIBLE);
                binding.deleteIv.setVisibility(View.VISIBLE);
                binding.downloadIv.setVisibility(View.VISIBLE);
            } else {
                binding.showBigView.setVisibility(View.GONE);
                binding.deleteIv.setVisibility(View.GONE);
                binding.downloadIv.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();

        binding.backIv.setOnClickListener(view -> {
            finish();
        });
        //下载
        binding.downloadIv.setOnClickListener(view -> {
            //下载单个文件
            //协议说明：其中index为所请求的文件的序号，storage_location为存储位置，
            // request_type为请求类型，可选取值见枚举变量MEDIAFILE_REQUEST_TYPE:
            msg_mediafile_request msg_mediafile_request = new msg_mediafile_request();
            msg_mediafile_request.index = currentFileBean.getIndex();
            msg_mediafile_request.storage_location = CAMERA_STORAGE_LOCATION.SDCARD;
            msg_mediafile_request.request_type = MEDIAFILE_REQUEST_TYPE.PREVIEW;
            MqttService.publish(msg_mediafile_request.pack().encodePacket());
        });
        //删除
        binding.deleteIv.setOnClickListener(view -> {
            MySystemTipsDialog dialog = new MySystemTipsDialog(this);
            dialog.setItem("Are you sure want to delete?", new MySystemTipsDialog.OperaterListener() {
                @Override
                public void operater() {

                }
            });
            dialog.show();
        });
    }

    @Override
    public void getFileCount(int count) {
        fileBeanCommonDaoUtils.deleteAll();
        List<FileBean> fileBeans = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FileBean fileBean = new FileBean();
            fileBean.setFileName("unkown");
            fileBean.setFilePath("unkown");
            fileBean.setIndex(i);
            fileBean.setFileType(0);
            fileBean.setTotalLength(0);
            fileBean.setCurrentProgress(0);
            fileBeans.add(fileBean);
        }
        fileBeanCommonDaoUtils.insertMulti(fileBeans);
        msg_mediafile_request mediaFileRequest = new msg_mediafile_request();
        mediaFileRequest.index = 0;
        mediaFileRequest.request_type = MEDIAFILE_REQUEST_TYPE.INFORMATION;
        mediaFileRequest.storage_location = CAMERA_STORAGE_LOCATION.INTERNAL_STORAGE;
        MqttService.publish(mediaFileRequest.pack().encodePacket());
    }

    @Override
    public void saveFileLocal(msg_mediafile_information msg_mediafile_information) {
        FileBean fileBean = new FileBean();
        try {
            fileBean.setFileName(new String(msg_mediafile_information.file_name,"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        fileBean.setFilePath("unkown");
        fileBean.setIndex(msg_mediafile_information.index);
        fileBean.setFileType(msg_mediafile_information.file_type);
        fileBean.setTotalLength(msg_mediafile_information.file_size);
        fileBean.setCurrentProgress(0);
        fileBeanCommonDaoUtils.insert(fileBean);
        KLog.e(TAG,new Gson().toJson(fileBean));
        msg_mediafile_request mediaFileRequest = new msg_mediafile_request();
        mediaFileRequest.index = msg_mediafile_information.index;
        mediaFileRequest.storage_location = CAMERA_STORAGE_LOCATION.INTERNAL_STORAGE;
        mediaFileRequest.request_type = MEDIAFILE_REQUEST_TYPE.PREVIEW;
        MqttService.publish(mediaFileRequest.pack().encodePacket());
    }
}
