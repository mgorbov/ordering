angular.module('ordering').controller('MainCtrl', ['$scope', '$http', function ($scope, $http) {
  $scope.cartGridData = [];
  $scope.itemsGridData = [];
  $http.get("data/items.json").then(function (response) {
    $scope.itemsGridData = response.data;
  });
  $scope.totalSum = 0;
}]);
