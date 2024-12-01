(function() {

    angular.module('rms')
        .directive('optional', Optional);

    function Optional() {
        return {
            restrict: 'E',
            scope: {
                property: '='
            },
            template: [
                '<span ng-if="property">{{property}}</span>',
                '<span ng-if="property == undefined">&mdash;</span>'
            ].join('')
        };
    }

})();