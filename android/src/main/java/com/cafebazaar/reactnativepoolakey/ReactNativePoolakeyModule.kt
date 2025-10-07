package com.cafebazaar.reactnativepoolakey

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.exception.DisconnectException

@ReactModule(name = ReactNativePoolakeyModule.NAME)
class ReactNativePoolakeyModule(private val reactContext: ReactApplicationContext) :
  NativeReactNativePoolakeySpec(reactContext) {

  private var paymentConnection: Connection? = null
  private lateinit var payment: Payment

  data class KeyValue(val key: String?, val value: String?)

  private fun createMap(vararg keyValues: KeyValue): WritableMap {
    val writableMap = Arguments.createMap()
    for (keyValue in keyValues) {
      if (keyValue.key != null) writableMap.putString(keyValue.key, keyValue.value)
    } // end for
    return writableMap
  } // end createMap


  /**
   * creates a new instance of Payment class
   * initiates a connection to Bazaar application
   * @param rsa
   */
  override fun connectPayment(rsa: String?, promise: Promise?) {
    // Step 1: define a payment object
    val securityCheck = if (rsa == null) {
      SecurityCheck.Disable
    } else {
      SecurityCheck.Enable(rsaPublicKey = rsa)
    }
    Log.i("Security check", securityCheck.toString())
    val paymentConfig = PaymentConfiguration(localSecurityCheck = securityCheck)
    payment = Payment(context = reactContext, config = paymentConfig)

    // Step 2: connect to bazaar service
    runIfPaymentInitialized(promise) {
      paymentConnection = payment.connect {
        /* different callbacks to notify the connection state change */
        connectionSucceed {
          promise?.resolve(null)
        }
        connectionFailed {
          promise?.reject(it)
        }
        disconnected {
          promise?.reject(DisconnectException())
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("disconnected", null);
        }
      }
    }
  } // end connectPayment

  /**
   * disconnects the connection from Bazaar
   */
  override fun disconnectPayment() {
    paymentConnection?.disconnect()
  }

  fun startActivity(
    command: PaymentActivity.Command,
    productId: String,
    promise: Promise,
    payload: String?,
    dynamicPriceToken: String?
  ) {

    if (paymentConnection?.getState() != ConnectionState.Connected) {
      promise.reject(IllegalStateException("Payment not connected"))
      return
    }
    val activity = reactContext.currentActivity;
    if (activity == null) {
      promise.reject(IllegalStateException("Activity not found"))
      return
    }

    PaymentActivity.start(
      activity, command, productId, payment, promise, payload, dynamicPriceToken
    )
  } // end startActivity

  override fun purchaseProduct(
    productId: String?, developerPayload: String?, dynamicPriceToken: String?, promise: Promise?
  ) {
    check(reactContext.currentActivity != null) {
      "currentActivity is null"
    }
    if (productId == null) {
      promise?.reject(code = "nullException", message = "productId can't be null")
      return
    }

    runIfPaymentInitialized(promise) {
      startActivity(
        command = PaymentActivity.Command.Purchase,
        productId = productId,
        promise = promise!!,
        payload = developerPayload,
        dynamicPriceToken = dynamicPriceToken,
      )
    }
  } // end purchaseProduct

  override fun subscribeProduct(
    productId: String?, developerPayload: String?, dynamicPriceToken: String?, promise: Promise?
  ) {
    check(reactContext.currentActivity != null) {
      "currentActivity is null"
    }

    if (productId == null) {
      promise?.reject(code = "nullException", message = "productId can't be null")
      return
    }

    runIfPaymentInitialized(promise) {
      startActivity(
        command = PaymentActivity.Command.Subscribe,
        productId = productId,
        promise = promise!!,
        payload = developerPayload,
        dynamicPriceToken = dynamicPriceToken,
      )
    }
  } // end subscribeProduct

  override fun consumePurchase(purchaseToken: String?, promise: Promise?) {
    if (purchaseToken == null) {
      promise?.reject(code = "nullException", message = "purchaseToken can't be null")
      return
    }

    runIfPaymentInitialized(promise) {
      payment.consumeProduct(purchaseToken) {
        consumeFailed { promise?.reject(it) }
        consumeSucceed { promise?.resolve(null) }
      }
    }
  } // end consumePurchase

  override fun getPurchasedProducts(promise: Promise?) {
    runIfPaymentInitialized(promise) {
      payment.getPurchasedProducts {
        queryFailed {
          promise?.reject(it)
        }
        querySucceed {
          promise?.resolve(Arguments.fromList(it))
        }
      }
    }
  } // end getPurchasedProducts

  override fun getSubscribedProducts(promise: Promise?) {
    runIfPaymentInitialized(promise) {
      payment.getSubscribedProducts {
        queryFailed { promise?.reject(it) }
        querySucceed {
          promise?.resolve(Util.getWritableMapOf(it))
        }
      }
    }

  } // end getSubscribedProducts

  override fun queryPurchaseProduct(productId: String?, promise: Promise?) {
    runIfPaymentInitialized(promise) {
      payment.getPurchasedProducts {
        queryFailed { promise?.reject(it) }
        querySucceed { purchaseList ->
          val product = purchaseList.firstOrNull {
            it.productId == productId
          }

          if (product == null) {
            promise?.reject(code = "notFound", "Product with id $productId not found")
          } else {
            promise?.resolve(Util.getWritableMapOf(product))
          }
        }
      }
    }
  } // end queryPurchaseProduct

  override fun querySubscribeProduct(productId: String?, promise: Promise?) {
    runIfPaymentInitialized(promise) {
      payment.getSubscribedProducts {
        queryFailed { promise?.reject(it) }
        querySucceed { purchaseList ->
          val product = purchaseList.firstOrNull {
            it.productId == productId
          }

          if (product == null) {
            promise?.reject(code = "notFound", "Product with id $productId not found")
          } else {
            promise?.resolve(Util.getWritableMapOf(product))
          }
        }
      }
    }
  } // end querySubscribeProduct

  override fun getInAppSkuDetails(productIds: ReadableArray?, promise: Promise?) {
    if (productIds == null || productIds.size() == 0) return
    val productIdsList = mutableListOf<String>()
    for (productId in productIds.toArrayList()) {
      productIdsList.add(productId.toString())
    } // end for
    runIfPaymentInitialized(promise) {
      payment.getInAppSkuDetails(productIdsList) {
        getSkuDetailsFailed { promise?.reject(it) }
        getSkuDetailsSucceed {
          promise?.resolve(Util.getWritableMapOf(it))
        }
      }
    }
  } // end getInAppSkuDetails

  override fun getSubscriptionSkuDetails(productIds: ReadableArray?, promise: Promise?) {
    if (productIds == null || productIds.size() == 0) return
    val productIdsList = mutableListOf<String>()
    for (productId in productIds.toArrayList()) {
      productIdsList.add(productId.toString())
    } // end for
    runIfPaymentInitialized(promise) {
      payment.getSubscriptionSkuDetails(productIdsList) {
        getSkuDetailsFailed { promise?.reject(it) }
        getSkuDetailsSucceed {
          promise?.resolve(Util.getWritableMapOf(it))
        }
      }
    }
  } // end getSubscriptionSkuDetails

  private fun runIfPaymentInitialized(promise: Promise?, callback: () -> Unit) {
    if (::payment.isInitialized.not()) {
      promise?.reject(IllegalStateException("Payment not initialized"))
      return
    }
    callback.invoke()
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "ReactNativePoolakey"
  }
}
