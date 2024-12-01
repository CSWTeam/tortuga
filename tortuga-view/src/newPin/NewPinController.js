(function() {
    angular.module('profile')
        .controller('NewPinController', [
            '$http',
            '$state',
            '$stateParams',
            'apiAddress',
            '$timeout',
            'PinService',
            NewPinController
        ]);

    function NewPinController($http, $state, $stateParams, apiAddress, $timeout, PinService) {
        var self = this;

        // configuiration
        var successNecessary = 3;



        self.isLoading = true;

        var pin = $http.post(apiAddress + "/users/" + $stateParams.userId + "/passcode").then(function(response) {
            isLoading = false;
            pin = response.data.passcode;
            return response.data.passcode;
        });


        self.twemoji = twemoji;

        var password = ["", "", "", "", ""];
        var passwordIndex = 0;

        var emojisArray = ["😈", "😃", "🎩", "👽", "💩", "❤️", "💎", "👂", "👍", "🐋", "🐶", "🐸", "❄", "🎉", "💿",
            "🍉", "☎", "🎥", "✂", "⚽", "🚀", "💄", "🌂", "🍄", "🍀", "🚗", "🍕", "🍔", "🍨", "💣",
            "🐧", "💼", "🌍", "🐝", "🏠", "⏰"];


        var emojis = [];

        var passwordShow = [0,0,0,0,0];
        var backButtonStatus = 0;

        var success = false;
        var error = false;

        var successCount = 0;

        var showPinHintState = false;

        self.getEmojis = getEmojis;
        self.getPassword = getPassword;
        self.addKey = addKey;
        self.inNormalState = inNormalState;
        self.inSuccesState = inSuccesState;
        self.inErrorState = inErrorState;
        self.getPasswordShow = getPasswordShow;
        self.deletePin = deletePin;
        self.getBackButtonStatus = getBackButtonStatus;
        self.getPin = getPin;
        self.getSuccessNecessary = getSuccessNecessary;
        self.getState = getState;
        self.showPinHint = showPinHint;
        self.doShowPinHint = doShowPinHint;

        resetPin();
        generatePasswordField();


        //public
        function getSuccessNecessary() {
            return successNecessary;
        }


        //public
        function getState() {
            return successCount;
        }

        //public
        function getPin() {
            return pin;
        }

        //public
        function getPassword() {
            return password;
        }

        //public
        function addKey(emoji) {
            if(!inNormalState()) {
                resetPin();
            }
            if(passwordIndex >= 5) {
                return;
            }
            password[passwordIndex] = emoji;
            passwordShow[passwordIndex] = 1;
            passwordIndex++;
            if(passwordIndex == 5) {
                login();
            }
        }



        //public
        function showPinHint(value) {
            showPinHintState = value;
        }


        //public
        function doShowPinHint() {
            return showPinHintState;
        }

        //public
        function getPasswordShow() {
            return passwordShow;
        }

        //public
        function deletePin() {
            if(passwordIndex <= 0) {
                return;
            }
            passwordIndex--;
            password[passwordIndex] = "";
            passwordShow[passwordIndex] = 0;
        }

        //public
        function getBackButtonStatus() {
            return backButtonStatus;
        }


        function updatePinState() {
            if(inErrorState()) {
                passwordShow = [4,4,4,4,4];
                backButtonStatus = 2;
                $timeout(resetPin, 700);
            } else if(inSuccesState()) {
                var timeBetweenAnim = 100;

                function updatePin(pin, number) {
                    return function() {
                        passwordShow[pin] = number;
                    }
                }

                for(var i = 0; i <= 4; i++ ) {
                    $timeout(updatePin(i,2), (i + 1) * timeBetweenAnim);
                }

                $timeout(function() {
                    backButtonStatus = 1;
                }, 6 * timeBetweenAnim);

                $timeout(function() {
                    var tmp = successCount;
                    resetPin();
                    generatePasswordField();

                    successCount = tmp + 1;
                    if(successCount == successNecessary) {
                        $timeout(function() {
                            PinService.finished();
                        },1000)
                    }

                }, 8 * timeBetweenAnim);
            }

        }


        function shufle(array) {
            for(var i = 0; i < array.length; i++) {
                var j = parseInt((Math.random() * (array.length - i))) + i;
                tmp = array[i];
                array[i] = array[j];
                array[j] = tmp;
            }
            return array;
        }

        function generatePasswordField() {
            var width = 6;
            shufle(emojisArray);
            emojis = [];
            for(var i = 0; i < emojisArray.length; i++) {
                if(i % width == 0) {
                    emojis.push([]);
                }
                var t = parseInt(i / width);
                emojis[t].push(emojisArray[i]);
            }
        }

        //public
        function getEmojis() {
            return emojis;
        }

        //public
        function resetPin() {
            password = ["", "", "", "", ""];
            passwordShow = [0,0,0,0,0];
            success = false;
            error = false;
            passwordIndex = 0;
            backButtonStatus = 0;
            successCount = 0;
            showPinHintState = false;
        }

        //public
        function inNormalState() {
            return !success && !error;
        }

        //public
        function inErrorState() {
            return error;
        }

        //public
        function inSuccesState() {
            return success;
        }

        function login() {
            var correct = true;
            for(var i  = 0; i < password.length; i++) {
                if(password[i] !== pin[i]) {
                    correct = false;
                }
            }
            if(correct) {
                success = true;
                successStartTime = (new Date()).valueOf();
                updatePinState();

            } else {
                error = true;
                updatePinState();
            }
        }

    }
})();