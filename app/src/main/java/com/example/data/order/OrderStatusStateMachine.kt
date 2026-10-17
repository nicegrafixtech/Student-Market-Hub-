package com.example.data.order

import com.example.data.model.UserRole

sealed class OrderEvent {
    data class PaymentSuccessful(val paystackReference: String, val amount: Double) : OrderEvent()
    data class SellerAccept(val storeId: String) : OrderEvent()
    data class SellerDispatch(val storeId: String, val courierNote: String? = null) : OrderEvent()
    data class ConfirmDelivery(val confirmedBy: String) : OrderEvent()
    data class CancelOrder(val reason: String, val cancelledByRole: UserRole) : OrderEvent()
}

object OrderStatusStateMachine {

    fun canTransition(
        from: OrderStatus,
        to: OrderStatus,
        role: UserRole
    ): Boolean {
        if (from == OrderStatus.DELIVERED || from == OrderStatus.CANCELLED) {
            return false
        }

        if (role == UserRole.ADMIN) {
            return to == OrderStatus.CANCELLED || to == OrderStatus.DELIVERED || to == OrderStatus.PAID
        }

        return when (from) {
            OrderStatus.PENDING -> {
                when (to) {
                    OrderStatus.PAID -> true
                    OrderStatus.CANCELLED -> role == UserRole.CUSTOMER || role == UserRole.ADMIN
                    else -> false
                }
            }
            OrderStatus.PAID -> {
                when (to) {
                    OrderStatus.PROCESSING -> role == UserRole.SELLER
                    OrderStatus.DISPATCHED -> role == UserRole.SELLER
                    OrderStatus.DELIVERED -> role == UserRole.CUSTOMER || role == UserRole.SELLER
                    OrderStatus.CANCELLED -> role == UserRole.SELLER || role == UserRole.CUSTOMER
                    else -> false
                }
            }
            OrderStatus.PROCESSING -> {
                when (to) {
                    OrderStatus.DISPATCHED -> role == UserRole.SELLER
                    OrderStatus.DELIVERED -> role == UserRole.CUSTOMER || role == UserRole.SELLER
                    OrderStatus.CANCELLED -> role == UserRole.SELLER
                    else -> false
                }
            }
            OrderStatus.DISPATCHED -> {
                when (to) {
                    OrderStatus.DELIVERED -> true
                    OrderStatus.CANCELLED -> role == UserRole.ADMIN || role == UserRole.SELLER
                    else -> false
                }
            }
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED -> false
        }
    }

    fun transition(
        currentStatus: OrderStatus,
        event: OrderEvent
    ): OrderStatus {
        return when (event) {
            is OrderEvent.PaymentSuccessful -> {
                if (currentStatus == OrderStatus.PENDING) OrderStatus.PAID
                else throw IllegalStateException("Cannot apply PaymentSuccessful to status $currentStatus")
            }
            is OrderEvent.SellerAccept -> {
                if (currentStatus == OrderStatus.PAID) OrderStatus.PROCESSING
                else throw IllegalStateException("Cannot accept order in status $currentStatus")
            }
            is OrderEvent.SellerDispatch -> {
                if (currentStatus == OrderStatus.PROCESSING || currentStatus == OrderStatus.PAID) OrderStatus.DISPATCHED
                else throw IllegalStateException("Cannot dispatch order in status $currentStatus")
            }
            is OrderEvent.ConfirmDelivery -> {
                if (currentStatus == OrderStatus.DISPATCHED || currentStatus == OrderStatus.PAID) OrderStatus.DELIVERED
                else throw IllegalStateException("Cannot confirm delivery in status $currentStatus")
            }
            is OrderEvent.CancelOrder -> {
                if (currentStatus != OrderStatus.DELIVERED && currentStatus != OrderStatus.CANCELLED) OrderStatus.CANCELLED
                else throw IllegalStateException("Cannot cancel finalized order in status $currentStatus")
            }
        }
    }
}
