(function() {

    angular.module('profile')
        .controller('ProfileController', [
            'AuthenticationService',
            '$mdDialog',
            'PinService',
            '$state',
            ProfileController
        ]);

    function ProfileController(AuthenticationService, $mdDialog, PinService,$state) {
        var self = this;

        self.getUser = AuthenticationService.getUser;

        self.getGender = getGender;
        self.getRole = getRole;
        self.changePassword = changePassword;
        self.newPin = newPin;

        //public
        function newPin() {
            PinService.createNewPin(self.getUser()).then(function() {
                $state.go('profile');
            });
        }



        self.test = function() {
            $state.go('management.users.finish' ,{user: self.getUser()});
        };


        //public
        function changePassword(event) {
            $mdDialog.show({
                templateUrl: 'src/profile/changePasswordModal.html',
                controller: 'ChangePasswordModalController',
                controllerAs: 'passwordModal',
                targetEvent: event
            });
        }

        function getGender() {
            switch(self.getUser().gender) {
                case 'MALE':
                    return 'Männlich';
                case 'FEMALE':
                    return 'Weiblich';
                default:
                    return self.getUser().gender;
            }
        }

        function getRole() {
            switch(self.getUser().role) {
                case 'STUDENT':
                    return 'Student';
                case 'LECTURER':
                    return 'Dozent';
                case 'CSW_TEAM':
                    return 'CSW Team';
                case 'ADMIN':
                    return 'Admin';
                default:
                    return self.getUser().role;
            }
        }
    }

})();