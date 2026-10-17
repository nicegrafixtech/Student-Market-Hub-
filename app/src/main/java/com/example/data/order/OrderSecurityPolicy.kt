package com.example.data.order

import com.example.data.model.Order
import com.example.data.model.UserProfile
import com.example.data.model.UserRole

class RlsPolicyViolationException(
    val policyName: String,
    override val message: String
) : SecurityException("RLS Policy Violation [$policyName]: $message")

object OrderSecurityPolicy {

    const val POLICY_SELECT = "orders_select_policy"
    const val POLICY_INSERT = "orders_insert_policy"
    const val POLICY_UPDATE = "orders_update_policy"
    const val POLICY_DELETE = "orders_delete_policy"

    fun canReadOrder(order: Order, user: UserProfile, sellerStoreId: String? = null): Boolean {
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.CUSTOMER -> order.customerId == user.id
            UserRole.SELLER -> sellerStoreId != null && order.storeId == sellerStoreId
        }
    }

    fun filterAccessibleOrders(orders: List<Order>, user: UserProfile, sellerStoreId: String? = null): List<Order> {
        return orders.filter { canReadOrder(it, user, sellerStoreId) }
    }

    fun validateInsert(newOrder: Order, user: UserProfile) {
        if (user.role != UserRole.CUSTOMER && user.role != UserRole.ADMIN) {
            throw RlsPolicyViolationException(
                POLICY_INSERT,
                "Only customers can place new orders. Current role: ${user.role}"
            )
        }
        if (user.role == UserRole.CUSTOMER && newOrder.customerId != user.id) {
            throw RlsPolicyViolationException(
                POLICY_INSERT,
                "Cannot create order with customerId ${newOrder.customerId} as authenticated user is ${user.id}"
            )
        }
        val initialStatus = OrderStatus.fromString(newOrder.status)
        if (initialStatus != OrderStatus.PENDING && initialStatus != OrderStatus.PAID) {
            throw RlsPolicyViolationException(
                POLICY_INSERT,
                "New orders must start in PENDING or PAID status. Attempted: $initialStatus"
            )
        }
    }

    fun validateUpdate(
        existingOrder: Order,
        targetStatus: OrderStatus,
        user: UserProfile,
        sellerStoreId: String? = null
    ) {
        if (!canReadOrder(existingOrder, user, sellerStoreId)) {
            throw RlsPolicyViolationException(
                POLICY_UPDATE,
                "User ${user.id} has no read permission for order ${existingOrder.id}"
            )
        }

        val currentStatus = OrderStatus.fromString(existingOrder.status)

        if (currentStatus == OrderStatus.DELIVERED) {
            throw RlsPolicyViolationException(
                POLICY_UPDATE,
                "Order ${existingOrder.id} is already DELIVERED and immutable."
            )
        }
        if (currentStatus == OrderStatus.CANCELLED) {
            throw RlsPolicyViolationException(
                POLICY_UPDATE,
                "Order ${existingOrder.id} is already CANCELLED and cannot be modified."
            )
        }

        if (targetStatus == OrderStatus.PAID && user.role == UserRole.CUSTOMER) {
            throw RlsPolicyViolationException(
                POLICY_UPDATE,
                "Customers cannot directly mark an order as PAID. Server-side payment verification is required."
            )
        }

        val allowed = OrderStatusStateMachine.canTransition(currentStatus, targetStatus, user.role)
        if (!allowed) {
            throw RlsPolicyViolationException(
                POLICY_UPDATE,
                "Transition from $currentStatus to $targetStatus is forbidden for role ${user.role}"
            )
        }
    }

    fun validateDelete(order: Order, user: UserProfile) {
        throw RlsPolicyViolationException(
            POLICY_DELETE,
            "Order deletion is permanently disabled for financial audit integrity. Please mark as CANCELLED instead."
        )
    }
}
