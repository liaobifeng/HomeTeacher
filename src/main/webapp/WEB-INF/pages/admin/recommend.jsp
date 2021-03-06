<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>AdminLTE | Dashboard</title>
    <meta content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no' name='viewport'>
    <!-- bootstrap 3.0.2 -->
    <link href="/public/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
    <!-- font Awesome -->
    <link href="/public/css/font-awesome.min.css" rel="stylesheet" type="text/css" />
    <!-- Ionicons -->
    <link href="/public/css/ionicons.min.css" rel="stylesheet" type="text/css" />
    <!-- Theme style -->
    <link href="/public/css/AdminLTE.css" rel="stylesheet" type="text/css" />
    <link href="/public/css/styles.css" rel="stylesheet" type="text/css" />

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
</head>
<body class="skin-blue">
<!-- header logo: style can be found in header.less -->
<header class="header">
    <nav class="navbar navbar-static-top" role="navigation">
        <div class="navbar-left">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <div class="navbar-brand">
                    家教你好
                </div>
            </div>
        </div>
        <div class="navbar-right">
            <ul class="nav navbar-nav">
                <!-- Messages: style can be found in dropdown.less-->
                <li class="dropdown messages-menu">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="fa fa-envelope"></i>
                        <span class="label label-success hidden">4</span>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="header">You have 4 messages</li>
                        <li>
                            <!-- inner menu: contains the actual data -->
                            <ul class="menu">

                            </ul>
                        </li>
                        <li class="footer"><a href="#">See All Messages</a></li>
                    </ul>
                </li>
                <!-- User Account -->
                <jsp:include page="../common/account_admin.jsp" />
            </ul>
        </div>
    </nav>
</header>
<div class="wrapper row-offcanvas row-offcanvas-left">
    <jsp:include page="../common/sidebar_admin.jsp" flush="true">
        <jsp:param name="activeMenu" value="recommends" />
    </jsp:include>

    <!-- Right side column. Contains the navbar and content of the page -->
    <aside class="right-side">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                首页
            </h1>
            <ol class="breadcrumb">
                <li class="active"><i class="fa fa-dashboard"></i>首页</li>
            </ol>
        </section>

        <!-- Main content -->
        <section class="content">
            <div class="row" ng-app="recommendApp">
                <!-- right column -->
                <div>
                    <div class="box" ng-controller="RecommendController">
                        <div class="box-body">
                            <table class="table table-bordered">
                                <tbody>
                                <tr>
                                    <th>#</th>
                                    <th>头像</th>
                                    <th>姓名</th>
                                    <th>排序</th>
                                    <th>操作</th>
                                </tr>
                                <tr ng-repeat="item in recommendItems">
                                    <td>{{$index + 1}}</td>
                                    <td>
                                        <img class="avatar-in-list" ng-src="{{item.entity.avatar}}">
                                    </td>
                                    <td>
                                        <a target="_blank" ng-href="/admin/teacher/edit?username={{item.entity.username}}">{{item.entity.name}}</a>
                                    </td>
                                    <td>
                                        <input ng-model="item.rank" class="col-md-2" ng-blur="update(item)">
                                    </td>
                                    <td>
                                        <button ng-click="delete(item)" type="button" class="btn btn-danger">删除</button>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="box-footer">
                            <input ng-model="teacherId" placeholder="教员ID">
                            <button ng-click="create()">增加</button>
                        </div>
                    </div>
                    <!-- /.box -->

                </div>
                <!--/.col (right) -->
            </div>
        </section><!-- /.content -->
    </aside><!-- /.right-side -->
</div><!-- ./wrapper -->
<!-- jQuery 2.0.2 -->
<script src="/public/js/jquery.min.js"></script>
<!-- Bootstrap -->
<script src="/public/js/bootstrap.min.js" type="text/javascript"></script>
<!-- AdminLTE App -->
<script src="/public/js/AdminLTE/app.js" type="text/javascript"></script>
<!-- AdminLTE for demo purposes -->
<script src="/public/js/AdminLTE/demo.js" type="text/javascript"></script>
<script src="/public/js/angular.min.js"></script>
<script src="/public/js/angular-resource.js"></script>
<script>
    angular.module('recommendApp', ['ngResource'])
            .controller('RecommendController', ['$scope', '$resource', function($scope, $resource) {
                var RecommendItem = $resource('/api/recommend/teachers/${recommendId}/:itemId', {itemId:'@id'});
                $scope.recommendItems = RecommendItem.query();

                $scope.create = function () {
                    RecommendItem.save({
                        "rank": 0,
                        "itemId": $scope.teacherId
                    }, function () {
                        console.log('增加成功');
                        $scope.teacherId = '';
                        $scope.recommendItems = RecommendItem.query();
                    });
                };

                $scope.delete = function (item) {
                    console.log('删除数据：', item);
                    item.$delete(function () {
                        console.log('删除成功');
                        $scope.recommendItems = RecommendItem.query();
                    });
                };

                $scope.update = function (item) {
                    console.log('更新数据：', item);
                    item.$save(function () {
                        console.log('更新成功');
                        $scope.recommendItems = RecommendItem.query();
                    });
                };
            }]);
</script>
</body>
</html>