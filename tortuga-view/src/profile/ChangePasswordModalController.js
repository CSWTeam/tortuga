(function() {

    angular.module('profile')
        .controller('ChangePasswordModalController', [
            '$mdDialog',
            'AuthenticationService',
            'User',
            '$http',
            'apiAddress',
            ChangePasswordModalController
        ]);

    function ChangePasswordModalController($mdDialog, AuthenticationService, User, $http, apiAddress) {
        var self = this;

        self.cancel = $mdDialog.cancel;
        self.submit = submit;
        self.validatePasswordRepeat = validatePasswordRepeat;

        self.oldPassword = '';
        self.password = '';
        self.passwordRepeat = '';

        //public
        function validatePasswordRepeat(repeatedPassword) {
            if(self.password == repeatedPassword)
                return {
                    passwordMatch: true
                };

            return {
                passwordMatch: false
            };
        }

        //public
        function submit() {
            $http.patch(apiAddress + "users/" + AuthenticationService.getUser().id, {

                    "password": self.password
                }, {
                    headers: {
                        "X-Old-Password": self.oldPassword
                    }
                }).
            then(function() {
                $mdDialog.hide();
            });
        }
    }

})();