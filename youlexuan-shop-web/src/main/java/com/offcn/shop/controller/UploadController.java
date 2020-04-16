package com.offcn.shop.controller;

import com.offcn.entity.Result;
import com.offcn.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传图片的控制层
 */
@RestController
public class UploadController {

    @Value( "${FILE_SERVER_URL}" )
    private String serverUrl;

    /**
     * 上传方法
     */
    @RequestMapping("/upload")
    public Result upload(MultipartFile file){

        //全文件名
        String originalFilename = file.getOriginalFilename();

        String extName = originalFilename.substring( originalFilename.lastIndexOf( "." ) + 1 );

        //获取扩展名

        //调用common中的工具类上传
        try {
            FastDFSClient fastDFSClient = new FastDFSClient( "classpath:/config/fdfs_client.conf" );

            String path = fastDFSClient.uploadFile( file.getBytes(), extName );//二级制字节,扩展名  .返回相对路径
            //拼接全路径
            String url = serverUrl + path;
            return  new Result( true, url );
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result( false, "上传失败" );
        }
    }
}
