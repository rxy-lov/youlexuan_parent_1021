app.service('uploadSevice',function ($http) {
    
    this.upload=function () {

        //封装一个多媒体表单二进制对象
        var formData = new FormData();
        formData.append("file" ,file.files[0]);
        return $http({

            //url
            url:"../upload.do",
            //method
            method: 'post',
            //data
            data: formData,
            //设置头
            headers:{
                'Content-Type':undefined//如果什么不配置,那json格式。我们要的不是json。是多媒体
            },
            transformRequest: angular.identity

        });
    }
})