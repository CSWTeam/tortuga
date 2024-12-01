(function() {

    angular.module('rms')
        .directive('navigationLink', navigationLink);

    function navigationLink() {
        return {
            restrict: 'E',
            template: '<md-button ng-class="{ \'md-accent\': isInState() }" ui-sref="{{state}}">{{stateName}}<ng-transclude></ng-transclude></md-button>',
            transclude: true,
            scope: {
                state: '@'
            },
            controller: [ '$scope', '$state', navigationLinkController ]
        };
    }

    function navigationLinkController($scope, $state) {
        $scope.isInState = isInState;

        $scope.stateName = $state.get($scope.state).data.viewName;

        function isInState() {
            return $state.$current.name.indexOf($scope.state) == 0;
        }

    }

})();