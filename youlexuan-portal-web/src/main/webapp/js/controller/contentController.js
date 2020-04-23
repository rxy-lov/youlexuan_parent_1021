 //控制层 
app.controller('contentController' ,function($scope,contentService){

	//楼层广告集合
    $scope.contentList=[];

    //读取列表数据绑定到表单中
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
                $scope.contentList[categoryId]=response;
			}			
		);
	}    

});	