(function () {

    angular.module('management.users', [
        'ui.router',
        'ngSweets'
    ]).config(['$stateProvider', function ($stateProvider) {
        $stateProvider.state('management.users', {
            url: '/users',
            redirectTo: 'management.users.list',
            template: '<ui-view></ui-view>'
        }).state('management.users.list', {
            url: '',
            templateUrl: 'src/management/users/list.html',
            controller: 'UserListController',
            controllerAs: 'userList'
        }).state('management.users.create', {
            url: '/create',
            templateUrl: 'src/management/users/create.html',
            controller: 'UserCreateController',
            controllerAs: 'userCreateController'
        }).state('management.users.finish', {
            url: '/finish',
            templateUrl: 'src/management/users/createFinish.html',
            controller: 'UserFinishController',
            controllerAs: 'userController',
            params: {'user': null}
        });
    }]);


})();
