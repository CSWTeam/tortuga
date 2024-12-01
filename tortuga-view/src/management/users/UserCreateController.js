(function() {
    angular.module('management.users')
        .controller('UserCreateController', [
            'User',
            'Major',
            '$state',
            'PinService',
            '$window',
            'AuthenticationService',
            UserCreateController
        ]);

    function UserCreateController(User, Major, $state, PinService, $window, AuthenticationService) {
        var self = this;
        var state = 0;

        self.majors = Major.query();

        self.getState = getState;
        self.back = back;
        self.advance = advance;
        self.save = save;
        self.validatePasswordRepeat = validatePasswordRepeat;

        self.isAdmin = AuthenticationService.isAdmin;

        self.pin = [];

        self.user = {};
        self.user.gender = "NONE";

        self.next = true;
        self.previous = false;

        self.privacyTxt = false;


        //public
        function getState() {
            return state;
        }

        //public
        function back() {

            if(state == 1){
                console.log("back");
                self.next = false;
                self.previous = true;
            }
            $window.scrollTo(0,0);
            state--;
        }

        //public
        function advance() {
            if(state == 1){
                self.next = true;
                self.previous = false;
            }
            state++;
        }

        //public
        function validatePasswordRepeat(repeatedPassword) {
            if(self.password1 == repeatedPassword)
                return {
                    passwordMatch: true
                };

            return {
                passwordMatch: false
            };
        }

        //public
        function save() {
            if(self.user.gender == "NONE") {
                self.user.gender = undefined;
            }
            self.user.enabled = true;
            self.user.password = self.password1;
            User.save(self.user).$promise
                .then(function(user) {
                    PinService.createNewPin(user).then(function() {
                        $state.go('management.users.finish' ,{user: user});
                    });
                })
        }


    }
})();