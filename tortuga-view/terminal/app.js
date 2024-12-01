(function() {


    function rgbToHex(r, g, b) {
        return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
    }

    angular.module('rms-terminal', [
        'ngResource'
    ])


    angular.module('rms-terminal')
        .constant('apiAddress', '/api/v1/');

    angular.module('rms-terminal')
        .service('ProblemService', [
            '$timeout',
            'SupportMessage',
            'ComplaintTemplate',
            '$interval',
            ProblemService
        ]);

    function ProblemService($timeout, SupportMessage, ComplaintTemplate, $interval) {
        var self = this;

        self.submitProblem = submitProblem;
        self.chooseProblem = chooseProblem;
        self.isChoosingProblem = isChoosingProblem;
        self.getPossibleProblems = getPossibleProblems;

        var choosingProblem = false;
        var possibleProblems = ComplaintTemplate.query();

        $interval(function() {
            possibleProblems = ComplaintTemplate.query();
        }, 600000);

        //public
        function isChoosingProblem() {
            return choosingProblem;
        }

        //public
        function submitProblem(problem) {
            SupportMessage.save({
                name: 'Terminal',
                subject: problem,
                body: '-'
            }).$promise.then(function() {
                choosingProblem = false;
            });
        }

        //public
        function chooseProblem() {
            choosingProblem = true;

            $timeout(function() {
                if(choosingProblem)
                    choosingProblem = false;
            }, 12000);
        }

        //public
        function getPossibleProblems() {
            return possibleProblems;
        }
    }

    angular.module('rms-terminal')
        .controller('LoginController', [
            '$http',
            'apiAddress',
            '$timeout',
            '$interval',
            'ComplaintTemplate',
            '$rootScope',
            '$window',
            'ProblemService',
            'baseHost',
            LoginController
        ]);

    function LoginController($http, apiAddress, $timeout, $interval, ComplaintTemplate, $scope, $window, ProblemService, baseHost) {
        var self = this;
        self.twemoji = twemoji;
        self.loginUrl = "not initialized";

        var password = ["", "", "", "", ""];
        var passwordIndex = 0;

        var emojisArray = ["😈", "😃", "🎩", "👽", "💩", "❤️", "💎", "👂", "👍", "🐋", "🐶", "🐸", "❄", "🎉", "💿",
            "🍉", "☎", "🎥", "✂", "⚽", "🚀", "💄", "🌂", "🍄", "🍀", "🚗", "🍕", "🍔", "🍨", "💣",
            "🐧", "💼", "🌍", "🐝", "🏠", "⏰"];


        var lastOpenedDoorAt = 0;

        var emojis = [];

        var passwordShow = [0, 0, 0, 0, 0];
        var backButtonStatus = 0;

        var success = false;
        var error = false;
        var successStartTime = 0;

        self.roomReservation = undefined;

        self.getEmojis = getEmojis;
        self.getPassword = getPassword;
        self.addKey = addKey;
        self.inNormalState = inNormalState;
        self.inSuccesState = inSuccesState;
        self.inErrorState = inErrorState;
        self.getPasswordShow = getPasswordShow;
        self.deletePin = deletePin;
        self.getBackButtonStatus = getBackButtonStatus;
        self.openDoor = openDoor;

        self.isChoosingProblem = ProblemService.isChoosingProblem;
        self.chooseProblem = ProblemService.chooseProblem;
        self.getPossibleProblems = ProblemService.getPossibleProblems;
        self.submitProblem = ProblemService.submitProblem;

        resetPin();
        generatePasswordField();

        function getPassword() {
            return password;
        }

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
                passwordShow = [4, 4, 4, 4, 4];
                backButtonStatus = 2;
                $timeout(resetPin, 700);
            } else if(inSuccesState()) {
                var timeBetweenAnim = 100;

                function updatePin(pin, number) {
                    return function() {
                        passwordShow[pin] = number;
                    }
                }

                for(var i = 0; i <= 4; i++) {
                    $timeout(updatePin(i, 2), (i + 1) * timeBetweenAnim);
                }

                $timeout(function() {
                    backButtonStatus = 1;
                }, 6 * timeBetweenAnim);

                $timeout(function() {
                    resetPin();
                    generatePasswordField();
                    $scope.$apply();

                }, 10 * timeBetweenAnim);
            }
        }


        pollForOpenedRoom();
        $interval(pollForOpenedRoom, 15 * 1000);
        function pollForOpenedRoom() {
            var now = (new Date()).valueOf();
            $http.get(apiAddress + "roomreservations?open=true&timeSpan.end=>" + (now - 5 * 60 * 60 * 1000)).then(function(response) {
                    var foundOpen = false;
                    if(response.data.length > 0) {
                        for(var i = 0; i < response.data.length; i++) {
                            var reservation = response.data[i];
                            if(reservation.openedTimeSpan.end >= now && reservation.openedTimeSpan.beginning <= now) {
                                foundOpen = true;
                                self.roomReservation = reservation;
                            }
                        }
                    }
                    if(!foundOpen) {
                        self.roomReservation = undefined;
                    }
                });
        }


        pollForQRcode();
        $interval(pollForQRcode, 15 * 1000);
        function pollForQRcode() {
            $http.get(apiAddress + "terminal/code").then(function(response) {
                self.loginUrl = baseHost + "login?t=" + response.data;
            })
        }

        //public
        function openDoor() {
            var now = new Date().valueOf();
            if(now - lastOpenedDoorAt > 10 * 1000) {
                lastOpenedDoorAt = now;
                $http.patch(apiAddress + "terminal/door", {open: true});
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
            var  width = 6;
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
            passwordShow = [0, 0, 0, 0, 0];
            success = false;
            error = false;
            passwordIndex = 0;
            backButtonStatus = 0;
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
            var passwordString = password.join("");
            $http.patch(apiAddress + "terminal/door?passcode=" + encodeURIComponent(passwordString), {open:true})
                .then(function() {
                    success = true;
                    successStartTime = (new Date()).valueOf();
                    updatePinState();
                }).catch(function() {
                error = true;
                updatePinState();
            });
        }
    }


    angular.module('rms-terminal')
        .factory('ComplaintTemplate', [
            '$resource',
            'apiAddress',
            ComplaintTemplate
        ]);

    function ComplaintTemplate($resource, apiAddress) {
        return $resource(apiAddress + 'complainttemplates/:id', null, {update: {method: 'PATCH'}});
    }

    angular.module('rms-terminal')
        .factory('SupportMessage', [
            '$resource',
            'apiAddress',
            SuppportMessage
        ]);

    function SuppportMessage($resource, apiAddress) {
        return $resource(apiAddress + 'supportmessages/:id', null, {update: {method: 'PATCH'}});
    }


    angular.module('rms-terminal')
        .directive('twemoji', ['$window', function($window) {
            return {
                restrict: 'E',
                scope: {
                    emoji: "="
                },
                link: function(scope, elem, attr) {
                    //console.dir(elem);

                    //var img = elem.append($window.twemoji.parse(scope.emoji));
                    elem.text(scope.emoji);

                    scope.$parent.$watch(attr.emoji, function(newVal) {
                        //img.children().remove();
                        //elem.html("");
                        //img = elem.append($window.twemoji.parse(newVal));
                        elem.text(newVal);
                    })
                }
            };
        }]);


    angular.module('rms-terminal')
        .directive('qrcode', ['$window' , function($window) {
            return {
                restrict: 'E',
                scope: {
                    code: "="
                },
                link: function(scope, elem, attr) {
                    //console.dir(elem);

                    var domElem = document.createElement("div");

                    var qrcode = new QRCode(domElem, {
                        text: scope.code,
                        width: 280,
                        height: 280,
                        colorDark : "#000000",
                        colorLight : "#ffffff",
                        correctLevel : QRCode.CorrectLevel.L
                    });

                    elem.append(domElem.childNodes);


                    scope.$parent.$watch(attr.code, function(newVal, oldVal, scope) {
                        if(newVal == undefined) {
                            newVal = scope.code;
                        }
                        qrcode.clear();
                        qrcode.makeCode(newVal);

                        elem.append(domElem.childNodes);
                    });

                }
            };
        }]);

})
();
