(function () {

    angular.module('management.users')
        .controller('UserListController', [
            'User',
            '$mdDialog',
            'AuthenticationService',
            UserListController
        ]);

    function UserListController(UserService, $mdDialog, AuthenticationService) {
        var self = this;

        self.users = UserService.query();

        self.showDetails = showDetails;
        self.editUser = editUser;
        self.deleteUser = deleteUser;
        self.validatePasswordRepeat = validatePasswordRepeat;
        self.extendValidTime = extendValidTime;
        self.setInactive = setInactive;
        self.setActive = setActive;
        self.userExpiresThisSemester = userExpiresThisSemester;
        self.activeFilter = activeFilter;
        self.isUserExpired = isUserExpired;
        self.resetPassword = resetPassword;
        self.canTouchUser = canTouchUser;

        self.showActive = true;

        self.userFilter = '';

        //public
        function isUserExpired(user){
            return user.expirationDate < (new Date()).getTime();
        }

        function userIsActive(user) {
            if(user.role != 'STUDENT')
                return user.enabled;

            var now = (new Date()).getTime();

            return user.expirationDate >= now && user.enabled;
        }

        //public
        function activeFilter(user) {
            return self.showActive ? userIsActive(user) : !userIsActive(user);
        }

        //public
        function canTouchUser(user) {
            if(AuthenticationService.isAdmin())
                return true;

            if(AuthenticationService.isCswTeam())
                return user.role == 'STUDENT' || user.role == 'LECTURER';

            return false;
        }

        //public
        function userExpiresThisSemester(user) {
            var date = getNextSemesterBeginning(new Date());
            return Math.abs(user.expirationDate - date.getTime()) < 60 * 60 * 10000;
        }

        function getNextSemesterBeginning(date) {
            if(date.getMonth() < 3) { //april
                date.setMonth(3);
            } else if(date.getMonth() < 9) { //october
                date.setMonth(9);
            } else { // else it's after october so we set the date to october
                date.setMonth(3);
                date.setFullYear(date.getFullYear() + 1);
            }

            date = new Date(date.getFullYear(), date.getMonth());
            return date;
        }

        //public
        function extendValidTime(user, event) {
            var date = new Date(user.expirationDate);

            do {
                date.setMonth(date.getMonth() + 6);
            } while((new Date).valueOf() > date.valueOf());

            user.expirationDate = date.valueOf();


            user = UserService.update({id: user.id}, user);
        }

        //public
        function setInactive(user, event) {
            user.enabled = false;

            //das kopieren kann weg wenn man expirationDate mitsenden kann
            var newUser = angular.copy(user);
            newUser.expirationDate = undefined;
            user = UserService.update({id: newUser.id}, newUser);
        }

        //public
        function setActive(user){
            user.enabled = true;

            //das kopieren kann weg wenn man expirationDate mitsenden kann
            var newUser = angular.copy(user);
            newUser.expirationDate = undefined;
            user = UserService.update({id: newUser.id}, newUser);
        }

        //public
        function validatePasswordRepeat() {
            return {
                samePassword: self.password1 == self.password2
            }
        }

        //public
        function deleteUser(user, event) {
            var dialog = $mdDialog.confirm()
                .title("Benutzer " + user.loginName + " löschen?")
                .textContent("Den Benutzer " + user.loginName + " wirklich löschen? Dies kann nicht rückgängig gemacht werden!")
                .ok("löschen")
                .targetEvent(event)
                .cancel("abbrechen");
            $mdDialog.show(dialog).then(function() {
                return UserService.delete({id: user.id}).$promise;
            }).then(function(response) {
                var index = self.users.indexOf(user);
                self.users.splice(index, 1);
            }).catch(function(fail) {
                console.warn(fail);
            });
        }

        //public
        function showDetails(user, event) {
            $mdDialog.show({
                clickOutsideToClose: true,
                templateUrl: 'src/management/users/details.html',
                controller: ['$mdDialog', UserDetailsModalController],
                controllerAs: 'userDetails',
                targetEvent: event,
                bindToController: true,
                locals: {
                    user: user
                }
            });

            function UserDetailsModalController($mdDialog) {
                var self = this;

                //self.user local

                self.close = close;

                function close() {
                    $mdDialog.cancel();
                }
            }
        }


        //public
        function resetPassword(userToReset, event){
            $mdDialog.show({
                templateUrl: 'src/management/users/resetPassword.html',
                controller: ['$mdDialog', resetPasswordController],
                controllerAs: 'passwordResetModal',
                targetEvent: event,
                bindToController: true,
                locals: {
                    user: angular.copy(userToReset)
                }
            }).then(function (user){
                return UserService.update({id: user.id}, {password: user.password});
            }).catch(function (reason) {
                if (reason != undefined)
                    console.warn(reason);
            });

            function resetPasswordController($mdDialog){
                var self = this;

                self.submit = submit;
                self.cancel = cancel;

                self.header = "Passwort von '" + self.user.loginName + "' zurücksetzen";

                //public
                function cancel() {
                    $mdDialog.cancel();
                }

                //public
                function submit() {
                    $mdDialog.hide(self.user);
                }


            }
        }

        //public
        function editUser(userToEdit, event) {
            var id = self.users.indexOf(userToEdit);
            if(userToEdit.gender === undefined){
                userToEdit.gender = "NONE";
            }
            $mdDialog.show({
                templateUrl: 'src/management/users/edit.html',
                controller: ['$mdDialog', 'Major',  EditUserModalController],
                controllerAs: 'userModal',
                targetEvent: event,
                bindToController: true,
                locals: {
                    user: angular.copy(userToEdit)
                }
            }).then(function (user) {
                    return UserService.update({id: user.id}, user);
            }).then(function (user) {
                userToEdit = user;
                self.users[id] = user;
            }).catch(function (reason) {
                if (reason != undefined)
                    console.warn(reason);
            });


            function EditUserModalController($mdDialog, Major) {
                var self = this;

                self.submit = submit;
                self.cancel = cancel;

                self.validatePasswordRepeat = validatePasswordRepeat;

                self.header = "";

                self.majors = Major.query();

                self.header = "Benutzer '" + self.user.loginName + "' bearbeiten";


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
                function cancel() {
                    $mdDialog.cancel();
                }

                //public
                function submit() {
                    if(self.user.gender == "NONE") {
                        self.user.gender = undefined;
                    }
                    self.user.password = self.password1;
                    $mdDialog.hide(self.user);
                }

            }

        }

    }

})();
