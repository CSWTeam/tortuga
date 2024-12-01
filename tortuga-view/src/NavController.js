(function () {

    angular.module('rms')
        .controller('NavController', [
            '$state',
            'AuthenticationService',
            'TimeoutService',
            '$mdDialog',
            '$rootScope',
            '$mdSidenav',
            '$mdMedia',
            'LocalService',
            NavController
        ]);

    function NavController($state, AuthenticationService, TimeoutService, $mdDialog, $rootScope, $mdSidenav, $mdMedia, LocalService) {
        var self = this;

        self.isMobile = isMobile;
        self.openSidenav = openSidenav;
        self.getCurrentStateName = getCurrentStateName;
        self.isInState = isInState;
        self.supportTicket = supportTicket;
        self.isLoggedIn = AuthenticationService.isLoggedIn;
        self.logout = AuthenticationService.logout;

        self.somethingHappened = TimeoutService.somethingHappened;

        self.isStudent = AuthenticationService.isStudent;
        self.isLecturer = AuthenticationService.isLecturer;
        self.isCswTeam = AuthenticationService.isCswTeam;
        self.isAdmin = AuthenticationService.isAdmin;

        self.isLocal = LocalService.isLocal;

        $rootScope.$on('$stateChangeStart', function onStateChange() {
            $mdSidenav('left').close();
            $mdDialog.cancel();
        });

        //public
        function getCurrentStateName() {
            if($state.$current.data)
                return $state.$current.data.viewName ;

            return '';
        }

        //public
        function openSidenav() {
            $mdSidenav('left').open();
        }

        //public
        function isInState(state) {
            return $state.$current.name.indexOf(state) == 0;
        }

        //public
        function isMobile() {
            return !$mdMedia('gt-sm');
        }

        //public
        function supportTicket() {
            $mdDialog.show({
                templateUrl: 'src/support/createSupportTicket.html',
                controller: 'CreateSupportTicketModalController',
                controllerAs: 'supportModal',
                targetEvent: event,
                bindToController: true
            });
        }
    }

})();

