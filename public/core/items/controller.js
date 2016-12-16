angular.module('ordering').controller('ItemsCtrl', ['$scope', '$timeout', '$http',
  function ($scope, $timeout, $http) {

    $scope.categoryIds = [];

    prepareItemGrid();
    prepareCategoryGrid();
    prepareCartGrid();

    function prepareItemGrid() {
      $scope.itemsGridOptions = {
        enableColumnMenus: false,
        enableFiltering: true,
        // flatEntityAccess: true,
        fastWatch: true,
        enableRowSelection: true,
        multiSelect: false,
        enableSelectAll: false,
        enableRowHeaderSelection: false,
        rowHeight: 25,
        noUnselect: true,
        columnDefs: [
          {name: 'Код', field: 'code', width: '10%', enableCellEdit: false},
          {name: 'Артикул', field: 'article', width: '10%', enableCellEdit: false},
          {name: 'Наименование', field: 'name', width: '50%', enableCellEdit: false},
          {name: 'Цена', field: 'price', width: '10%', enableFiltering: false},
          {name: 'Заказано', field: 'amount', width: '10%', enableFiltering: false, enableCellEdit: true, type: 'number'},
          {name: 'Остаток', field: 'stock', width: '10%', enableFiltering: false, enableCellEdit: false}
        ],
        onRegisterApi: function (gridApi) {
          $scope.itemsGridApi = gridApi;
          gridApi.edit.on.afterCellEdit($scope, function (rowEntity, colDef, newValue, oldValue) {
            var index = $scope.cartGridOptions.data.indexOf(rowEntity);
            if (rowEntity.amount > 0){
              calcSum(rowEntity);
              if (index === -1) {
                $scope.cartGridOptions.data.push(rowEntity);
              }
            } else {
              rowEntity.amount = '';
              $scope.cartGridOptions.data.splice(index, 1)
            }
            calcTotalSum()
          });
          gridApi.grid.registerRowsProcessor( $scope.categoryFilter, 200 );
        },
        data: $scope.$parent.itemsGridData
      };
    }

    function prepareCartGrid() {
      $scope.cartGridOptions = {
        gridMenuShowHideColumns: false,
        enableColumnMenus: false,
        enableGridMenu: true,
        enableFiltering: true,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false,
        enableCellEditOnFocus: true,
        rowHeight: 25,
        columnDefs: [
          {name: '', field:'del', width: '2%', enableFiltering: false, enableCellEdit: false, allowCellFocus : false,
            cellTemplate: '<div href="" ng-click="grid.appScope.deleteRow(row)" class="text-center ui-grid-cell-contents">' +
            '<a href=""><strong>Х</strong></a></div>'
          },
          {name: 'Код', field: 'code', width: '10%', enableCellEdit: false},
          {name: 'Артикул', field: 'article', width: '10%', enableCellEdit: false},
          {name: 'Наименование', field: 'name', width: '46%', enableCellEdit: false},
          {name: 'Количество', field: 'amount', width: '10%', enableFiltering: false, type: 'number'},
          {name: 'Цена', field: 'price', width: '10%', enableCellEdit: false, enableFiltering: false},
          {name: 'Сумма', field: 'sum', width: '10%', enableCellEdit: false, enableFiltering: false}
        ],
        onRegisterApi: function (gridApi) {
          $scope.cartGridApi = gridApi;
          gridApi.edit.on.afterCellEdit($scope, function (rowEntity, colDef, newValue, oldValue) {
            calcSum(rowEntity);
            calcTotalSum();
          });
        },
        importerDataAddCallback: function (grid, newObjects) {
          $scope.cartGridOptions.data = $scope.cartGridOptions.data.concat(newObjects);
        },
        data: $scope.$parent.cartGridData
      };
    }

    function prepareCategoryGrid() {
      $scope.categoryGridOptions = {
        enableColumnMenus: false,
        enableSorting: true,
        enableFiltering: true,
        enableColumnMenu: false,
        showTreeExpandNoChildren: false,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false,
        rowHeight: 25,
        columnDefs: [
          {name: 'Категория', field: 'name'}
        ],
        onRegisterApi: function (gridApi) {
          gridApi.selection.on.rowSelectionChanged($scope, onCategoryChange)
        },
        data: []
      };

      $http.get("data/categories.json").then(function (response) {
        $scope.categoryGridOptions.data = response.data
      });
    }

    $scope.deleteRow = function(row) {
      var index = $scope.cartGridOptions.data.indexOf(row.entity);
      row.entity.amount = "";
      $scope.cartGridOptions.data.splice(index, 1);
      calcTotalSum();
    };

    $scope.categoryFilter = function (renderedRows) {
      if ($scope.categoryIds.length > 0)
        renderedRows.forEach(function (row) {
          if ($scope.categoryIds.indexOf(row.entity.categoryId) === -1){
            row.visible = false;
          }
        });

      return renderedRows;
    };

    function getChildrenCategories(row) {
      var children = row.treeNode.children;
      $scope.categoryIds.push(row.entity.id);
      if (children.length) {
        children.forEach(function (child) {
          getChildrenCategories(child.row)
        });
      }
    }

    function onCategoryChange(categoriesRow) {
      $scope.categoryIds = [];
      if (categoriesRow.isSelected) {
        getChildrenCategories(categoriesRow);
      }
      $scope.itemsGridApi.grid.refresh();
    }

    function calcSum(row) {
      if (row.amount) {
        row.sum = row.amount * row.price;
      }
    }
    
    function calcTotalSum() {
      $scope.$parent.totalSum =  $scope.cartGridOptions.data.reduce(function (sum, row) {
        return sum + row.sum;
      }, 0)
    }

    $scope.createOrder = function createOrder() {

    }
  }
]);






