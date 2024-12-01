(function() {

    angular.module('rms')
        .factory('Major', [
            '$resource',
            'apiAddress',
            Major
        ]);

    function Major($resource, apiAddress) {
        return $resource(apiAddress + 'majors/:id', null, {update: { method: 'PATCH'}});
    }

})();
