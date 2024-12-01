(function () {

    angular.module('login')
        .controller('LoginController', [
            'AuthenticationService',
            'ErrorToasts',
            '$state',
            '$animate',
            '$location',
            '$http',
            '$stateParams',
            '$q',
            '$mdMedia',
            'LocalService',
            LoginController
        ]);

    function LoginController(AuthenticationService, ErrorToasts, $state, $animate, $location, $http, $stateParams, $q, $mdMedia, LocalService) {
        var self = this;

        self.username = '';
        self.password = '';
        self.longToken = false;

        self.login = login;
        self.isMobile = isMobile;

        self.isTerminal = LocalService.isLocal;

        self.loginButtonText = 'Anmelden';

        var token = $stateParams.t;
        if(!!token) {
            openDoorWithToken(token).then(function() {
                $state.go('doorSuccess');
            }).catch(function() {
                self.loginButtonText = 'Anmelden & Tür öffnen';
            })
        } else {
            if(AuthenticationService.isLoggedIn()) {
                $state.go('home');
            }
        }

        // public
        function login() {
            AuthenticationService.login(self.username, self.password, self.longToken)
                .then(function() {
                    if(!!token) {
                        return openDoorWithToken(token);
                    } else {
                        $state.go('home');
                    }
                }).then(function() {
                    if(!!token) {
                        $state.go('doorSuccess');
                    }
                }).catch(function (response) {
                    if (response.status == 401) {
                        ErrorToasts.show("Benutzername und/oder Passwort sind falsch.", 3500, false);
                    }
                });
        }

        function openDoorWithToken(token) {
            if(!token) {
                return $q.reject();
            }

            return $http.patch('/api/v1/terminal/door', {
                open: true
            }, {
                params: {
                    token: token
                }
            });
        }

        //public
        function isMobile() {
            return $mdMedia('xs') || $mdMedia('sm');
        }
    }

})();