package common.ecommerce;

public enum CartOperation {
    Add, Remove, Delete, CheckOut, Get
}

/**
 * User input:
 *  Add: add <product name> <count>
 *  Remove: remove <product name>
 *  Delete: delete <product name> <count>
 *  CheckOut: checkout -- rest cart, calculate total price
 *  Get: get -- retrieve cart items
 */
