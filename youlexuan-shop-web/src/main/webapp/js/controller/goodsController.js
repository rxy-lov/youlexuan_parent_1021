 //控制层 
app.controller('goodsController' ,function($scope,$controller  ,goodsService, uploadSevice, itemCatService, typeTemplateService){

	$controller('baseController',{$scope:$scope});

    //读取列表数据绑定到表单中
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}

    /**
	 * 定义前端组合实体对象
     */
    $scope.entity = {goods:{isEnableSpec:0},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]};  //{}--json对象  []--数组或者集合对象

	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{

			//把富文本编辑器中的值赋值给entity
			$scope.entity.goodsDesc.introduction = editor.html();

			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){

					//提示成功
					alert("保存成功")
                    $scope.entity = {goods:{isEnableSpec:0},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]};
					//清空编辑器
                    editor.html('');
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    $scope.image_entity = {};

	//上传图片
    $scope.upload=function () {
        uploadSevice.upload().success(
            function (response) {
                if(response.success){
                    //上传成功,双向绑定回显图片
                    $scope.image_entity.url=response.message;
                }
            }
        );
    }

    //图片保存到组合实体对象中的spu_desc中的图片集合
    $scope.add_image_entity=function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);//image_entity包含color,url
    }

	//删除spu_desc中的图片集合移除,同规格删除规格项一致
	$scope.remove_image_entity=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //1.查询一级分类
	$scope.selectItemCat1List=function () {
        itemCatService.findByParentId(0).success(
        	function (response) {
				$scope.itemCat1List = response;
            }
		);
    }
    
    //2.分类的二级下拉,观察一级下拉的id变化(新id，旧id)就查询二级
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {//启动一个观察者,实时盯着.分2步骤(有$watch就会开始盯着看.直到看到的变化了才触发)
		if(newValue){//根据新值去查询下拉礼包
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List = response;
                }
            );
		}
    });

    //3.分类的三级下拉,观察二级下拉的id变化(新id，旧id)就查询三级
    $scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
        if(newValue){//根据新值去查询下拉礼包
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response;
                }
            );
        }
    });

    //4.模板,观察三级下拉的id变化(新id，旧id)就查询显示模板id
    $scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {
        if(newValue){//根据新值去查询下拉礼包

            //根据模板id查询单个分类对象
            itemCatService.findOne(newValue).success(
            	function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeId;
                }
			);
        }
    });
    
    $scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
		if(newValue){
			//根据模板id查询模板
            typeTemplateService.findOne(newValue).success(
            	function (response) {
                    $scope.typeTemplate = response;//模板对象

                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds)//字符串转json品牌集合
                    $scope.typeTemplate.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems)//字符串转json自定义属性集合
                    //$scope.typeTemplate.specIds = JSON.parse($scope.typeTemplate.specIds)//字符串转json自定义属性集合
                }
			);
            //没法同上直接转json是因为没有规格项,所以新写如下方法,查询规格项
            typeTemplateService.findSpecList(newValue).success(
                function (response) {
                    $scope.specList = response;//模板对象

                }
            );

		}
    })

    /**
     * 根据复选框勾选取消来增加或删除组合实体对象中的spu_desc中的规格项集合
     */
    $scope.updateSpecAttribute=function ($event,name,value) {

            //调用公共方法判断name在集合中是否已经存在了
            var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);

            if(object != null){//存在
                if($event.target.checked){//添加
                    object.attributeValue.push(value);
                }else{//删除
                    object.attributeValue.splice(object.attributeValue.indexOf(value), 1);

                    //要考虑object.attributeValue集合如果长度变成0，所在对象都已经没有存在的意义了
                    if(object.attributeValue.length == 0){
                        $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                    }
                }

            }else{//不存在,新建一个自定义的规格对象
                $scope.entity.goodsDesc.specificationItems.push({'attributeName':name, 'attributeValue':[value]});
            }
        }


    /**
	 * 根据所选规格项生成sku列表
	 * 勾选规格项本质就是加sku列表中的spec:{},sku的数量  排列组合
     */
    $scope.createItemList=function () {
		//1.初始化母本
		$scope.entity.itemList=[{spec:{},price:0,num:9999,status:'0',isDefault:'0'}];

		//2.起个别名$scope.entity.goodsDesc.specificationItems   list
		var items = $scope.entity.goodsDesc.specificationItems;

		for(var i=0;i<items.length;i++){
			//调用加工itemList后返回加工好的itemList.加工谁呢?加工每一个item中的spec:{},用什么加工呢
            $scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName, items[i].attributeValue );
		}
    }

    /**
	 * 给定的集合中,循环加工(没有新加,有的插入)spec:{},最终返回加工后的集合$scope.entity.itemList
     */
    addColumn=function (list,attributeName,attributeValue) {

    	//a.初始化一个空的新集合
     	var newList=[];
     	//b.
		for(var i=0;i<list.length;i++){//如果是第一次进来,只会循环一次.因为其中只有一个初始化的集合
			//加工的旧行
			var oldRow = list[i];
			for(var j=0;j<attributeValue.length;j++){
				var newRow = JSON.parse(JSON.stringify(oldRow));//有问题,先转换成字符串,再将字符串转换成json--深层拷贝--2个地址
                newRow.spec[attributeName]= attributeValue[j];

                newList.push(newRow);
			}
		}
		return newList;
    }

   //定义商品状态集合
	$scope.status=['未审核','已审核','审核未通过','关闭'];

    //定义商品分类,数据库查询
	$scope.itemCatList=[];
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(
			function (response) {
				//循环封装返回结果到$scope.itemCatList
				for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;//$scope.itemCatList[1]='图书、音像、电子书刊'
				}
            }
		);
    }
});	