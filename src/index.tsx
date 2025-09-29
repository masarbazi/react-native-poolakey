import { useEffect } from 'react';
import ReactNativePoolakey, { type Spec } from './NativeReactNativePoolakey';

let isConnected = false;
let isConnecting = false;

const connect = (rsa: string): Promise<void> =>
  new Promise((resolve, reject) => {
    if (isConnected || isConnecting) return;
    isConnecting = true;
    ReactNativePoolakey.connectPayment(rsa)
      .then(() => {
        isConnected = true;
        resolve();
      })
      .catch((e: Error) => {
        isConnected = false;
        reject(e);
      })
      .finally(() => (isConnecting = false));
  });

const disconnect = () => {
  ReactNativePoolakey.disconnectPayment();
  isConnected = false;
  isConnecting = false;
};

type Poolakey = Omit<Spec, 'connectPayment' | 'disconnectPayment'> & {
  connect: (rsa: string) => Promise<void>;
  disconnect: () => void;
};

const poolakey: Poolakey = {
  connect: connect,
  disconnect: disconnect,
  // was getting Exception in Host function: "TurboModule method call with less arguments than expected so I had to forward params like this
  purchaseProduct: (productId, developerPayload, dynamicPriceToken) =>
    ReactNativePoolakey.purchaseProduct(
      productId,
      developerPayload,
      dynamicPriceToken
    ),
  subscribeProduct: (productId, devleoperPayload, dynamicPriceToken) =>
    ReactNativePoolakey.subscribeProduct(
      productId,
      devleoperPayload,
      dynamicPriceToken
    ),
  consumePurchase: (purchaseToken) =>
    ReactNativePoolakey.consumePurchase(purchaseToken),
  getPurchasedProducts: ReactNativePoolakey.getPurchasedProducts,
  getSubscribedProducts: ReactNativePoolakey.getSubscribedProducts,
  getInAppSkuDetails: (productIds) =>
    ReactNativePoolakey.getInAppSkuDetails(productIds),
  getSubscriptionSkuDetails: (productIds) =>
    ReactNativePoolakey.getSubscriptionSkuDetails(productIds),
  queryPurchaseProduct: (productId) =>
    ReactNativePoolakey.queryPurchaseProduct(productId),
  querySubscribeProduct: (productId) =>
    ReactNativePoolakey.querySubscribeProduct(productId),
};

export function useBazaar(rsa: string) {
  useEffect(() => {
    poolakey.connect(rsa);
    return poolakey.disconnect;
  }, [rsa]);

  return poolakey;
}

export default poolakey;
