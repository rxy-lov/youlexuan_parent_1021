app.controller('indexController', function ($scope, loginService) {//依赖注入服务

    $scope.showName=function () {
        loginService.showName().success(
            function (response) {
                $scope.name = response.loginName;
            }
        );
    }

});