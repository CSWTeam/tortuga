(function() {
    angular.module('management.statistics')
        .controller('StatisticsController', [
            'DeviceReservation',
            'Device',
            'DeviceCategory',
            'Major',
            'RoomReservation',
            'SupportMessage',
            'User',
            'DoorAuthorisationAttempt',
            StatisticsController
        ]);


    function StatisticsController(DeviceReservation, Device, DeviceCategory, Major, RoomReservation, SupportMessage, User, DoorAuthorisationAttempt) {
        var self = this;

        self.allDeviceReservations = DeviceReservation.query();

        self.allUsages = DoorAuthorisationAttempt.query();

        self.allRoomReservations = RoomReservation.query();

        self.allUsers = User.query();

        self.allMajors = Major.query();

        self.allDevices = Device.query();

        self.selectedDate = new Date();


        self.deviceResCount = undefined;
        self.deviceNames = undefined;
        self.reservationsPerDay = undefined;
        self.weekDays = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag', 'Sonntag'];
        self.reservationCountPerDevicePerDayArray = undefined;
        self.reservationPerGender = undefined;
        self.reservationPerMajor = undefined;
        self.majorNames = undefined;
        self.lengthLabelsArray = undefined;
        self.usersPerGenderArray = undefined;
        self.weekOfMonthArray = undefined;
        self.usagePerWeekArray = undefined;
        self.usagePerMonthArray = undefined;
        self.usagePerMajorAndWeekArray = undefined;
        self.usagePerMajorAndMonthArray = undefined;

        self.reservationCount = reservationCount;
        self.getDeviceNames = getDeviceNames;
        self.reservationCountPerDay = reservationCountPerDay;
        self.reservationCountPerDevicePerDay = reservationCountPerDevicePerDay;
        self.reservationCountPie = reservationCountPie;
        self.reservationsPerGender = reservationsPerGender;
        self.reservationsPerMajor = reservationsPerMajor;
        self.getMajorNames = getMajorNames;
        self.lengthLabels = lengthLabels;
        self.reservationsPerLength = reservationsPerLength;
        self.usagePerWeek = usagePerWeek;
        self.updateDateUsages = updateDateUsages;
        self.getWeekOfMonth = getWeekOfMonth;
        self.usagePerMonth = usagePerMonth;


        /**
         * To add a new statistic, you need to add an object to this array.
         * name: Name of the statistic
         * type: type of the statistic one of 'Bar', 'Line', 'Pie', 'Doughnut', 'Radar', 'Polar-Atea'
         *
         *
         *
         * for mor see http://jtblin.github.io/angular-chart.js/
         * @type {*[]}
         */
        self.graphs = [
            {
                name: 'Reservierungen nach Gerät',
                type: 'Bar',
                data: reservationCount,
                labels: getDeviceNames,
                serie: wrapperFunction(['Reservierungen']),
                legend: false
            },
            {
                name: 'Reservierungen nach Gerät (Kuchen)',
                type: 'Pie',
                data: reservationCountPie,
                labels: getDeviceNames,
                serie: wrapperFunction(['Devices']),
                legend: false
            },
            {
                name: 'Reservierungen nach Wochentag',
                type: 'Bar',
                data: reservationCountPerDay,
                labels: wrapperFunction(self.weekDays),
                serie: wrapperFunction(['Reservierungen']),
                legend: false
            },
            {
                name: 'Reservierungen nach Gerät und Wochentag',
                type: 'Line',
                data: reservationCountPerDevicePerDay,
                labels: wrapperFunction(self.weekDays),
                serie: getDeviceNames,
                legend: true
            },
            {
                name: 'Reservierungen nach Geschlecht',
                type: 'Pie',
                data: reservationsPerGender,
                labels: wrapperFunction(['männlich', 'weiblich', 'K.A.']),
                serie: wrapperFunction(['Reservierungen']),
                legend: true
            },
            {
                name: 'Reservierungen nach Studiengang',
                type: 'Pie',
                data: reservationsPerMajor,
                labels: getMajorNames,
                serie: wrapperFunction(['Reservierungen']),
                legend: true
            },
            {
                name: 'Reservierungen nach Länge',
                type: 'Bar',
                data: reservationsPerLength,
                labels: lengthLabels,
                serie: wrapperFunction(['Reservierungen']),
                legend: true
            },
            {
                name: 'Benutzer nach Geschlecht',
                type: 'Pie',
                data: usersPerGender,
                labels: wrapperFunction(['männlich', 'weiblich', 'K.A.']),
                serie: wrapperFunction(['Benutzer']),
                legend: true
            },
            {
                name: 'Benutzung nach Woche',
                type: 'Line',
                data: usagePerWeek,
                labels: getWeekOfMonth,
                serie: wrapperFunction(['emoji', 'qr code', 'Daueröffnungen', 'Alle']),
                legend: true,
                needsDate: true
            },
            {
                name: 'Benutzung nach Monat',
                type: 'Line',
                data: usagePerMonth,
                labels: getMonth,
                serie: wrapperFunction(['emoji', 'qr code', 'Daueröffnungen', 'Alle']),
                legend: true,
                needsDate: true
            },
            {
                name: 'Benutzung nach Studiengang und Woche',
                type: 'Line',
                data: usagePerMajorAndWeek,
                labels: getWeekOfMonth,
                serie: getMajorNames,
                legend: true,
                needsDate: true
            },
            {
                name: 'Benutzung nach Studiengang und Monat',
                type: 'Line',
                data: usagePerMajorAndMonth,
                labels: getMonth,
                serie: getMajorNames,
                legend: true,
                needsDate: true
            }
        ];

        updateDateUsages();

        //public
        function updateDateUsages() {
            updateWeekOfMonth();
            updateUsagePerWeek();
            updateMonth();
            updateUsagePerMonth();
            updateUsagePerMajorAndWeek();
            updateUsagePerMajorAndMonth();
        }




        //public
        function usagePerMajorAndMonth() {
            return self.usagePerMajorAndMonthArray;
        }

        function updateUsagePerMajorAndMonth() {
            self.usagePerMajorAndMonthArray = [];

            self.allMajors.forEach(function(major) {

                var ret = [];
                var originalDate = angular.copy(self.selectedDate);


                originalDate.setMonth(originalDate.getMonth() - 11);
                for(var i = 0; i < 12; i++) {
                    var userSet = getUsageUserPerMonthByType(originalDate);

                    ret.push(0);
                    userSet.forEach(function(userId) {
                        var user = getUser(userId);
                        if(user.major != undefined && user.major.id == major.id) {
                            ret[i]++;
                        }
                    });
                    originalDate.setMonth(originalDate.getMonth() + 1);
                }
                self.usagePerMajorAndMonthArray.push(ret);
            });
        }
        
        
        
        //public
        function usagePerMajorAndWeek() {
            return self.usagePerMajorAndWeekArray;
        } 
        
        function updateUsagePerMajorAndWeek() {
            self.usagePerMajorAndWeekArray = [];
            
            self.allMajors.forEach(function(major) {

                var ret = [];
                var originalDate = angular.copy(self.selectedDate);
                originalDate.setDate(originalDate.getDate() - 10 * 7);
                for(var i = 0; i < 10; i++) {
                    var userSet = getUsageUserPerWeekByType(originalDate);

                    ret.push(0);
                    userSet.forEach(function(userId) {
                        var user = getUser(userId);
                        if(user.major != undefined && user.major.id == major.id) {
                            ret[i]++;
                        }
                    });
                    originalDate.setDate(originalDate.getDate() + 7);
                }
                self.usagePerMajorAndWeekArray.push(ret);
            });
        }

        //public
        function getWeekOfMonth() {
            return self.weekOfMonthArray;
        }


        //public
        function updateWeekOfMonth() {
            self.weekOfMonthArray = [];
            var startDate = angular.copy(self.selectedDate);
            var endDate = angular.copy(self.selectedDate);
            endDate.setDate(endDate.getDate() + 6);
            startDate.setDate(startDate.getDate() - 10 * 7);
            endDate.setDate(endDate.getDate() - 10 * 7);
            for(var i = 0; i < 10; i++) {
                self.weekOfMonthArray[i] = startDate.toDateString() + "\n- " + endDate.toDateString();
                endDate.setDate(endDate.getDate() + 7);
                startDate.setDate(startDate.getDate() + 7);
            }

        }

        function getMonth() {
            return self.monthArray;
        }

        function updateMonth() {

            var monthNames = [
                "Januar", "Februar", "März",
                "April", "Mai", "Juni", "Juli",
                "August", "September", "Oktober",
                "November", "December"
            ];


            self.monthArray = [];
            var originalDate = angular.copy(self.selectedDate);

            originalDate.setMonth(originalDate.getMonth() - 11);
            for(var i = 0; i < 12; i++) {
                self.monthArray.push(monthNames[originalDate.getMonth()] + " " + originalDate.getFullYear());
                originalDate.setMonth(originalDate.getMonth() + 1);
            }
        }



        //public
        function usagePerMonth() {
            return self.usagePerMonthArray;
        }


        function getUsageUserPerMonthByType(dayInMonth, type) {
            dayInMonth = new Date(dayInMonth);
            dayInMonth.setDate(1);
            var startTime = dayInMonth.valueOf();

            dayInMonth.setMonth(dayInMonth.getMonth() + 1);
            dayInMonth.setDate(0);
            dayInMonth.setHours(23,59);
            var endTime = dayInMonth.valueOf();

            var userSet = new Set();
            self.allUsages.forEach(function(usage) {
                if(usage.successful && usage.user != undefined && (type == undefined || usage.authType == type) && startTime < usage.time && usage.time < endTime)  {
                    userSet.add(usage.user.id);
                }
            });
            return userSet;

        }

        function updateUsagePerMonth() {
            self.usagePerMonthArray = [];

            var types = ["EMOJIS", "QR_CODE", "ROOM_RESERVATION", undefined];
            types.forEach(function(type) {

                var ret = [];
                var originalDate = angular.copy(self.selectedDate);
                originalDate.setMonth(originalDate.getMonth() - 11);
                for(var i = 0; i < 12; i++) {
                    ret.push(getUsageUserPerMonthByType(originalDate, type).size);
                    originalDate.setMonth(originalDate.getMonth() + 1);
                }
                self.usagePerMonthArray.push(ret);
            });
        }

        //public
        function usagePerWeek() {
            return self.usagePerWeekArray;
        }


        function getUsageUserPerWeekByType(startDay, type) {
            var startTime = startDay.valueOf();
            var endTime = startDay.valueOf() + 7 * 24 * 60 * 60 * 1000;

            var userSet = new Set();
            self.allUsages.forEach(function(usage) {
                if(usage.successful && usage.user != undefined && (type == undefined || usage.authType == type) && startTime < usage.time && usage.time < endTime)  {
                    userSet.add(usage.user.id);
                }
            });
            return userSet;

        }

        function updateUsagePerWeek() {
            self.usagePerWeekArray = [];

            var types = ["EMOJIS", "QR_CODE", "ROOM_RESERVATION", undefined];
            types.forEach(function(type) {

                var ret = [];
                var originalDate = angular.copy(self.selectedDate);
                originalDate.setDate(originalDate.getDate() - 10 * 7);
                for(var i = 0; i < 10; i++) {
                    ret.push(getUsageUserPerWeekByType(originalDate, type).size);
                    originalDate.setDate(originalDate.getDate() + 7);
                }
                self.usagePerWeekArray.push(ret);
            });
        }


        //public
        function usersPerGender() {
            if(self.usersPerGenderArray == undefined && self.allUsers.length != 0) {
                self.usersPerGenderArray = [0,0,0];

                self.allUsers.forEach(function(user) {
                    if(user.gender == "MALE") {
                        self.usersPerGenderArray[0]++;
                    } else if(user.gender == "FEMALE") {
                        self.usersPerGenderArray[1]++;
                    } else {
                        self.usersPerGenderArray[2]++;
                    }
                });
            }
            return self.usersPerGenderArray;
        }


        //public
        function lengthLabels() {
            if(self.lengthLabelsArray == undefined && self.reservationPerLengthArray != undefined) {
                self.lengthLabelsArray = [];
                for(var i in self.reservationPerLengthArray[0]) {
                    self.lengthLabelsArray.push(i * 1 + "h");
                }
            }
            return self.lengthLabelsArray;
        }

        //public
        function reservationsPerLength() {
            if(self.reservationPerLengthArray == undefined && self.allDeviceReservations.length != 0) {
                self.reservationPerLengthArray = [];
                self.allDeviceReservations.forEach(function(reservation) {
                    var length = reservation.timeSpan.end - reservation.timeSpan.beginning;
                    length = Math.floor(length / 1000 / 60 / 60); // so we have an int
                    if(self.reservationPerLengthArray[length] == undefined) {
                        self.reservationPerLengthArray[length] = 0;
                    }
                    self.reservationPerLengthArray[length]++;
                });

                for(var i = 0; i < self.reservationPerLengthArray.length; i++) {
                    if(!self.reservationPerLengthArray.hasOwnProperty(i)) {
                        self.reservationPerLengthArray[i] = 0;
                    }
                }
                self.reservationPerLengthArray = [self.reservationPerLengthArray];
                console.dir(self.reservationPerLengthArray);
            }
            return self.reservationPerLengthArray;
        }

        //public
        function getMajorNames() {
            if(self.majorNames == undefined && self.allMajors.length != 0) {
                self.majorNames = self.allMajors.map(function(major) {
                    return major.name;
                })
            }
            return self.majorNames;
        }

        //public
        function reservationsPerMajor() {
            if(self.reservationPerMajor == undefined && self.allMajors.length != 0 && self.allDeviceReservations.length != 0) {
                self.reservationPerMajor = [];

                var userSet = new Set();
                self.allDeviceReservations.forEach(function(reservation) {
                    userSet.add(reservation.user.id);
                });

                var userArray = [];
                userSet.forEach(function(user) {
                    userArray.push(user)
                });

                self.allMajors.forEach(function(major) {
                    self.reservationPerMajor.push(userArray.filter(function(userId) {
                        var user = getUser(userId);
                        if(user.major == undefined) {
                            return false;
                        }
                        return user.major.id == major.id;
                    }).length);
                });
            }
            return self.reservationPerMajor;
        }

        function getUser(id) {
            for(var userIndex in self.allUsers) {
                if(self.allUsers[userIndex].id == id) {
                    return self.allUsers[userIndex];
                }
            }
            return null;
        }

        //public
        function reservationsPerGender() {
            if(self.reservationPerGender == undefined && self.allDeviceReservations.length != 0) {
                self.reservationPerGender = [0, 0, 0];
                var userSet = new Set();
                self.allDeviceReservations.forEach(function(reservation) {
                    userSet.add(reservation.user.id);
                });
                userSet.forEach(function(userId) {
                    var user = getUser(userId);
                    if(user.gender == "MALE") {
                        self.reservationPerGender[0]++;
                    } else if(user.gender == "FEMALE") {
                        self.reservationPerGender[1]++;
                    } else {
                        self.reservationPerGender[2]++;
                    }
                });
            }
            return self.reservationPerGender;
        }

        //public
        function reservationCountPie() {
            return reservationCount()[0];
        }

        function wrapperFunction(value) {
            return function() {
                return value;
            }
        }

        //public
        function getDeviceNames() {
            if(self.deviceNames == undefined && self.allDevices.length != 0) {
                self.deviceNames = self.allDevices.map(function(device) {
                    return device.name
                });
            }
            return self.deviceNames;
        }


        //public
        function reservationCountPerDay() {
            if(self.reservationsPerDay == undefined && self.allDeviceReservations.length != 0) {
                self.reservationsPerDay = [];
                for(var i = 0; i < 7; i++) {
                    self.reservationsPerDay[i] = self.allDeviceReservations.filter(function(res) {
                        return new Date(res.timeSpan.beginning).getUTCDay() == (i + 1) % 7; //  (i + 1) % 7 so monday is the start of the week
                    }).length;
                }
                self.reservationsPerDay = [self.reservationsPerDay];
            }
            return self.reservationsPerDay;
        }


        //public
        function reservationCountPerDevicePerDay() {
            if(self.reservationCountPerDevicePerDayArray == undefined && self.allDevices.length != 0 && self.allDeviceReservations.length != 0) {
                self.reservationCountPerDevicePerDayArray = self.allDevices.map(function(device) {
                    var ret = [];
                    for(var i = 0; i < 7; i++) {
                        ret[i] = self.allDeviceReservations.filter(function(res) {
                            return res.device.id == device.id &&
                                (new Date(res.timeSpan.beginning)).getDay() ==  (i + 1) % 7;//  (i + 1) % 7 so monday is the start of the week
                        }).length;
                    }
                    return ret;
                })
            }
            return self.reservationCountPerDevicePerDayArray;
        }


        //public
        function reservationCount() {
            if(self.deviceResCount == undefined && self.allDevices.length != 0 && self.allDeviceReservations.length != 0) {
                self.deviceResCount = [];
                self.deviceResCount.push(self.allDevices.map(function(device) {
                    return self.allDeviceReservations.filter(function(reservation) {
                        return reservation.device.id == device.id;
                    }).length;
                }));
            }
            return self.deviceResCount;
        }

    }

})();