(function() {
    angular.module('management.users')
        .controller('UserFinishController',[
            '$stateParams',
            'AuthenticationService',
            UserFinishController
        ]);
    function UserFinishController($stateParams, AuthenticationService) {
        var self = this;

        self.user = $stateParams.user;
        self.logedInUser = AuthenticationService.getUser();
    }
})();