package com.sky.controller.admin;/**
 * ClassName: CommonController
 * Package: com.sky.controller.admin
 */

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-09-28 23:11
 **/
@RestController
@RequestMapping("admin/common")
@Slf4j
public class CommonController {
    //本地文件上传
//    @PostMapping("upload")
//    public Result<String> uploadFile(@RequestParam("file") MultipartFile multipartFile){
//        StringBuilder rootPath=new StringBuilder();
//        rootPath.append(System.getProperty("user.dir"));
//        rootPath.append("\\sky-server\\src\\main\\resources\\public\\imgs\\addpicture\\");
//        String fileName=multipartFile.getOriginalFilename();
//        String filePath=rootPath.append(fileName).toString();
//        File fileResult=new File(filePath);
//        if (fileResult.exists()){
//            return Result.error("上传失败,图片已经存在,请更改图片名后重试");
//        }else {
//            try {
//                multipartFile.transferTo(fileResult);
//                return Result.success("public/imgs/addpicture/"+fileName);   //  "public/imgs/addpicture/"+fileName        filePath
//            } catch (IOException e) {
//                e.printStackTrace();
//                return Result.error("图片上传时出现异常");
//            }
//        }
//    }
    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("upload")
    public Result<String> upload(@RequestParam("file") MultipartFile multipartFile)  {
        String originalFilename = multipartFile.getOriginalFilename();
        String backName = originalFilename.substring(originalFilename.lastIndexOf("."));


        String url = null;
        try {
            url = aliOssUtil.upload(multipartFile.getBytes(), UUID.randomUUID().toString() + backName);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败",e);
            return null;
        }



    }
}
